package igentuman.nc.multiblock.particle_chamber;

import igentuman.nc.block.target_chamber.DetectorBlock;
import igentuman.nc.block.target_chamber.entity.TargetChamberBeamPortBE;
import igentuman.nc.block.target_chamber.entity.TargetChamberControllerBE;
import igentuman.nc.content.particles.ParticleStack;
import igentuman.nc.multiblock.AbstractMultiblock;
import igentuman.nc.multiblock.MultiblockHandler;
import igentuman.nc.multiblock.ValidationResult;
import igentuman.nc.util.BlockPosInstance;
import igentuman.nc.util.PortMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
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
import static igentuman.nc.multiblock.particle_chamber.TargetChamberRegistration.*;
import static igentuman.nc.util.PortMode.PORT_MODE;
import static igentuman.nc.util.TagUtil.getBlocksByTagKey;

public class TargetChamberMultiblock extends AbstractMultiblock {

    protected final HashMap<Long, DetectorBlock> validDetectors = new HashMap<>();
    protected final HashSet<Long> allDetectors = new HashSet<>();
    public double efficiency = 0;
    public int power = 0;
    public final HashMap<Long, BlockEntity> beamPorts = new HashMap<>();

    @Override
    public int maxHeight() {
        return PARTICLE_CHAMBER_CONFIG.MAX_SIZE.get();
    }

    @Override
    public int maxWidth() {
        return PARTICLE_CHAMBER_CONFIG.MAX_SIZE.get();
    }

    @Override
    public int maxDepth() {
        return PARTICLE_CHAMBER_CONFIG.MAX_SIZE.get();
    }

    @Override
    public int minHeight() {
        return PARTICLE_CHAMBER_CONFIG.MIN_SIZE.get();
    }

    @Override
    public int minWidth() {return PARTICLE_CHAMBER_CONFIG.MIN_SIZE.get(); }

    @Override
    public int minDepth() { return PARTICLE_CHAMBER_CONFIG.MIN_SIZE.get(); }

    public TargetChamberMultiblock(TargetChamberControllerBE TargetChamberControllerBE) {
        super(
                getBlocksByTagKey(TARGET_CHAMBER_CASING_BLOCKS.location().toString()),
                getBlocksByTagKey(TARGET_CHAMBER_INNER_BLOCKS.location().toString()),
                new TargetChamberController(TargetChamberControllerBE)
        );
        id = "target_chamber_"+TargetChamberControllerBE.getBlockPos().toShortString();

        controllerBe = TargetChamberControllerBE;
        MultiblockHandler.get(getLevel().dimension()).addMultiblock(this);
    }

    public Map<Long, DetectorBlock> validateDetectors() {
        if(validDetectors.isEmpty()) {
            for(long packedPos: allDetectors) {
                BlockPos hpos = BlockPos.of(packedPos);
                Block block = getBlockState(hpos).getBlock();
                if(block instanceof DetectorBlock hs) {
                    if(hs.isValid(getLevel(), hpos, this)) {
                        validDetectors.put(packedPos, hs);
                    }
                }
            }
        }
        controllerBE().detectorsCount = validDetectors.size();
        return validDetectors;
    }


    @Override
    public void tick(Level level) {
        super.tick(level);
    }

    protected boolean isDetector(BlockState bs) {
        return bs.getBlock() instanceof DetectorBlock;
    }

    protected boolean isDetector(BlockPos pos) {
        return allDetectors.contains(pos.asLong()) || isDetector(getBlockState(pos));
    }

