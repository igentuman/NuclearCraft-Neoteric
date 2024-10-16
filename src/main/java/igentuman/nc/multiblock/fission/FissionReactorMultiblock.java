package igentuman.nc.multiblock.fission;

import igentuman.nc.block.entity.fission.*;
import igentuman.nc.block.fission.FissionFuelCellBlock;
import igentuman.nc.block.fission.HeatSinkBlock;
import igentuman.nc.block.fission.IrradiationChamberBlock;
import igentuman.nc.handler.MultiblockHandler;
import igentuman.nc.multiblock.AbstractNCMultiblock;
import igentuman.nc.util.NCBlockPos;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.*;

import static igentuman.nc.handler.config.FissionConfig.FISSION_CONFIG;
import static igentuman.nc.util.TagUtil.getBlocksByTagKey;

public class FissionReactorMultiblock extends AbstractNCMultiblock {

    private int irradiationConnections = 0;

    @Override
    public int maxHeight() {
        return FISSION_CONFIG.MAX_SIZE.get();
    }

    @Override
    public int maxWidth() {
        return FISSION_CONFIG.MAX_SIZE.get();
    }

    @Override
    public int maxDepth() {
        return FISSION_CONFIG.MAX_SIZE.get();
    }

    @Override
    public int minHeight() {
        return FISSION_CONFIG.MIN_SIZE.get();
    }

    @Override
    public int minWidth() {return FISSION_CONFIG.MIN_SIZE.get(); }

    @Override
    public int minDepth() { return FISSION_CONFIG.MIN_SIZE.get(); }

    private final List<Block> validModerators;

    public HashMap<BlockPos, HeatSinkBlock> activeHeatSinks = new HashMap<>();
    private List<BlockPos> moderators = new ArrayList<>();
    private List<BlockPos> irradiators = new ArrayList<>();
    public List<BlockPos> heatSinks = new ArrayList<>();
    public List<BlockPos> fuelCells = new ArrayList<>();
    private double heatSinkCooling = 0;

    public FissionReactorMultiblock(FissionControllerBE<?> fissionControllerBE) {
        super(
                getBlocksByTagKey(FissionBlocks.CASING_BLOCKS.location().toString()),
                getBlocksByTagKey(FissionBlocks.INNER_REACTOR_BLOCKS.location().toString())
        );
        id = "fission_reactor_"+fissionControllerBE.getBlockPos().toShortString();
        validModerators = getBlocksByTagKey(FissionBlocks.MODERATORS_BLOCKS.location().toString());
        MultiblockHandler.addMultiblock(this);
        controller = new FissionReactorController(fissionControllerBE);
    }

    public Map<BlockPos, HeatSinkBlock> activeHeatSinks() {
        if(activeHeatSinks.isEmpty()) {
            for(BlockPos hpos: heatSinks) {
                Block block = getBlockState(hpos).getBlock();
                if(block instanceof HeatSinkBlock hs) {
                    if(hs.isValid(getLevel(), hpos)) {
                        activeHeatSinks.put(hpos, hs);
                    }
                }
            }
        }
        ((FissionControllerBE<?>)controller().controllerBE()).heatSinksCount = activeHeatSinks.size();
        return activeHeatSinks;
    }

    public boolean isModerator(BlockPos pos, Level level) {
        return  validModerators.contains(level.getBlockState(pos).getBlock());
    }

    public boolean isModerator(BlockPos pos) {
        return  validModerators.contains(getBlockState(pos).getBlock());
    }

    public boolean isIrradiator(BlockPos pos) {
        return  getBlockState(pos).getBlock() instanceof IrradiationChamberBlock;
    }

    protected boolean isHeatSink(BlockPos pos) {
        return getBlockState(pos).getBlock() instanceof HeatSinkBlock;
    }

    protected boolean isFuelCell(BlockPos pos) {
        return getBlockState(pos).getBlock() instanceof FissionFuelCellBlock;
    }

    private boolean isAttachedToFuelCell(BlockPos toCheck) {
        for(Direction d : Direction.values()) {
            if(toCheck instanceof NCBlockPos) {
                ((NCBlockPos) toCheck).revert();
            }
            if(isFuelCell(toCheck.relative(d))) {
                return true;
            }
        }
        return false;
    }

    public void validateInner()
    {
        super.validateInner();
        heatSinkCooling = getHeatSinkCooling(true);
        FissionControllerBE<?> controller = (FissionControllerBE<?>) controller().controllerBE();
        controller.fuelCellsCount = fuelCells.size();
        controller.moderatorsCount = moderators.size();
        controller.irradiationConnections = irradiationConnections;
    }

    @Override
    protected boolean processInnerBlock(BlockPos toCheck) {
        if(isFuelCell(toCheck)) {
            fuelCells.add(toCheck);
            int moderatorAttachments = getAttachedModeratorsToFuelCell(toCheck);
            ((FissionControllerBE<?>)controller().controllerBE()).fuelCellMultiplier += countAdjuscentFuelCells((NCBlockPos) toCheck, 3);
            ((FissionControllerBE<?>)controller().controllerBE()).moderatorCellMultiplier += (countAdjuscentFuelCells((NCBlockPos) toCheck, 1)+1)*moderatorAttachments;
            ((FissionControllerBE<?>)controller().controllerBE()).moderatorAttachments += moderatorAttachments;
        }
        if(isModerator(toCheck)) {
            if(isAttachedToFuelCell(toCheck)) {
                moderators.add(toCheck);
            }
        }
        if(isHeatSink(toCheck)) {
            heatSinks.add(toCheck);
        }
        if(isIrradiator(toCheck)) {
            irradiators.add(toCheck);
            countIrradiationConnections(toCheck);
        }
        return true;
    }

    private int getAttachedModeratorsToFuelCell(BlockPos toCheck) {
        int count = 0;
        for(Direction d : Direction.values()) {
            if(isModerator(toCheck.relative(d))) {
                count++;
            }
        }
        return count;
    }

    private void countIrradiationConnections(BlockPos toCheck) {
        for(Direction d: Direction.values()) {
            if(isModerator(toCheck.relative(d))) {
                Block bs = getBlockState(toCheck.relative(d, 2)).getBlock();
                if(bs instanceof FissionFuelCellBlock) {
                    irradiationConnections++;
                }
            }
        }
    }

    private int countAdjuscentFuelCells(NCBlockPos toCheck, int step) {
        int count = 0;
        for (Direction d : Direction.values()) {
            if (isFuelCell(toCheck.revert().relative(d))) {
                count+=step;
                continue;
            }
            if(isModerator(toCheck.revert().relative(d))) {
                if(isFuelCell(toCheck.revert().relative(d, 2))) {
                    count += step;
                }
            }
        }
        return count;
    }

    public void invalidateStats()
    {
        controller().clearStats();
        moderators.clear();
        irradiators.clear();
        fuelCells.clear();
        heatSinks.clear();
        activeHeatSinks.clear();
        irradiationConnections = 0;
    }

    protected Direction getFacing() {
        return ((FissionControllerBE<?>)controller().controllerBE()).getFacing();
    }

    public double getHeatSinkCooling(boolean forceCheck) {
        if(refreshInnerCacheFlag || forceCheck) {
            heatSinkCooling = 0;
            for (HeatSinkBlock hs : activeHeatSinks().values()) {
                heatSinkCooling += hs.heat;
            }
        }
        return heatSinkCooling;
    }

}
