package igentuman.nc.block.entity.turbine;

import igentuman.nc.block.entity.NuclearCraftBE;
import igentuman.nc.block.entity.fission.FissionControllerBE;
import igentuman.nc.multiblock.AbstractMultiblock;
import igentuman.api.nc.multiblock.MultiblockAttachable;
import igentuman.nc.multiblock.turbine.TurbineRegistration;
import igentuman.nc.multiblock.turbine.TurbineMultiblock;
import igentuman.nc.util.annotation.NBTField;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
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
            controller = (TurbineControllerBE) this.multiblock.controller().controllerBE();
            setChanged();
            level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
        }
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
