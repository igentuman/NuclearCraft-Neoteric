package igentuman.nc.block.fission.entity;

import igentuman.nc.NuclearCraft;
import igentuman.nc.block.entity.MultiblockPortBE;
import igentuman.nc.handler.sided.capability.FluidCapabilityHandler;
import igentuman.nc.handler.sided.capability.ItemCapabilityHandler;
import igentuman.nc.multiblock.AbstractMultiblock;
import igentuman.nc.multiblock.MultiblockHandler;
import igentuman.nc.multiblock.fission.FissionReactorMultiblock;
import igentuman.nc.multiblock.fission.FissionReactorRegistration;
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
    public boolean isSteamMode = false;
    protected FissionReactorMultiblock multiblock;

    public FissionPortBE(BlockPos pPos, BlockState pBlockState) {
        super(FissionReactorRegistration.FISSION_BE.get(NAME).get(), pPos, pBlockState);
    }

    public FissionReactorMultiblock getMultiblock() {
        return multiblock;
    }

    public boolean updateController() {
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
        if(lastTickTime == currentTick || NuclearCraft.instance.isNcBeStopped || isRemoved()) return;
        lastTickTime = currentTick;

        boolean updated = updateController();
        if(getMultiblock() != null && isConnectedToController()) {
            sendOutPower();
        }
        int wasSignal = analogSignal;

        if (currentTick % 10 == 0 && isConnectedToController()) {
            updateAnalogSignal();
            updated |= pushPull();
            updated |= wasSignal != analogSignal;
            switch (redstoneMode) {
                case SignalSource.SWITCH -> controller().toggleMultiblock(analogSignal > 0);
                case SignalSource.MODERATOR -> controller().adjustModerator(analogSignal);
            }
        }

        updateIfNeeded(updated);
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

    public void updateAnalogSignal() {
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
        return (FissionControllerBE) super.controller();
    }

    public double getDepletionProgress() {
        if (controller() == null) return 0;
        return controller().getDepletionProgress();
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
