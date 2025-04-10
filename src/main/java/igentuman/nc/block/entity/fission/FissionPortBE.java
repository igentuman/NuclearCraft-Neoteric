package igentuman.nc.block.entity.fission;

import igentuman.api.nc.multiblock.MultiblockAttachable;
import igentuman.nc.NuclearCraft;
import igentuman.nc.block.entity.NuclearCraftBE;
import igentuman.nc.handler.sided.capability.FluidCapabilityHandler;
import igentuman.nc.handler.sided.capability.ItemCapabilityHandler;
import igentuman.nc.multiblock.AbstractNCMultiblock;
import igentuman.nc.multiblock.MultiblockHandler;
import igentuman.nc.multiblock.fission.FissionReactor;
import igentuman.nc.multiblock.fission.FissionReactorMultiblock;
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

import static igentuman.nc.compat.oc2.NCFissionReactorDevice.DEVICE_CAPABILITY;
import static igentuman.nc.util.ModUtil.*;

public class FissionPortBE extends NuclearCraftBE implements MultiblockAttachable {

    public static String NAME = "fission_reactor_port";
    @NBTField
    public byte analogSignal = 0;
    @NBTField
    public byte redstoneMode = SignalSource.HEAT;
    @NBTField
    public BlockPos controllerPos;
    @NBTField
    public boolean isSteamMode = false;
    protected FissionReactorMultiblock multiblock;
    protected FissionControllerBE controller;

    public FissionPortBE(BlockPos pPos, BlockState pBlockState) {
        super(FissionReactor.FISSION_BE.get(NAME).get(), pPos, pBlockState);
    }
    public Direction getFacing() {
        return getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
    }

    public boolean hasRedstoneSignal() {
        return Objects.requireNonNull(getLevel()).hasNeighborSignal(worldPosition);
    }

    public int getRedstoneSignal() {
        return Objects.requireNonNull(getLevel()).getBestNeighborSignal(worldPosition);
    }

    public FissionReactorMultiblock getMultiblock() {
        return multiblock;
    }

