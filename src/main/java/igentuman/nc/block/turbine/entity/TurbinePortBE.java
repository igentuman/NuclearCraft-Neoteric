package igentuman.nc.block.turbine.entity;

import igentuman.nc.NuclearCraft;
import igentuman.nc.block.entity.MultiblockPortBE;
import igentuman.nc.handler.sided.capability.FluidCapabilityHandler;
import igentuman.nc.multiblock.AbstractMultiblock;
import igentuman.nc.multiblock.MultiblockHandler;
import igentuman.nc.multiblock.turbine.TurbineMultiblock;
import igentuman.nc.multiblock.turbine.TurbineRegistration;
import igentuman.nc.util.annotation.NBTField;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.templates.FluidTank;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

import static igentuman.nc.NuclearCraft.currentTick;
import static igentuman.nc.compat.gregtech.GTUtils.*;
import static igentuman.nc.util.ModUtil.isCcLoaded;
import static igentuman.nc.util.ModUtil.isGtLoaded;
import static net.minecraftforge.common.capabilities.ForgeCapabilities.ENERGY;

public class TurbinePortBE extends MultiblockPortBE {
    public static String NAME = "turbine_port";
    @NBTField
    public byte comparatorMode = SignalSource.OVERFLOW;
    protected TurbineMultiblock multiblock;
    public TurbineControllerBE controller;
    public TurbinePortBE(BlockPos pPos, BlockState pBlockState) {
        super(TurbineRegistration.TURBINE_BE.get(NAME).get(), pPos, pBlockState);
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


    public TurbineMultiblock getMultiblock() {
        return multiblock;
    }

    @Override
    public void tickServer() {
        if(lastTickTime == currentTick || NuclearCraft.instance.isNcBeStopped || isRemoved()) return;
        lastTickTime = currentTick;
        int wasSignal = analogSignal;
        boolean updated = updateController();
        if(isConnectedToController()) {
            sendOutPower();
        }

        if(hasRedstoneSignal()) {
            controller().controllerEnabled = true;
        }
        updateAnalogSignal();
        updated = wasSignal != analogSignal || updated;
        updated |= pushPull();
        updateIfNeeded(updated);
    }

    public void updateAnalogSignal() {
        if(!isConnectedToController()) {
            analogSignal = 0;
            return;
        }
        switch (comparatorMode) {
            case SignalSource.ENERGY:
                analogSignal = (byte) (controller().energyStorage().getEnergyStored() * 15 / controller().energyStorage().getMaxEnergyStored());
                break;
            case SignalSource.OVERFLOW:
                if (controller().getFlow() == 0) {
                    analogSignal = 0;
                    break;
                }
                analogSignal = (byte) (controller().realFlow * 15 / controller().getFlow());
                break;
        }
    }

    public void voidFluidSlot(int slotId) {
        if(isConnectedToController()) {
            controller().voidFluidSlot(slotId);
            setChanged();
        }
    }


    @Override
    public boolean canInvalidateCache() {
        return false;
    }

    @Override
    public TurbineControllerBE controller() {
        return (TurbineControllerBE) super.controller();
    }

    public void toggleRedstoneMode() {
        if(comparatorMode == SignalSource.ENERGY) {
            comparatorMode = SignalSource.OVERFLOW;
        } else {
            comparatorMode = SignalSource.ENERGY;
        }
        setChanged();
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
    }

    public static class SignalSource {
        public static final byte ENERGY = 0;
        public static final byte OVERFLOW = 2;
    }
}
