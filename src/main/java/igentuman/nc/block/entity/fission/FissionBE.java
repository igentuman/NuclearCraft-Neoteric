package igentuman.nc.block.entity.fission;

import igentuman.nc.block.entity.NuclearCraftBE;
import igentuman.nc.multiblock.fission.FissionReactorMultiblock;
import igentuman.nc.setup.multiblocks.FissionReactor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class FissionBE extends NuclearCraftBE {
    public FissionReactorMultiblock multiblock() {
        return multiblock;
    }

    public void setMultiblock(FissionReactorMultiblock multiblock) {
        this.multiblock = multiblock;
    }

    protected FissionReactorMultiblock multiblock;

    public static String NAME;
    public boolean refreshCacheFlag = true;
    public boolean attachedToFuelCell = false;

    public byte validationRuns = 0;

    public FissionControllerBE controller;

    public FissionBE(BlockPos pPos, BlockState pBlockState, String name) {
        super(FissionReactor.MULTIBLOCK_BE.get(name).get(), pPos, pBlockState);
    }

    public void invalidateCache()
    {
        refreshCacheFlag = true;
        validationRuns = 0;
    }

    public void tickClient() {
    }

    public void tickServer() {
    }

    public boolean isAttachedToFuelCell() {
        if(refreshCacheFlag || validationRuns < 2) {
            validationRuns++;
            for (Direction dir : Direction.values()) {
                BlockEntity be = getLevel().getBlockEntity(getBlockPos().relative(dir));
                if (be instanceof FissionFuelCellBE) {
                    attachedToFuelCell = true;
                    break;
                }
                if(be instanceof FissionBE) {
                    attachedToFuelCell = ((FissionBE) be).attachedToFuelCell || attachedToFuelCell;
                }
            }
        }
        return attachedToFuelCell;
    }

    public void onNeighborChange(BlockState state, BlockPos pos, BlockPos neighbor) {
        if(multiblock() != null) {
            multiblock().onNeighborChange(state, pos, neighbor);
        }
    }
}