    @Override
    public void validateOuter() {
        debugLog("Starting target chamber outer validation");
        
        super.validateOuter();
        if(!validationResult.isValid) {
            debugLog("Base outer validation failed with result: " + validationResult);
            return;
        }
        
        debugLog("Checking target chamber proportions - " + width() + "x" + height() + "x" + depth());
        
        if(
                height() % 2 == 0 || width() % 2 == 0 || depth() % 2 == 0
                || (height() != width() || height() != depth() || width() != depth())
        ) {
            debugLog("Proportion validation failed - dimensions must be odd and equal (cube)");
            debugLog("Current: " + width() + "x" + height() + "x" + depth() + 
                    " (even dimensions: H=" + (height() % 2 == 0) + 
                    ", W=" + (width() % 2 == 0) + 
                    ", D=" + (depth() % 2 == 0) + ")");
            validationResult = ValidationResult.WRONG_PROPORTIONS;
            outerValid = false;
            return;
        }
        
        outerValid = true;
        validationResult = ValidationResult.VALID;
        debugLog("Target chamber outer validation completed successfully");
    }

    @Override
    public void validate() {
        debugLog("=== Starting Target Chamber validation at " + controllerPos.toShortString() + " ===");
        
        validDetectors.clear();
        allDetectors.clear();
        
        debugLog("Cleared target chamber specific caches");
        super.validate();
        
        if(validationResult.isValid) {
            debugLog("Target chamber validation completed successfully");
            debugLog("Detectors - All: " + allDetectors.size() + 
                    ", Valid: " + validDetectors.size() + 
                    ", Efficiency: " + String.format("%.2f%%", efficiency * 100) + 
                    ", Power: " + power);
        } else {
            debugLog("Target chamber validation failed with result: " + validationResult);
        }
    }

    @Override
    public void validateInner()
    {
        debugLog("Starting target chamber inner validation");
        
        efficiency = 1;
        power = 0;
        if(!outerValid) {
            debugLog("Skipping inner validation - outer validation failed");
            clearStats();
            return;
        }

        debugLog("Indexing inner blocks for target chamber");
        indexInnerBlocks();
        if(!validationResult.isValid) {
            debugLog("Inner block indexing failed with result: " + validationResult);
            clearStats();
            return;
        }
        
        debugLog("Checking center block for camera at " + getCenterBlock().toShortString());
        if(!getBlockState(getCenterBlock()).is(TARGET_CHAMBER_BLOCKS.get("target_chamber_camera").get())) {
            debugLog("Center block validation failed - expected camera, found: " + 
                    getBlockState(getCenterBlock()).getBlock().getDescriptionId());
            validationResult = ValidationResult.WRONG_INNER;
            errorBlockPos = getCenterBlock();
            clearStats();
            return;
        }
        
        debugLog("Validating beam lines");
        validateBeamLines();
        
        // Update controller stats
        controllerBE().allDetectors = allDetectors.size();
        controllerBE().efficiency = efficiency*100;
        controllerBE().energyPerTick = power;
        controllerBE().connectedPorts = connectedPorts;
        controllerBE().detectorsCount = validDetectors.size();
        controllerBE().height = height;
        controllerBE().width = width;
        
        debugLog("Target chamber inner validation completed - Efficiency: " + String.format("%.2f%%", efficiency * 100) + 
                ", Power: " + power + ", Detectors: " + validDetectors.size() + "/" + allDetectors.size());
        controllerBE().depth = depth;
        if(validDetectors.isEmpty()) {
            validationResult = ValidationResult.NO_DETECTORS;
            errorBlockPos = getCenterBlock();
            clearStats();
            return;
        }
        controllerBE().refresh();
    }

    private void validateBeamLines() {
        for(Direction dir: List.of(Direction.WEST, Direction.EAST, Direction.NORTH, Direction.SOUTH)) {
            BlockPos pos = getCenterBlock();
            for(int i = 1; i < width() / 2; i++) {
                pos = pos.relative(dir, i);
                BlockState bs = getBlockState(pos);
                if(!bs.is(ACCELERATOR_BLOCKS.get("particle_beam").get())) {
                    validationResult = ValidationResult.WRONG_INNER;
                    errorBlockPos = pos;
                    return;
                }
            }
            BlockPos target = getCenterBlock().relative(dir, width() / 2);
            BlockState bs = getBlockState(target);
            if(!bs.is(TARGET_CHAMBER_BLOCKS.get("target_chamber_beam_port").get())) {
                validationResult = ValidationResult.NO_PORT;
                errorBlockPos = new BlockPos(target);
                return;
            } else {
                beamPorts.put(target.asLong(), getBlockEntity(target));
            }
        }
    }

