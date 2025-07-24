package igentuman.nc.block.target_chamber.entity;

import igentuman.api.nc.multiblock.MultiblockAttachable;
import igentuman.nc.NuclearCraft;
import igentuman.nc.block.entity.NuclearCraftBE;
import igentuman.nc.handler.sided.capability.FluidCapabilityHandler;
import igentuman.nc.handler.sided.capability.ItemCapabilityHandler;
import igentuman.nc.multiblock.AbstractMultiblock;
import igentuman.nc.multiblock.MultiblockHandler;
import igentuman.nc.multiblock.accelerator.TargetChamberMultiblock;
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

import static igentuman.nc.compat.gregtech.GTUtils.getGTEnergy;
import static igentuman.nc.compat.gregtech.GTUtils.isOnlyGTCEUCapEnabled;
import static igentuman.nc.compat.oc2.TargetChamberDevice.DEVICE_CAPABILITY;
import static igentuman.nc.multiblock.accelerator.TargetChamberRegistration.TARGET_CHAMBER_BE;
import static igentuman.nc.util.ModUtil.*;
import static net.minecraftforge.common.capabilities.ForgeCapabilities.ENERGY;

public class TargetChamberPortBE extends NuclearCraftBE implements MultiblockAttachable {

    public static final String NAME = "target_chamber_beam_port";

    @NBTField
    public byte analogSignal = 0;
    @NBTField
    public byte redstoneMode = SignalSource.HEAT;
    @NBTField
    public BlockPos controllerPos;
    public boolean isSteamMode = false;
    @NBTField
    public boolean connected = false;

    protected TargetChamberMultiblock multiblock;
    protected TargetChamberControllerBE controller;

    public TargetChamberPortBE(BlockPos pPos, BlockState pBlockState) {
        super(TARGET_CHAMBER_BE.get(NAME).get(), pPos, pBlockState);
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

    public TargetChamberMultiblock getMultiblock() {
        return multiblock;
    }

    private boolean updateController() {
        boolean result = false;
        if (controller != controller()) {
            controller = controller();
            controllerPos = BlockPos.ZERO;
            result = true;
        }
        if (controller != null) {
            controllerPos = new BlockPos(controller.getBlockPos());
            result = true;
        }

        return result;
    }

    public void tickServer() {
        if (NuclearCraft.instance.isNcBeStopped || isRemoved()) return;

        int wasSignal = analogSignal;
        boolean wasConnected = connected;
        if(getMultiblock() != null && controller() != null) {
            sendOutPower();
        }
        boolean updated = updateController();
        if(level.getGameTime() % 20 == 0 && controller() != null) {
            pushPull();
        }
        if (level.getGameTime() % 10 == 0 && controller() != null) {
            updateAnalogSignal();

            updated = wasSignal != analogSignal || updated;
            if (redstoneMode == SignalSource.SWITCH) {
                controller().toggleReactor(analogSignal > 0);
            }
        }
        connected = getMultiblock() != null && getMultiblock().isFormed();
        if (updated || wasConnected != connected) {
            if(connected) {
                MultiblockHandler.get(level.dimension()).addIgnoreToUpdate(getBlockPos());
            }
            setChanged();
            level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    private boolean pushPull() {
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
            case SignalSource.PROGRESS:
                analogSignal = (byte) (controller().recipeInfo().ticksProcessed * 15 / controller().recipeInfo().ticks);
                break;
            case SignalSource.ITEMS:
                analogSignal = (byte) (itemHandler().getStackInSlot(0).getCount() * 15 / itemHandler().getStackInSlot(0).getMaxStackSize());
                break;
            case SignalSource.SWITCH:
                analogSignal = (byte) (Math.max(0, getRedstoneSignal()));
                break;
        }
    }

    @Override
    protected void transferEnergyToSide(Direction direction) {
        return;
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
        if(isGtLoaded()) {
            if (cap == com.gregtechceu.gtceu.api.capability.forge.GTCapability.CAPABILITY_ENERGY_CONTAINER) {
                if (isGTEUCapEnabled()) {
                    return getGTEnergy(controller(), side).cast();
                }
            }
        }
        if (cap == ENERGY) {
            if(!isOnlyGTCEUCapEnabled()) {
                return controller().getEnergy().cast();
            } else {
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
        this.multiblock = (TargetChamberMultiblock) multiblock;
        if (this.multiblock != null) {
            controllerPos = this.multiblock.controller().controllerBE().getBlockPos();
            controller = (TargetChamberControllerBE) this.multiblock.controller().controllerBE();
            setChanged();
            level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
        }
    }

    @Override
    public TargetChamberControllerBE controller() {
        if (NuclearCraft.instance.isNcBeStopped || (!getLevel().isClientSide() && getLevel().getServer() != null && !getLevel().getServer().isRunning())) return null;
        if (controller == null && getLevel().isClientSide && controllerPos != null) {
            BlockEntity be = getLevel().getExistingBlockEntity(controllerPos);
            if (be instanceof TargetChamberControllerBE controllerBe) {
                controller = controllerBe;
                return  controller;
            }
        }
        try {
            BlockEntity be = getMultiblock().controller().controllerBE();
            if (be instanceof TargetChamberControllerBE controllerBe) {
                controller = controllerBe;
                return controller;
            }
        } catch (NullPointerException e) {
            if (controllerPos != null) {
                BlockEntity be = getLevel().getExistingBlockEntity(controllerPos);
                if (be instanceof TargetChamberControllerBE controllerBe) {
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
        return controller().getRecipeProgress();
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

    public static class SignalSource {
        public static final byte ENERGY = 1;
        public static final byte HEAT = 2;
        public static final byte PROGRESS = 3;
        public static final byte ITEMS = 4;
        public static final byte SWITCH = 5;
        public static final byte MODERATOR = 6;
    }
}
