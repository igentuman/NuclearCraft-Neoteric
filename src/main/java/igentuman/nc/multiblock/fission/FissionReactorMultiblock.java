package igentuman.nc.multiblock.fission;

import igentuman.nc.block.entity.fission.*;
import igentuman.nc.multiblock.AbstractNCMultiblock;
import igentuman.nc.util.NCBlockPos;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.*;

import static igentuman.nc.handler.config.CommonConfig.FISSION_CONFIG;
import static igentuman.nc.util.TagUtil.getBlocksByTagKey;

public class FissionReactorMultiblock extends AbstractNCMultiblock {

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


    public HashMap<BlockPos, FissionHeatSinkBE> activeHeatSinks = new HashMap<>();
    private List<BlockPos> moderators = new ArrayList<>();
    public List<BlockPos> heatSinks = new ArrayList<>();
    public List<BlockPos> fuelCells = new ArrayList<>();
    private double heatSinkCooling = 0;

    public FissionReactorMultiblock(FissionControllerBE<?> fissionControllerBE) {
        super(
                getBlocksByTagKey(FissionBlocks.CASING_BLOCKS.location().toString()),
                getBlocksByTagKey(FissionBlocks.INNER_REACTOR_BLOCKS.location().toString())
        );
        controller = new FissionReactorController(fissionControllerBE);
    }

    public Map<BlockPos, FissionHeatSinkBE> activeHeatSinks() {
        if(activeHeatSinks.isEmpty()) {
            for(BlockPos hpos: heatSinks) {
                BlockEntity be = getLevel().getBlockEntity(hpos);
                if(be instanceof FissionHeatSinkBE) {
                    FissionHeatSinkBE hs = (FissionHeatSinkBE) be;
                    if(hs.isValid(true)) {
                        activeHeatSinks.put(hpos, hs);
                    }
                }
            }
        }
        ((FissionControllerBE<?>)controller().controllerBE()).heatSinksCount = activeHeatSinks.size();
        return activeHeatSinks;
    }

    public static boolean isModerator(BlockPos pos, Level world) {
        return  world.getBlockEntity(pos) instanceof FissionModeratorBE;
    }

    protected boolean isHeatSink(BlockPos pos) {
        return getLevel().getBlockEntity(pos) instanceof FissionHeatSinkBE;
    }

    protected boolean isFuelCell(BlockPos pos) {
        return getLevel().getBlockEntity(pos) instanceof FissionFuelCellBE;
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
        controller.updateEnergyStorage();
        controller.moderatorsCount = moderators.size();
    }

    @Override
    protected boolean validateInnerBlock(BlockPos toCheck) {
        if(isFuelCell(toCheck)) {
            BlockEntity be = getLevel().getBlockEntity(toCheck);
            if(be instanceof FissionFuelCellBE) {
                fuelCells.add(toCheck);
                int moderatorAttachments = ((FissionFuelCellBE) be).getAttachedModeratorsCount(true);
                ((FissionControllerBE<?>)controller().controllerBE()).fuelCellMultiplier += countAdjuscentFuelCells((NCBlockPos) toCheck, 3);
                ((FissionControllerBE<?>)controller().controllerBE()).moderatorCellMultiplier += (countAdjuscentFuelCells((NCBlockPos) toCheck, 1)+1)*moderatorAttachments;
                ((FissionControllerBE<?>)controller().controllerBE()).moderatorAttacmentsCount += moderatorAttachments;
            }
        }
        if(isModerator(toCheck, getLevel())) {
            if(isAttachedToFuelCell(toCheck)) {
                moderators.add(toCheck);
            }
        }
        if(isHeatSink(toCheck)) {
            heatSinks.add(toCheck);
        }
        return true;
    }

    private int countAdjuscentFuelCells(NCBlockPos toCheck, int step) {
        int count = 0;
        for (Direction d : Direction.values()) {
            if (isFuelCell(toCheck.revert().relative(d))) {
                count+=step;
            }
        }
        return count;
    }

    public void invalidateStats()
    {
        controller().clearStats();
        moderators.clear();
        fuelCells.clear();
        heatSinks.clear();
        activeHeatSinks.clear();
    }

    protected Direction getFacing() {
        return ((FissionControllerBE<?>)controller().controllerBE()).getFacing();
    }

    public double getHeatSinkCooling(boolean forceCheck) {
        if(refreshInnerCacheFlag || forceCheck) {
            heatSinkCooling = 0;
            for (FissionHeatSinkBE hs : activeHeatSinks().values()) {
                heatSinkCooling += hs.getHeat();
            }
        }
        return heatSinkCooling;
    }

}
