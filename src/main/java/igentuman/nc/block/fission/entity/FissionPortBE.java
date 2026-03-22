package igentuman.nc.block.fission.entity;

import igentuman.nc.NuclearCraft;
import igentuman.nc.block.MultiblockPortBE;
import igentuman.nc.handler.sided.capability.FluidCapabilityHandler;
import igentuman.nc.handler.sided.capability.ItemCapabilityHandler;
import igentuman.nc.multiblock.AbstractMultiblock;
import igentuman.nc.multiblock.MultiblockHandler;
import igentuman.nc.multiblock.fission.FissionReactorMultiblock;
import igentuman.nc.multiblock.fission.FissionReactorRegistration;
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

import static igentuman.nc.NuclearCraft.currentTick;
import static igentuman.nc.compat.gregtech.GTUtils.*;
import static igentuman.nc.compat.oc2.FissionReactorDevice.DEVICE_CAPABILITY;
import static igentuman.nc.util.ModUtil.*;
import static net.minecraftforge.common.capabilities.ForgeCapabilities.ENERGY;

public class FissionPortBE extends MultiblockPortBE {

    public static final String NAME = "fission_reactor_port";

    @NBTField
    public byte analogSignal = 0;
    @NBTField
    public byte redstoneMode = SignalSource.HEAT;
    @NBTField
    public BlockPos controllerPos;
    public boolean isSteamMode = false;
    @NBTField
    public boolean connected = false;
    protected long lastTickTime = 0;
    protected FissionReactorMultiblock multiblock;
    protected FissionControllerBE controller;

