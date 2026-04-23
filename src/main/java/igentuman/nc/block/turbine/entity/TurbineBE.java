package igentuman.nc.block.turbine.entity;

import igentuman.api.nc.multiblock.MultiblockAttachable;
import igentuman.nc.block.entity.NuclearCraftBE;
import igentuman.nc.multiblock.AbstractMultiblock;
import igentuman.nc.multiblock.turbine.TurbineMultiblock;
import igentuman.nc.multiblock.turbine.TurbineRegistration;
import igentuman.nc.util.annotation.NBTField;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class TurbineBE extends NuclearCraftBE implements MultiblockAttachable {

    protected TurbineMultiblock multiblock;
    public static String NAME;
    public TurbineControllerBE controller;
    @NBTField
    public BlockPos controllerPos;
    public TurbineBE(BlockPos pPos, BlockState pBlockState, String name) {
        super(TurbineRegistration.TURBINE_BE.get(name).get(), pPos, pBlockState);
    }

    @Override
    public void setMultiblock(AbstractMultiblock multiblock) {
        if(this.multiblock == multiblock) {
            return;
        }
        this.multiblock = (TurbineMultiblock) multiblock;
        if (this.multiblock != null) {
            controllerPos = this.multiblock.controller().controllerBE().getBlockPos();
            controller = this.multiblock.controller().controllerBE();
            markDirty();
        }
    }

    @Override
    public TurbineControllerBE controller() {
        try {
            return getMultiblock().controller().controllerBE();
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
