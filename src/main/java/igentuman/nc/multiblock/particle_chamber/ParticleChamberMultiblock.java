package igentuman.nc.multiblock.particle_chamber;

import igentuman.api.nc.multiblock.MultiblockController;
import igentuman.nc.block.entity.MultiblockControllerBE;
import igentuman.nc.block.entity.ParticleChamberControllerBE;
import igentuman.nc.block.target_chamber.DetectorBlock;
import igentuman.nc.block.target_chamber.entity.TargetChamberBeamPortBE;
import igentuman.nc.content.particles.ParticleStack;
import igentuman.nc.multiblock.AbstractMultiblock;
import igentuman.nc.multiblock.ValidationResult;
import igentuman.nc.util.PortMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static igentuman.nc.NuclearCraft.debugLog;
import static igentuman.nc.multiblock.accelerator.AcceleratorRegistration.ACCELERATOR_BLOCKS;
import static igentuman.nc.multiblock.particle_chamber.ParticleChamberRegistration.PARTICLE_CHAMBER_BLOCKS;
import static igentuman.nc.util.PortMode.PORT_MODE;

/**
 * Shared base for particle chamber multiblock validators.
 * Target chamber, decay chamber, collision chamber all extend this.
 */
public abstract class ParticleChamberMultiblock extends AbstractMultiblock {

    public final HashMap<Long, BlockEntity> beamPorts = new HashMap<>();
    public double efficiency = 0;
    public int power = 0;
    protected final HashMap<Long, DetectorBlock> validDetectors = new HashMap<>();
    protected final HashSet<Long> allDetectors = new HashSet<>();

    protected ParticleChamberMultiblock(HashSet<Block> validOuterBlocks, HashSet<Block> validInnerBlocks, ParticleChamberController controller) {
        super(validOuterBlocks, validInnerBlocks, controller);
    }

    public Map<Long, DetectorBlock> validateDetectors() {
        if (validDetectors.isEmpty()) {
            for (long packedPos : allDetectors) {
                BlockPos hpos = BlockPos.of(packedPos);
                Block block = getBlockState(hpos).getBlock();
                if (block instanceof DetectorBlock hs) {
                    if (hs.isValid(getLevel(), hpos, this)) {
                        validDetectors.put(packedPos, hs);
                    }
                }
            }
        }
        controllerBE().detectorsCount = validDetectors.size();
        return validDetectors;
    }

    @Override
    public ParticleChamberController controller() {
        return (ParticleChamberController) controller;
    }

    @Override
    protected ParticleChamberControllerBE controllerBE() {
        if (controllerBe == null) {
            controllerBe = controller().controllerBE();
        }
        return (ParticleChamberControllerBE) controllerBe;
    }

