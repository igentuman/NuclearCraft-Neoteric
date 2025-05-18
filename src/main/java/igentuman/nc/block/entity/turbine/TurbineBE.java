package igentuman.nc.block.entity.turbine;

import igentuman.nc.block.entity.NuclearCraftBE;
import igentuman.nc.multiblock.AbstractMultiblock;
import igentuman.api.nc.multiblock.MultiblockAttachable;
import igentuman.nc.multiblock.turbine.TurbineRegistration;
import igentuman.nc.multiblock.turbine.TurbineMultiblock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class TurbineBE extends NuclearCraftBE implements MultiblockAttachable {

    protected TurbineMultiblock multiblock;
    public static String NAME;
    public TurbineControllerBE controller;

    public TurbineBE(BlockPos pPos, BlockState pBlockState, String name) {
        super(TurbineRegistration.TURBINE_BE.get(name).get(), pPos, pBlockState);
    }

    @Override
    public void setMultiblock(AbstractMultiblock multiblock) {
        this.multiblock = (TurbineMultiblock) multiblock;
    }

    @Override
    public TurbineControllerBE controller() {
        try {
            return (TurbineControllerBE) getMultiblock().controller().controllerBE();
        } catch (NullPointerException ignore) {
            return null;
        }
    }

    public TurbineMultiblock getMultiblock() {
        return multiblock;
    }

    @Override
    public boolean canInvalidateCache() {
        return true;
    }

    @Override
    public void setRemoved()
    {
        if(controller() != null) controller().invalidateCache();
        super.setRemoved();
    }
}
