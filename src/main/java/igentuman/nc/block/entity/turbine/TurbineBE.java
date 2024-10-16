package igentuman.nc.block.entity.turbine;

import igentuman.nc.block.entity.NuclearCraftBE;
import igentuman.nc.multiblock.AbstractNCMultiblock;
import igentuman.nc.multiblock.IMultiblockAttachable;
import igentuman.nc.multiblock.turbine.TurbineRegistration;
import igentuman.nc.multiblock.turbine.TurbineMultiblock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class TurbineBE extends NuclearCraftBE implements IMultiblockAttachable {

    @Override
    public void setMultiblock(AbstractNCMultiblock multiblock) {
        this.multiblock = (TurbineMultiblock) multiblock;
    }

    @Override
    public TurbineControllerBE<?> controller() {
        try {
            return (TurbineControllerBE<?>) multiblock().controller().controllerBE();
        } catch (NullPointerException ignore) {
            return null;
        }
    }

    public TurbineMultiblock multiblock() {
        return multiblock;
    }

    @Override
    public boolean canInvalidateCache() {
        return true;
    }

    protected TurbineMultiblock multiblock;

    public static String NAME;
    public boolean refreshCacheFlag = true;

    public byte validationRuns = 0;

    public TurbineControllerBE<?> controller;

    public TurbineBE(BlockPos pPos, BlockState pBlockState, String name) {
        super(TurbineRegistration.TURBINE_BE.get(name).get(), pPos, pBlockState);
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

    @Override
    public void setRemoved()
    {
        if(controller() != null) controller().invalidateCache();
        super.setRemoved();
    }

    public boolean isValidating = false;

    public void onNeighborChange(BlockState state, BlockPos pos, BlockPos neighbor) {
        if(multiblock() != null) {
            multiblock().onNeighborChange(state, pos, neighbor);
        }
    }
}