    public void tickServer() {
        if (NuclearCraft.instance.isNcBeStopped) return;
        if (getMultiblock() == null || controller() == null) return;
        int wasSignal = analogSignal;
        boolean updated = sendOutPower();
        if (controller != controller()) {
            controller = controller();
            controllerPos = BlockPos.ZERO;
            if (controller != null) {
                controllerPos = controller.getBlockPos();
            }
            updated = true;
        }
        if (isSteamMode != controller().isSteamMode) {
            isSteamMode = controller().isSteamMode;
            updated = true;
        }

        if (level.getGameTime() % 10 == 0) {
            updateAnalogSignal();
            if (itemHandler() != null) {
                Direction dir = getFacing();
                itemHandler().pushItems(dir, true, worldPosition);
                itemHandler().pullItems(dir, true, worldPosition);
            }
            updated = wasSignal != analogSignal || updated;
        }
        switch (redstoneMode) {
            case SignalSource.SWITCH -> controller().toggleReactor(analogSignal > 0);
            case SignalSource.MODERATOR -> controller().adjustModerator(analogSignal);
        }

        if (updated) {
            MultiblockHandler.addIgnoreToUpdate(getBlockPos());
            setChanged();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_NEIGHBORS);
        }
    }

    private void updateAnalogSignal() {
        switch (redstoneMode) {
            case SignalSource.ENERGY:
                analogSignal = (byte) (controller().energyStorage.getEnergyStored() * 15 / controller().energyStorage.getMaxEnergyStored());
                break;
            case SignalSource.HEAT:
                analogSignal = (byte) (controller().heat * 15 / controller().getMaxHeat());
                break;
            case SignalSource.PROGRESS:
                analogSignal = (byte) (controller().recipeInfo.ticksProcessed * 15 / controller().recipeInfo.ticks);
                break;
            case SignalSource.ITEMS:
                analogSignal = (byte) (itemHandler().getStackInSlot(0).getCount() * 15 / itemHandler().getStackInSlot(0).getMaxStackSize());
                break;
            case SignalSource.MODERATOR:
                analogSignal = (byte) (Math.max(1, getRedstoneSignal()));
            case SignalSource.SWITCH:
                analogSignal = (byte) (Math.max(0, getRedstoneSignal()));
                break;
        }
    }

    protected ItemCapabilityHandler itemHandler()
    {
        return controller().contentHandler().itemHandler;
    }

    protected FluidCapabilityHandler fluidHandler()
    {
        return controller().contentHandler().fluidCapability;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (controller() == null) return super.getCapability(cap, side);
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return controller().contentHandler().itemCapability.cast();
        }
        if (cap == ForgeCapabilities.FLUID_HANDLER && controller().canAcceptFluid()) {
            return controller().getCapability(cap, side);
        }
        if (cap == ForgeCapabilities.ENERGY && !controller().isSteamMode) {
            return controller().getEnergy().cast();
        }

        if (isMekanismLoadeed() && isSteamMode) {
            if (cap == mekanism.common.capabilities.Capabilities.GAS_HANDLER) {
                if (controller().contentHandler().hasFluidCapability(side)) {
                    return LazyOptional.of(() -> controller().contentHandler().gasConverter(side));
                }
                return LazyOptional.empty();
            }
            if (cap == mekanism.common.capabilities.Capabilities.SLURRY_HANDLER) {
                if (controller().contentHandler().hasFluidCapability(side)) {
                    return LazyOptional.of(() -> controller().contentHandler().getSlurryConverter(side));
                }
                return LazyOptional.empty();
            }
        }

        if (isOC2Loaded()) {
            if (cap == DEVICE_CAPABILITY) {
                return controller().getOCDevice(cap, side);
            }
        }

        if (isCcLoaded()) {
            if (cap == dan200.computercraft.shared.Capabilities.CAPABILITY_PERIPHERAL) {
                return controller().getPeripheral(cap, side);
            }
        }
        return super.getCapability(cap, side);
    }

    protected boolean sendOutPower() {
        if (getMultiblock() == null) return false;
        AtomicInteger capacity = new AtomicInteger(controller().energyStorage().getEnergyStored());
        if (capacity.get() > 0) {
            for (Direction direction : Direction.values()) {
                BlockEntity be = getLevel().getExistingBlockEntity(worldPosition.relative(direction));
                if (be == null) {
                    continue;
                }
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
            return true;
        }
        return false;
    }

    @Override
    public boolean canInvalidateCache() {
        return false;
    }

    @Override
    public void setMultiblock(AbstractNCMultiblock multiblock) {
        this.multiblock = (FissionReactorMultiblock) multiblock;
    }

    @Override
    public FissionControllerBE controller() {
        if (NuclearCraft.instance.isNcBeStopped || (!getLevel().isClientSide() && getLevel().getServer() != null && !getLevel().getServer().isRunning())) return null;
        if (controller == null && getLevel().isClientSide && controllerPos != null) {
            BlockEntity be = getLevel().getExistingBlockEntity(controllerPos);
            if (be instanceof FissionControllerBE controllerBe) {
                controller = controllerBe;
                return  controller;
            }
        }
        try {
            BlockEntity be = getMultiblock().controller().controllerBE();
            if (be instanceof FissionControllerBE controllerBe) {
                controller = controllerBe;
                return controller;
            }
        } catch (NullPointerException e) {
            if (controllerPos != null) {
                BlockEntity be = getLevel().getExistingBlockEntity(controllerPos);
                if (be instanceof FissionControllerBE controllerBe) {
                    controller = controllerBe;
                }
            }
        }
        return controller;
    }

    public int getEnergyStored() {
        if (controller() == null) return 0;
        return controller().energyStorage().getEnergyStored();
    }

    public double getDepletionProgress() {
        if (controller() == null) return 0;
        return controller().getDepletionProgress();
    }

    public int getMaxEnergyStored() {
        if (controller() == null) return 0;
        return controller().energyStorage().getMaxEnergyStored();
    }

    public int energyPerTick() {
        if (controller() == null) return 0;
        return controller().energyPerTick;
    }

    public void toggleRedstoneMode() {
        redstoneMode++;
        if (redstoneMode > SignalSource.MODERATOR) {
            redstoneMode = SignalSource.ENERGY;
        }
        analogSignal = 0;
        setChanged();
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
    }

    public FluidTank getFluidTank(int i) {
        if (controller() == null) return null;
        return controller().getFluidTank(i);
    }

    public boolean getMode() {
        if (controller() == null) return false;
        return controller().isSteamMode;
    }

    public int getSteamPerTick() {
        if (controller() == null) return 0;
        return controller().steamPerTick;
    }

    public static class SignalSource {
        public static final byte ENERGY = 1;
        public static final byte HEAT = 2;
        public static final byte PROGRESS = 3;
        public static final byte ITEMS = 4;
        public static final byte SWITCH = 5;
        public static final byte MODERATOR = 6;
    }
}
