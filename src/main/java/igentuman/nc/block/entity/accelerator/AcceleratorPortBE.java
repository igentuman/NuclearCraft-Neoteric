package igentuman.nc.block.entity.accelerator;

import igentuman.api.nc.multiblock.MultiblockAttachable;
import igentuman.nc.NuclearCraft;
import igentuman.nc.block.entity.NuclearCraftBE;
import igentuman.nc.handler.config.CommonConfig;
import igentuman.nc.handler.sided.capability.FluidCapabilityHandler;
import igentuman.nc.multiblock.AbstractMultiblock;
import igentuman.nc.multiblock.MultiblockHandler;
import igentuman.nc.multiblock.accelerator.AbstractAcceleratorMultiblock;
import igentuman.nc.util.annotation.NBTField;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.templates.FluidTank;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

import static igentuman.nc.compat.oc2.FusionReactorDevice.DEVICE_CAPABILITY;
import static igentuman.nc.content.particles.CapabilityParticleStackHandler.PARTICLE_HANDLER_CAPABILITY;
import static igentuman.nc.handler.config.CommonConfig.GTCEU_CONFIG;
import static igentuman.nc.multiblock.accelerator.AcceleratorRegistration.ACCELERATOR_BE;
import static igentuman.nc.util.ModUtil.isCcLoaded;
import static igentuman.nc.util.ModUtil.isOC2Loaded;

public class AcceleratorPortBE extends NuclearCraftBE implements MultiblockAttachable {

    public static String NAME = "accelerator_port";
    @NBTField
    public byte analogSignal = 0;
    @NBTField
    public byte comparatorMode = SignalSource.ENERGY;
    @NBTField
    public BlockPos controllerPos;
    protected AbstractAcceleratorMultiblock multiblock;
    public boolean refreshCacheFlag = true;
    public byte validationRuns = 0;
    public LinearAcceleratorControllerBE controller;

    public AcceleratorPortBE(BlockPos pPos, BlockState pBlockState) {
        super(ACCELERATOR_BE.get(NAME).get(), pPos, pBlockState);
    }
    public Direction getFacing() {
        return getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
    }

    public boolean hasRedstoneSignal() {
        return Objects.requireNonNull(getLevel()).hasNeighborSignal(worldPosition);
    }

    @Override
    public void setMultiblock(AbstractMultiblock multiblock) {
        this.multiblock = (AbstractAcceleratorMultiblock) multiblock;
    }

    @Override
    public AbstractAcceleratorMultiblock getMultiblock() {
        return multiblock;
    }

    @Override
    public boolean canInvalidateCache() {
        return true;
    }