    public FissionPortBE(BlockPos pPos, BlockState pBlockState) {
        super(FissionReactorRegistration.FISSION_BE.get(NAME).get(), pPos, pBlockState);
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

    private boolean updateController() {
        boolean result = false;
        if (controller != controller()) {
            controller = controller();
            controllerPos = BlockPos.ZERO;
            result = true;
        }
        if (controller != null && !controller.getBlockPos().equals(controllerPos)) {
            controllerPos = new BlockPos(controller.getBlockPos());
            result = true;
        }
        if (controller() != null && isSteamMode != controller().isSteamMode) {
            isSteamMode = controller().isSteamMode;
            result = true;
        }
        return result;
    }

    public void tickServer() {
        if (NuclearCraft.instance.isNcBeStopped || isRemoved()) return;

        int wasSignal = analogSignal;
        boolean wasConnected = connected;
        //Disallow boosters like torcherino
        if(lastTickTime == level.getGameTime()) {
            return;
        }
        lastTickTime = level.getGameTime();
        if(getMultiblock() != null && controller() != null) {
            sendOutPower();
        }

        boolean updated = updateController();
        if(currentTick % 20 == 0 && controller() != null) {
            pushPull();
        }
        if (currentTick % 10 == 0 && controller() != null) {
            updateAnalogSignal();

            updated = wasSignal != analogSignal || updated;
            switch (redstoneMode) {
                case SignalSource.SWITCH -> controller().toggleReactor(analogSignal > 0);
                case SignalSource.MODERATOR -> controller().adjustModerator(analogSignal);
            }
        }
        //if no recipe - tick only 5 times per second
        if(currentTick % 5 == 0 && controller() != null && !controller().hasRecipe()) {
            return;
        }
        connected = getMultiblock() != null && getMultiblock().isFormed();
        if (updated || wasConnected != connected || needToUpdate) {
            needToUpdate = false;
            if(connected) {
                MultiblockHandler.get(level.dimension()).addIgnoreToUpdate(getBlockPos());
            }
            setChanged();
            level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    public boolean pushPull() {
        boolean pushed = false;
        if (itemHandler() != null) {
            Direction dir = getFacing();
            pushed = itemHandler().pushItems(dir, true, worldPosition);
            pushed = itemHandler().pullItems(dir, true, worldPosition) || pushed;
        }
        return pushed;
    }

    private void updateAnalogSignal() {
        switch (redstoneMode) {
            case SignalSource.ENERGY:
                analogSignal = (byte) (controller().energyStorage().getEnergyStored() * 15 / controller().energyStorage().getMaxEnergyStored());
                break;
            case SignalSource.HEAT:
                analogSignal = (byte) (getHeatStored() * 15 / getMaxHeat());
                break;
            case SignalSource.PROGRESS:
                analogSignal = (byte) (controller().recipeInfo().ticksProcessed * 15 / controller().recipeInfo().ticks);
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

    public double getHeatStored() {
        if(controller() == null) return 0;
        return controller().heat;
    }

    public double getMaxHeat() {
        if(controller() == null) return 0;
        return controller().maxHeat;
    }

    protected void transferEnergyToSide(Direction direction) {
        if (getEnergyStored() <= 0) {
            return; // No energy to transfer
        }
        int wasEnergy = getEnergyStored();
        BlockEntity be = level.getExistingBlockEntity(worldPosition.relative(direction));
        if (be == null || be instanceof FissionPortBE || be instanceof FissionControllerBE) {
            return;
        }
        if((isGtLoaded() && isGTEUCapEnabled())) {
            transferEU(controller(), be, controller().energyStorage(), direction);
        }
        if(isGtLoaded() && isOnlyGTCEUCapEnabled()) {
            return;
        }
        if(wasEnergy - getEnergyStored() >= controller().energyStorage().getMaxExtract()) {
            return;
        }
        int extracted = wasEnergy - controller().energyStorage().getEnergyStored();
        if(extracted >= controller().energyStorage().getMaxExtract()) {
            return;
        }
        int canExtract = Math.min(controller().energyStorage().getMaxExtract() - extracted, getEnergyStored());
        be.getCapability(ForgeCapabilities.ENERGY, direction.getOpposite()).map(handler -> {
                    if (handler.canReceive()) {
                        int received = handler.receiveEnergy(canExtract, false);
                        controller().energyStorage().consumeEnergy(received);
                        controller().setChanged();
                        return getEnergyStored() > 0;
                    } else {
                        return true;
                    }
                }
        );
    }

    protected ItemCapabilityHandler itemHandler()
    {
        return controller().contentHandler().itemHandler;
    }

    protected FluidCapabilityHandler fluidHandler()
    {
        return controller().contentHandler().fluidHandler;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (controller() == null) return super.getCapability(cap, side);
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return controller().getCapability(cap, side);
        }
        if (cap == ForgeCapabilities.FLUID_HANDLER && controller().canAcceptFluid()) {
            return controller().getCapability(cap, side);
        }
        if(isGtLoaded()) {
            if (cap == com.gregtechceu.gtceu.api.capability.forge.GTCapability.CAPABILITY_ENERGY_CONTAINER) {
                if (isGTEUCapEnabled() && !isSteamMode) {
                    return getGTEnergy(controller(), side).cast();
                }
            }
        }
        if (cap == ENERGY && !isSteamMode) {
            if(!isOnlyGTCEUCapEnabled()) {
                return controller().getEnergy().cast();
            } else {
                return LazyOptional.empty();
            }
        }

        if (isMekanismLoaded() && isSteamMode) {
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

    @Override
    public boolean canInvalidateCache() {
        return true;
    }

    @Override
    public void setMultiblock(AbstractMultiblock multiblock) {
        if(this.multiblock == multiblock) {
            return;
        }
        this.multiblock = (FissionReactorMultiblock) multiblock;
        if (this.multiblock != null) {
            controllerPos = this.multiblock.controller().controllerBE().getBlockPos();
            controller = (FissionControllerBE) this.multiblock.controller().controllerBE();
            MultiblockHandler.get(level.dimension()).addIgnoreToUpdate(getBlockPos());
            markDirty();
        }
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
        MultiblockHandler.get(level.dimension()).addIgnoreToUpdate(getBlockPos());
        setChanged();
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
    }

    public FluidTank getFluidTank(int i) {
        if (controller() == null) return null;
        return controller().getFluidTank(i);
    }

    public boolean isBoilingMode() {
        if (controller() == null) return false;
        return controller().isSteamMode;
    }

    public int getSteamPerTick() {
        if (controller() == null) return 0;
        return controller().steamPerTick;
    }

    public int getFuelCount() {
        if(controller() == null) return 0;
        return itemHandler().getStackInSlot(0).getCount();
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