    protected void indexInnerBlocks() {
        BlockPos thePos = initialPos().copy();
        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                for (int z = 1; z < depth - 1; z++) {
                    switch (getControllerDirection().ordinal()) {
                        case 3 -> thePos = initialPos().copy().east(x - leftCasing).above(y - bottomCasing).relative(getControllerDirection(), -z);
                        case 5 -> thePos = initialPos().copy().north(x - leftCasing).above(y - bottomCasing).relative(getControllerDirection(), -z);
                        case 2 -> thePos = initialPos().copy().west(x - leftCasing).above(y - bottomCasing).relative(getControllerDirection(), -z);
                        case 4 -> thePos = initialPos().copy().south(x - leftCasing).above(y - bottomCasing).relative(getControllerDirection(), -z);
                    }
                    if (!processInnerBlock(thePos)) {
                        validationResult = ValidationResult.WRONG_INNER;
                        errorBlockPos = new BlockPos(thePos);
                        return;
                    }
                }
            }
        }
        validationResult = ValidationResult.VALID;
        errorBlockPos = null;
    }

    protected boolean isDetector(BlockState bs) {
        return bs.getBlock() instanceof DetectorBlock;
    }

    protected boolean isDetector(BlockPos pos) {
        return allDetectors.contains(pos.asLong()) || isDetector(getBlockState(pos));
    }

    @Override
    protected boolean processInnerBlock(BlockPos toCheck) {
        addIfNotExists(toCheck, allBlocks);
        final BlockState bs = getBlockState(toCheck);
        if (isDetector(bs)) {
            DetectorBlock detectorBlock = (DetectorBlock) bs.getBlock();
            if (detectorBlock.isValid(getLevel(), toCheck, this)) {
                efficiency += detectorBlock.efficiency;
                power += detectorBlock.power;
                validDetectors.put(toCheck.asLong(), detectorBlock);
            }
            addIfNotExists(toCheck, allDetectors);
            return true;
        }
        return isValidForInner(bs);
    }

    @Override
    public void validateInner() {
        debugLog("Target chamber inner validation");
        efficiency = 1;
        power = 0;
        if (!outerValid) {
            clearStats();
            return;
        }
        indexInnerBlocks();
        if (!validationResult.isValid) {
            clearStats();
            return;
        }
        if (!getBlockState(getCenterBlock()).is(PARTICLE_CHAMBER_BLOCKS.get("target_chamber_camera").get())) {
            validationResult = ValidationResult.WRONG_INNER;
            errorBlockPos = getCenterBlock();
            clearStats();
            return;
        }
        validateBeamLines();
        validateDetectors();
        controllerBE().allDetectors = allDetectors.size();
        controllerBE().efficiency = efficiency * 100;
        controllerBE().energyPerTick = power;
        controllerBE().connectedPorts = connectedPorts;
        controllerBE().detectorsCount = validDetectors.size();
        controllerBE().height = height;
        controllerBE().width = width;
        controllerBE().depth = depth;
        if (validDetectors.isEmpty()) {
            validationResult = ValidationResult.NO_DETECTORS;
            errorBlockPos = getCenterBlock();
            clearStats();
            return;
        }
        controllerBE().refresh();
    }

    protected void onCachedBlockRemoved(long packedPos) {
        allDetectors.remove(packedPos);
    }

    @Override
    public void clearStats() {
        if (controller() != null) {
            controller().clearStats();
        }
        validDetectors.clear();
        allDetectors.clear();
        controllerBE().bottomLeft = BlockPos.ZERO;
        controllerBE().topRight = BlockPos.ZERO;
        controllerBE().connectedPorts = 0;
        controllerBE().detectorsCount = 0;
        controllerBE().efficiency = 0;
        controllerBE().energyPerTick = 0;
        controllerBE().markDirty();
    }

    private void validateBeamLines() {
        for (Direction dir : List.of(Direction.WEST, Direction.EAST, Direction.NORTH, Direction.SOUTH)) {
            BlockPos pos = getCenterBlock();
            for (int i = 1; i < width() / 2; i++) {
                pos = pos.relative(dir, i);
                BlockState bs = getBlockState(pos);
                if (!bs.is(ACCELERATOR_BLOCKS.get("particle_beam").get())) {
                    validationResult = ValidationResult.WRONG_INNER;
                    errorBlockPos = pos;
                    return;
                }
            }
            BlockPos target = getCenterBlock().relative(dir, width() / 2);
            BlockState bs = getBlockState(target);
            if (!bs.is(PARTICLE_CHAMBER_BLOCKS.get("target_chamber_beam_port").get())) {
                validationResult = ValidationResult.NO_PORT;
                errorBlockPos = new BlockPos(target);
                return;
            } else {
                beamPorts.put(target.asLong(), getBlockEntity(target));
            }
        }
    }

    /**
     * All particle chambers must form an odd-sided cube and use the controller's facing direction.
     */
    @Override
    public void validateOuter() {
        debugLog("Particle chamber outer validation for " + getClass().getSimpleName());
        super.validateOuter();
        if (!validationResult.isValid) {
            debugLog("Base outer validation failed: " + validationResult);
            return;
        }

        if (requireCubeShape()) {
            if (height() % 2 == 0 || width() % 2 == 0 || depth() % 2 == 0
                    || height() != width() || height() != depth() || width() != depth()) {
                debugLog("Cube proportion check failed: " + width() + "x" + height() + "x" + depth());
                validationResult = ValidationResult.WRONG_PROPORTIONS;
                outerValid = false;
                return;
            }
        }

        outerValid = true;
        validationResult = ValidationResult.VALID;
    }

    /** Override to false for non-cube chamber shapes (e.g. asymmetric collision). */
    protected boolean requireCubeShape() {
        return true;
    }

    @Override
    protected Direction getControllerDirection() {
        if (controllerBE() != null && controllerBE().getBlockState() != null) {
            try {
                return controllerBE().getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
            } catch (Exception ignored) { }
        }
        return null;
    }

    @Override
    public void removeFromCacheIfChanged(BlockPos pos) {
        long packedPos = pos.asLong();
        if (beCache.containsKey(packedPos)) {
            BlockEntity be = getLevel().getExistingBlockEntity(pos);
            if (be != beCache.get(packedPos) || (be != null && be.isRemoved())) {
                beCache.remove(packedPos);
            }
        }
        if (bsCache.containsKey(packedPos)) {
            net.minecraft.world.level.block.state.BlockState bs = getLevel().getBlockState(pos);
            net.minecraft.world.level.block.state.BlockState cachedState = bsCache.get(packedPos);
            if (cachedState == null || !bs.is(cachedState.getBlock())) {
                bsCache.remove(packedPos);
                onCachedBlockRemoved(packedPos);
            }
        }
        //check if block in AABB between bottomLeft and topRight
        if (bottomLeft != null && topRight != null) {
            if (pos.getX() >= Math.min(bottomLeft.getX(), topRight.getX()) && pos.getX() <= Math.max(bottomLeft.getX(), topRight.getX()) &&
                pos.getY() >= Math.min(bottomLeft.getY(), topRight.getY()) && pos.getY() <= Math.max(bottomLeft.getY(), topRight.getY()) &&
                pos.getZ() >= Math.min(bottomLeft.getZ(), topRight.getZ()) && pos.getZ() <= Math.max(bottomLeft.getZ(), topRight.getZ())) {
                hasToRefresh = true;
            }
        }
    }

    public void extractParticle(int id, ParticleStack outputParticle) {
        int i = -1;
        for (long pos : beamPorts.keySet()) {
            if (getBlockState(pos).getValue(PORT_MODE) == PortMode.Mode.OUTPUT) {
                BlockEntity be = beamPorts.get(pos);
                i++;
                if (id != i) continue;
                if (be instanceof TargetChamberBeamPortBE port) {
                    port.extractParticle(outputParticle);
                }
            }
        }
    }
}