    private void indexInnerBlocks() {
        BlockPosInstance toCheck = new BlockPosInstance(initialPos());
        for(int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                for (int z = 1; z < depth - 1; z++) {
                    switch (getMultiblockDirection().ordinal()) {
                        case 3 -> toCheck.revert().east(x - leftCasing);
                        case 5 -> toCheck.revert().north(x - leftCasing);
                        case 2 -> toCheck.revert().west(x - leftCasing);
                        case 4 -> toCheck.revert().south(x - leftCasing);
                    }
                    toCheck.above(y - bottomCasing).relative(getControllerDirection(), -z);
                    if(!processInnerBlock(toCheck)) {
                        validationResult = ValidationResult.WRONG_INNER;
                        errorBlockPos = new BlockPos(toCheck);
                        return;
                    }
                }
            }
        }
        validationResult = ValidationResult.VALID;
        errorBlockPos = null;
    }

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

        if(isDetector(bs)) {
            DetectorBlock detectorBlock = (DetectorBlock) bs.getBlock();
            if(detectorBlock.isValid(getLevel(), toCheck, this)) {
                efficiency += detectorBlock.efficiency;
                power += detectorBlock.power;
                validDetectors.put(toCheck.asLong(), detectorBlock);
            }
            addIfNotExists(toCheck, allDetectors);
            return true;
        }


        return isValidForInner(bs);
    }

    public boolean checkAttachmentToBlock(Class<?> toCheck, Level level, BlockPos pos, Direction dir) {
        if (
                getBottomLeftBlock().getX() >= pos.getX()
                && getBottomLeftBlock().getY() >= pos.getX()
                && getBottomLeftBlock().getZ() >= pos.getZ()
                && getTopRightBlock().getX() <= pos.getX()
                && getTopRightBlock().getY() <= pos.getY()
                && getTopRightBlock().getZ() <= pos.getZ()
                && !allBlocks.contains(pos.asLong())
        ) {
            return false;
        }

        return false;
    }

    @Override
    public void removeFromCacheIfChanged(BlockPos pos) {
        long packedPos = pos.asLong();
        if (beCache.containsKey(packedPos)) {
            BlockEntity be = getLevel().getExistingBlockEntity(pos);
            if(be != beCache.get(packedPos) || (be != null && be.isRemoved())) {
                beCache.remove(packedPos);
            }
        }
        if (bsCache.containsKey(packedPos)) {
            BlockState bs = getLevel().getBlockState(pos);
            BlockState cachedState = bsCache.get(packedPos);
            if(cachedState == null || !bs.is(bsCache.get(packedPos).getBlock())) {
                bsCache.remove(packedPos);
                allDetectors.remove(packedPos);
            }
        }
    }

    public void clearStats()
    {
        controller().clearStats();
        controllerBE().bottomLeft = BlockPos.ZERO;
        controllerBE().topRight = BlockPos.ZERO;
        controllerBE().setChanged();
    }

    protected Direction getControllerDirection() {
        return controllerBE().getFacing();
    }

    public void extractParticle(int id, ParticleStack outputParticle) {
        int i = -1;
        for(long pos: beamPorts.keySet()) {
            if(getBlockState(pos).getValue(PORT_MODE) == PortMode.Mode.OUTPUT) {
                BlockEntity be = beamPorts.get(pos);
                i++;
                if(id != i) {
                    continue;
                }

                if(be instanceof TargetChamberBeamPortBE port) {
                    port.extractParticle(outputParticle);
                }
            }
        }
    }
}
