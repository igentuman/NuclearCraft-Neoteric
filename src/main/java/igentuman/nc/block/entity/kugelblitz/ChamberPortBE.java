package igentuman.nc.block.entity.kugelblitz;

import igentuman.api.nc.multiblock.MultiblockAttachable;
import igentuman.nc.NuclearCraft;
import igentuman.nc.block.entity.NuclearCraftBE;
import igentuman.nc.handler.sided.capability.FluidCapabilityHandler;
import igentuman.nc.multiblock.AbstractMultiblock;
import igentuman.nc.multiblock.MultiblockHandler;
import igentuman.nc.multiblock.kugelblitz.KugelblitzMultiblock;
import igentuman.nc.util.annotation.NBTField;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import java.util.concurrent.atomic.AtomicInteger;

import static igentuman.nc.compat.oc2.FusionReactorDevice.DEVICE_CAPABILITY;
import static igentuman.nc.multiblock.kugelblitz.KugelblitzRegistration.KUGELBLITZ_BE;
import static igentuman.nc.util.ModUtil.isCcLoaded;
import static igentuman.nc.util.ModUtil.isOC2Loaded;

public class ChamberPortBE extends NuclearCraftBE implements MultiblockAttachable {

    public static String NAME = "chamber_port";
    @NBTField
    public byte analogSignal = 0;
    @NBTField
    public byte comparatorMode = SignalSource.ENERGY;
    @NBTField
    public BlockPos controllerPos;
    protected KugelblitzMultiblock multiblock;
    public boolean refreshCacheFlag = true;
    public byte validationRuns = 0;
    public ChamberTerminalBE controller;

    public ChamberPortBE(BlockPos pPos, BlockState pBlockState) {
        super(KUGELBLITZ_BE.get(NAME).get(), pPos, pBlockState);
    }
    public Direction getFacing() {
        return getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
    }

    public boolean hasRedstoneSignal() {
        return Objects.requireNonNull(getLevel()).hasNeighborSignal(worldPosition);
    }

    @Override
    public void setMultiblock(AbstractMultiblock multiblock) {
        this.multiblock = (KugelblitzMultiblock) multiblock;
    }

    @Override
    public KugelblitzMultiblock getMultiblock() {
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
        boolean updated = sendOutPower();
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
            case SignalSource.FREQUENCY -> controller().frequency = analogSignal;
            case SignalSource.TRANSFORMATION_ENERGY_RATE -> controller().energyConvertionRate = analogSignal/15*100;
        }
        Direction dir = getFacing();

        if(fluidHandler() != null) {
            //updated = fluidHandler().pushFluids(dir, false, worldPosition) || updated;
            updated = fluidHandler().pullFluids(dir, false, worldPosition) || updated;
        }

        if(updated) {
            MultiblockHandler.get(level.dimension()).addIgnoreToUpdate(getBlockPos());
            setChanged();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
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
            case SignalSource.MASS:
                analogSignal = (byte) (controller().mass * 15 / BlackHoleBE.MAX_MASS);
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
        return controller().contentHandler().fluidCapability;
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

    protected boolean sendOutPower() {
        if(getMultiblock() == null) return false;
        AtomicInteger capacity = new AtomicInteger(controller().energyStorage().getEnergyStored());
        if (capacity.get() > 0) {
            for (Direction direction : Direction.values()) {
                BlockEntity be = getLevel().getExistingBlockEntity(worldPosition.relative(direction));
                if (be != null) {
                    boolean doContinue = be.getCapability(ForgeCapabilities.ENERGY, direction.getOpposite()).map(handler -> {
                                if (handler.canReceive()) {
                                    int received = handler.receiveEnergy(Math.min(capacity.get(), controller().energyStorage().getMaxEnergyStored()), false);
                                    capacity.addAndGet(-received);
                                    controller().energyStorage().consumeEnergy(received);
                                    setChanged();
                                    return capacity.get() > 0;
                                } else {
                                    return true;
                                }
                            }
                    ).orElse(true);
                    if (!doContinue) {
                        return true;
                    }
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public ChamberTerminalBE controller() {
        if(NuclearCraft.instance.isNcBeStopped || (!getLevel().isClientSide() && getLevel().getServer() != null && !getLevel().getServer().isRunning())) return null;
        if(getLevel().isClientSide && controllerPos != null) {
            return (ChamberTerminalBE) getLevel().getExistingBlockEntity(controllerPos);
        }
        try {
            return (ChamberTerminalBE) getMultiblock().controller().controllerBE();
        } catch (NullPointerException e) {
            if(controllerPos != null) {
                return (ChamberTerminalBE) getLevel().getExistingBlockEntity(controllerPos);
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
        return controller().energyPerTick;
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


    public static class SignalSource {
        public static final byte ENERGY = 1;
        public static final byte MASS = 2;
        public static final byte PROGRESS = 3;
        public static final byte ITEMS = 4;
        public static final byte FREQUENCY = 5;
        public static final byte TRANSFORMATION_ENERGY_RATE = 6;
    }
}
