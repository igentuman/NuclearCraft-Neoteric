package igentuman.nc.multiblock.particle_chamber;

import igentuman.nc.block.target_chamber.DetectorBlock;
import igentuman.nc.block.target_chamber.entity.TargetChamberBeamPortBE;
import igentuman.nc.block.target_chamber.entity.TargetChamberControllerBE;
import igentuman.nc.content.particles.ParticleStack;
import igentuman.nc.multiblock.MultiblockHandler;
import igentuman.nc.multiblock.ValidationResult;
import igentuman.nc.util.PortMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static igentuman.nc.NuclearCraft.debugLog;
import static igentuman.nc.handler.config.AcceleratorConfig.PARTICLE_CHAMBER_CONFIG;
import static igentuman.nc.multiblock.accelerator.AcceleratorRegistration.ACCELERATOR_BLOCKS;
import static igentuman.nc.multiblock.particle_chamber.ParticleChamberRegistration.*;
import static igentuman.nc.util.PortMode.PORT_MODE;
import static igentuman.nc.util.TagUtil.getBlocksByTagKey;

public class TargetChamberMultiblock extends ParticleChamberMultiblock {

    protected final HashMap<Long, DetectorBlock> validDetectors = new HashMap<>();
    protected final HashSet<Long> allDetectors = new HashSet<>();

    @Override
    public int maxHeight() { return PARTICLE_CHAMBER_CONFIG.MAX_SIZE.get(); }

    @Override
    public int maxWidth() { return PARTICLE_CHAMBER_CONFIG.MAX_SIZE.get(); }

    @Override
    public int maxDepth() { return PARTICLE_CHAMBER_CONFIG.MAX_SIZE.get(); }

    @Override
    public int minHeight() { return PARTICLE_CHAMBER_CONFIG.MIN_SIZE.get(); }

    @Override
    public int minWidth() { return PARTICLE_CHAMBER_CONFIG.MIN_SIZE.get(); }

    @Override
    public int minDepth() { return PARTICLE_CHAMBER_CONFIG.MIN_SIZE.get(); }

    public TargetChamberMultiblock(TargetChamberControllerBE controllerBE) {
        super(
                getBlocksByTagKey(TARGET_CHAMBER_CASING_BLOCKS.location().toString()),
                getBlocksByTagKey(TARGET_CHAMBER_INNER_BLOCKS.location().toString()),
                new TargetChamberController(controllerBE)
        );
        id = "target_chamber_" + controllerBE.getBlockPos().toShortString();
        controllerBe = controllerBE;
        MultiblockHandler.get(getLevel().dimension()).addMultiblock(this);
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

    protected boolean isDetector(BlockState bs) {
        return bs.getBlock() instanceof DetectorBlock;
    }

    protected boolean isDetector(BlockPos pos) {
        return allDetectors.contains(pos.asLong()) || isDetector(getBlockState(pos));
    }

    @Override
    public void validate() {
        validDetectors.clear();
        allDetectors.clear();
        super.validate();
        if (validationResult.isValid) {
            debugLog("Target chamber validation OK. detectors=" + validDetectors.size() + "/" + allDetectors.size());
        }
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

    private void indexInnerBlocks() {
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

    @Override
    protected TargetChamberControllerBE controllerBE() {
        if (controllerBe == null) {
            controllerBe = controller().controllerBE();
        }
        return (TargetChamberControllerBE) controllerBe;
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
    protected void onCachedBlockRemoved(long packedPos) {
        allDetectors.remove(packedPos);
    }

    @Override
    public void clearStats() {
        super.clearStats();
        if (controller() != null) {
            controller().clearStats();
        }
        controllerBE().bottomLeft = BlockPos.ZERO;
        controllerBE().topRight = BlockPos.ZERO;
        controllerBE().setChanged();
    }

    @Override
    protected Direction getControllerDirection() {
        return controllerBE().getFacing();
    }

    @Override
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
