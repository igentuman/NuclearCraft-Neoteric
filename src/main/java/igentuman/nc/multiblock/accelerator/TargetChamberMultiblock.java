package igentuman.nc.multiblock.accelerator;

import igentuman.nc.block.accelerator.DetectorBlock;
import igentuman.nc.block.entity.accelerator.TargetChamberControllerBE;
import igentuman.nc.multiblock.AbstractMultiblock;
import igentuman.nc.multiblock.MultiblockHandler;
import igentuman.nc.multiblock.ValidationResult;
import igentuman.nc.util.NCBlockPos;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static igentuman.nc.handler.config.AcceleratorConfig.PARTICLE_CHAMBER_CONFIG;
import static igentuman.nc.multiblock.accelerator.TargetChamberRegistration.TARGET_CHAMBER_CASING_BLOCKS;
import static igentuman.nc.multiblock.accelerator.TargetChamberRegistration.TARGET_CHAMBER_INNER_BLOCKS;
import static igentuman.nc.util.TagUtil.getBlocksByTagKey;

public class TargetChamberMultiblock extends AbstractMultiblock {

    protected final HashMap<Long, DetectorBlock> validDetectors = new HashMap<>();
    protected final HashSet<Long> allDetectors = new HashSet<>();
    protected double cellsHeatMult = 0.0D;

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
    public void tick() {
        super.tick();
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
    }

    @Override
    public void validateInner()
    {
        if(!outerValid) {
            clearStats();
            return;
        }

        //Stage 1: Index all inner blocks
        indexInnerBlocks();
        if(validationResult != ValidationResult.VALID) {
            clearStats();
            return;
        }
        //Stage 4: count heat sinks and their cooling
        indexDetectors();
        //Stage 5: update controller stats
        controllerBE().allDetectors = allDetectors.size();
        controllerBE().connectedPorts = connectedPorts;
        controllerBE().detectorsCount = validDetectors.size();
        controllerBE().height = height;
        controllerBE().width = width;
        controllerBE().depth = depth;
        controllerBE().refresh();
    }

    private void indexDetectors() {
        validDetectors.clear();
        for(long pos: allDetectors) {
            BlockPos hsPos = BlockPos.of(pos);
            if(isDetectorValid(hsPos)) {
                DetectorBlock hb = (DetectorBlock) getBlockState(pos).getBlock();
                validDetectors.put(pos, hb);
            }
        }
    }

    private boolean isDetectorValid(BlockPos pos) {
        DetectorBlock hb = (DetectorBlock) getBlockState(pos).getBlock();
        return hb.isValid(getLevel(), pos, this);
    }

    private void indexInnerBlocks() {
        NCBlockPos toCheck = new NCBlockPos(initialPos());
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
}