    @Override
    public void tickServer() {
        if(NuclearCraft.instance.isNcBeStopped || isRemoved()) return;
        super.tickServer();
        if(getMultiblock() == null || controller() == null) return;
        int wasSignal = analogSignal;
        boolean updated = false;
        sendOutPower();
        if(controllerPos == null) {
            controllerPos = controller().getBlockPos();
            updated = true;
            setChanged();
        }
        if(hasRedstoneSignal()) {
            controller().controllerEnabled = true;
        }

        updateAnalogSignal();

        updated = wasSignal != analogSignal || updated;
        switch (comparatorMode) {
            case SignalSource.FREQUENCY -> controller().heatRate = analogSignal;
            case SignalSource.TRANSFORMATION_ENERGY_RATE -> controller().heatRate = analogSignal/15*100;
        }
        Direction dir = getFacing();

        if(fluidHandler() != null) {
            updated = fluidHandler().pullFluids(dir, false, worldPosition) || updated;
        }

        if(updated) {
            MultiblockHandler.get(level.dimension()).addIgnoreToUpdate(getBlockPos());
            setChanged();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    protected void sendOutPower() {

    }


    private void updateAnalogSignal() {
        if(controller() == null) {
            analogSignal = 0;
            return;
        }
        switch (comparatorMode) {
            case SignalSource.ENERGY:
                analogSignal = (byte) (controller().energyStorage().getEnergyStored() * 15 / controller().energyStorage().getMaxEnergyStored());
                break;
            case SignalSource.PROGRESS:
                analogSignal = (byte) (getProgress()/100D * 15);
                break;
            case SignalSource.ITEMS:
                analogSignal = (byte) (controller().contentHandler().itemHandler.getStackInSlot(0).getCount() * 15 / controller().contentHandler().itemHandler.getStackInSlot(0).getMaxStackSize());
                break;
            case SignalSource.FREQUENCY:
                analogSignal = (byte) (Math.max(1, getRedstoneSignal()));
                break;
            case SignalSource.TRANSFORMATION_ENERGY_RATE:
                analogSignal = (byte) (Math.max(1, getRedstoneSignal()));
                break;
        }
    }

    public int getRedstoneSignal() {
        return Objects.requireNonNull(getLevel()).getBestNeighborSignal(worldPosition);
    }

    protected FluidCapabilityHandler fluidHandler()
    {
        return controller().contentHandler().fluidHandler;
    }

    protected <T> LazyOptional<T> fluidHandler(@Nullable Direction side)
    {
        return controller().contentHandler().getFluidCapability(side);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if(controller() == null) return super.getCapability(cap, side);

        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return controller().getCapability(cap, side);
        }
        if (cap == ForgeCapabilities.FLUID_HANDLER) {
            return controller().getCapability(cap, side);
        }
        if (cap == ForgeCapabilities.ENERGY) {
            return  controller().getCapability(cap, side);
        }
        if(isCcLoaded()) {
            if(cap == dan200.computercraft.shared.Capabilities.CAPABILITY_PERIPHERAL) {
                return controller().getPeripheral(cap, side);
            }
        }

        if(isOC2Loaded()) {
            if(cap == DEVICE_CAPABILITY) {
                return controller().getOCDevice(cap, side);
            }
        }
        return super.getCapability(cap, side);
    }

    @Override
    public LinearAcceleratorControllerBE controller() {
        if(NuclearCraft.instance.isNcBeStopped || (!getLevel().isClientSide() && getLevel().getServer() != null && !getLevel().getServer().isRunning())) return null;
        if(getLevel().isClientSide && controllerPos != null) {
            return (LinearAcceleratorControllerBE) getLevel().getExistingBlockEntity(controllerPos);
        }
        try {
            return (LinearAcceleratorControllerBE) getMultiblock().controller().controllerBE();
        } catch (NullPointerException e) {
            if(controllerPos != null) {
                return (LinearAcceleratorControllerBE) getLevel().getExistingBlockEntity(controllerPos);
            }
            return null;
        }
    }

    public int getEnergyStored() {
        if(controller() == null) return 0;
        return controller().energyStorage().getEnergyStored();
    }

    public int getMaxEnergyStored() {
        if(controller() == null) return 0;
        return controller().energyStorage().getMaxEnergyStored();
    }

    public int energyPerTick() {
        if(controller() == null) return 0;
        return controller().heatRate;
    }

    public void toggleComparatorMode() {
        comparatorMode++;
        if(comparatorMode > SignalSource.TRANSFORMATION_ENERGY_RATE) {
            comparatorMode = SignalSource.ENERGY;
        }
        MultiblockHandler.get(level.dimension()).addIgnoreToUpdate(getBlockPos());
        setChanged();
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
    }

    public FluidTank getFluidTank(int i) {
        if(controller() == null) return null;
        return controller().getFluidTank(i);
    }

    public double getProgress() {
        if(controller() == null) return 0;
        return controller().recipeInfo().getProgress();
    }

    public int getEnergyRequired() {
        if(controller() == null) return 0;
        return controller().energyRequired;
    }

    public CommonConfig.GTCEUCompatibilityConfig.GTCEUTier getTier() {
        if(controller() == null) return GTCEU_CONFIG.ACCELERATORS_ENERGY_TIER.get();
        return controller().getTier();
    }


    public static class SignalSource {
        public static final byte ENERGY = 1;
        public static final byte MASS = 2;
        public static final byte PROGRESS = 3;
        public static final byte ITEMS = 4;
        public static final byte FREQUENCY = 5;
        public static final byte TRANSFORMATION_ENERGY_RATE = 6;
    }
}
