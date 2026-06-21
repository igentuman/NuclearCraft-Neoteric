package igentuman.nc.block.heat_exchanger.entity;

import igentuman.nc.NuclearCraft;
import igentuman.nc.block.entity.MultiblockPortBE;
import igentuman.nc.block.entity.ParticleChamberPortBE;
import igentuman.nc.multiblock.AbstractMultiblock;
import igentuman.nc.multiblock.MultiblockHandler;
import igentuman.nc.multiblock.heat_exchanger.HeatExchangerMultiblock;
import igentuman.nc.multiblock.heat_exchanger.HeatExchangerRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.IFluidHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static igentuman.nc.NuclearCraft.currentTick;

public class HeatExchangerColdCoolantPortBE extends MultiblockPortBE {
    public static String NAME = "heat_exchanger_cold_coolant_port";
    protected HeatExchangerMultiblock multiblock;
    public HeatExchangerControllerBE controller;
    private LazyOptional<IFluidHandler> fluidCap = LazyOptional.empty();

    public HeatExchangerColdCoolantPortBE(BlockPos pPos, BlockState pBlockState) {
        super(HeatExchangerRegistration.HX_BE.get(NAME).get(), pPos, pBlockState);
    }

    @Override
    public void setMultiblock(AbstractMultiblock multiblock) {
        if (this.multiblock == multiblock) {
            return;
        }
        fluidCap.invalidate();
        fluidCap = LazyOptional.empty();
        this.multiblock = (HeatExchangerMultiblock) multiblock;
        if (this.multiblock != null) {
            controllerPos = this.multiblock.controller().controllerBE().getBlockPos();
            controller = this.multiblock.controller().controllerBE();
            markDirty();
        }
    }

    public HeatExchangerMultiblock getMultiblock() {
        return multiblock;
    }

    public void voidFluidSlot(int slotId) {
        if (isConnectedToController()) {
            controller().voidFluidSlot(slotId);
            setChanged();
        }
    }

    @Override
    public boolean canInvalidateCache() {
        return false;
    }

    @Override
    public HeatExchangerControllerBE controller() {
        return (HeatExchangerControllerBE) super.controller();
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (isConnectedToController() && cap == ForgeCapabilities.FLUID_HANDLER) {
            if (!fluidCap.isPresent()) {
                fluidCap = LazyOptional.of(() -> new HeatExchangerPortFluidHandler(
                        this::controller,
                        HeatExchangerControllerBE.TANK_COLD_IN,
                        HeatExchangerControllerBE.TANK_COLD_OUT,
                        false));
            }
            return fluidCap.cast();
        }
        return super.getCapability(cap, side);
    }

    public void tickServer() {
        if (NuclearCraft.instance.isNcBeStopped || isRemoved()) return;
        if (lastTickTime == currentTick) return;
        lastTickTime = currentTick;
        boolean wasConnected = connected;
        boolean updated = updateController() || needToUpdate || currentTick % 20 == 0;
        if (isConnectedToController() && currentTick % 10 == 0) {
            pushPull();
        }
        connected = isConnectedToController();
        updated |= wasConnected != connected;
        if (updated) {
            needToUpdate = false;
            if (connected) {
                MultiblockHandler.get(level.dimension()).addIgnoreToUpdate(getBlockPos());
            }
            setChanged();
            assert level != null;
            level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        fluidCap.invalidate();
    }
}
