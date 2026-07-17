package igentuman.nc.block.accelerator.entity;

import igentuman.nc.NuclearCraft;
import igentuman.nc.block.entity.MultiblockPortBE;
import igentuman.nc.multiblock.AbstractMultiblock;
import igentuman.nc.multiblock.accelerator.AbstractAcceleratorMultiblock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static igentuman.nc.NuclearCraft.currentTick;
import static igentuman.nc.multiblock.accelerator.AcceleratorRegistration.ACCELERATOR_BE;

public class AcceleratorIonSourcePortBE extends MultiblockPortBE {

    public static String NAME = "accelerator_ion_source_port";

    protected AbstractAcceleratorMultiblock multiblock;
    public LinearAcceleratorControllerBE controller;

    public AcceleratorIonSourcePortBE(BlockPos pPos, BlockState pBlockState) {
        super(ACCELERATOR_BE.get(NAME).get(), pPos, pBlockState);
    }

    @Override
    public void setMultiblock(AbstractMultiblock multiblock) {
        if(multiblock == this.multiblock) return;
        this.multiblock = (AbstractAcceleratorMultiblock) multiblock;
        if (this.multiblock != null) {
            controllerPos = this.multiblock.controller().controllerBE().getBlockPos();
            controller = (LinearAcceleratorControllerBE) this.multiblock.controller().controllerBE();
            markDirty();
        }
    }

    @Override
    public AbstractAcceleratorMultiblock getMultiblock() {
        return multiblock;
    }

    @Override
    public void tickServer() {
        if(lastTickTime == currentTick || NuclearCraft.instance.isNcBeStopped || isRemoved()) return;
        lastTickTime = currentTick;
        boolean updated = updateController();
        if(!isConnectedToController()) return;
        updated |= pushPull();
        updateIfNeeded(updated);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if(!isConnectedToController()) return super.getCapability(cap, side);
        return controller().getCapability(cap, side);
    }

    @Override
    public LinearAcceleratorControllerBE controller() {
        return (LinearAcceleratorControllerBE)super.controller();
    }
}
