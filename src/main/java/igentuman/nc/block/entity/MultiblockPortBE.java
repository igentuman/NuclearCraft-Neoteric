package igentuman.nc.block.entity;

import igentuman.api.nc.multiblock.MultiblockAttachable;
import igentuman.nc.NuclearCraft;
import igentuman.nc.block.fission.entity.FissionControllerBE;
import igentuman.nc.block.fission.entity.FissionPortBE;
import igentuman.nc.block.turbine.entity.TurbineControllerBE;
import igentuman.nc.block.turbine.entity.TurbinePortBE;
import igentuman.nc.handler.sided.capability.FluidCapabilityHandler;
import igentuman.nc.handler.sided.capability.ItemCapabilityHandler;
import igentuman.nc.multiblock.MultiblockHandler;
import igentuman.nc.util.WorldUtil;
import igentuman.nc.util.annotation.NBTField;
import igentuman.nc.util.capability.CustomEnergyStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

import static igentuman.nc.NuclearCraft.currentTick;
import static igentuman.nc.compat.gregtech.GTUtils.*;
import static igentuman.nc.util.ModUtil.isCcLoaded;
import static igentuman.nc.util.ModUtil.isGtLoaded;
import static net.minecraftforge.common.capabilities.ForgeCapabilities.ENERGY;

public abstract class MultiblockPortBE extends NuclearCraftBE implements MultiblockAttachable {

    protected MultiblockControllerBE controller;
    @NBTField
    public BlockPos controllerPos;
    @NBTField
    public byte analogSignal = 0;
    @NBTField
    public byte redstoneMode = FissionPortBE.SignalSource.HEAT;
    @NBTField
    public boolean connected = false;
    protected long lastTickTime = 0;

    private static final LazyOptional<IItemHandler> DUMMY_ITEM_HANDLER = LazyOptional.of(() -> new ItemStackHandler(0));
    private static final LazyOptional<IFluidHandler> DUMMY_FLUID_HANDLER = LazyOptional.of(() -> new FluidTank(0));
    private static final LazyOptional<IEnergyStorage> DUMMY_ENERGY_HANDLER = LazyOptional.of(() -> new EnergyStorage(0));

    public MultiblockPortBE(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
    }

    protected void updateIfNeeded(boolean updated) {
        if(updated || needToUpdate) {
            needToUpdate = false;
            MultiblockHandler.get(level.dimension()).addIgnoreToUpdate(getBlockPos());
            setChanged();
            level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public boolean canInvalidateCache() {
        return true;
    }

    public Direction getFacing() {
        return getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
    }

    public boolean hasRedstoneSignal() {
        return Objects.requireNonNull(getLevel()).hasNeighborSignal(worldPosition);
    }


    public boolean pushPull() {
        Direction dir = getFacing();
        boolean wasChanged = false;
        if (itemHandler() != null) {
            wasChanged |= itemHandler().pushItems(dir, true, worldPosition);
            wasChanged |= itemHandler().pullItems(dir, true, worldPosition);
        }
        if(fluidHandler() != null) {
            wasChanged |= fluidHandler().pullFluids(dir, false, worldPosition);
        }
        return wasChanged;
    }

    public void updateAnalogSignal() {
    }

    public boolean isConnectedToController() {
        return controller() != null && !controller().isRemoved() && controller().isInternalValid;
    }

    protected ItemCapabilityHandler itemHandler() {
        return (!isConnectedToController() || controller().contentHandler() == null)
                ? null : controller().contentHandler().itemHandler;
    }

    protected FluidCapabilityHandler fluidHandler() {
        return (!isConnectedToController() || controller().contentHandler() == null)
                ? null : controller().contentHandler().fluidHandler;
    }

    protected <T> LazyOptional<T> fluidHandler(@Nullable Direction side) {
        return controller().contentHandler().getFluidCapability(side);
    }

    public FluidTank getFluidTank(int i) {
        if (!isConnectedToController() || controller().contentHandler() == null) return null;
        return controller().getFluidTank(i);
    }

    @Override
    public MultiblockControllerBE controller() {
        if (
                NuclearCraft.instance.isNcBeStopped
                || (!getLevel().isClientSide() && getLevel().getServer() != null && !getLevel().getServer().isRunning())
        ) return null;
        if (controller == null && getLevel().isClientSide && controllerPos != null) {
            BlockEntity be = getLevel().getExistingBlockEntity(controllerPos);
            if (be instanceof FissionControllerBE controllerBe) {
                controller = controllerBe;
                return  controller;
            }
        }
        try {
            BlockEntity be = getMultiblock().controller().controllerBE();
            if (be instanceof MultiblockControllerBE controllerBe) {
                controller = controllerBe;
                return controller;
            }
        } catch (NullPointerException e) {
            if (controllerPos != null) {
                BlockEntity be = getLevel().getExistingBlockEntity(controllerPos);
                if (be instanceof MultiblockControllerBE controllerBe) {
                    controller = controllerBe;
                }
            }
        }
        return controller;
    }

    public boolean updateController() {
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
        if (lastTickTime == currentTick) return;
        lastTickTime = currentTick;
        byte wasSignal = analogSignal;
        boolean wasConnected = connected;
        boolean updated = updateController() || needToUpdate;
        if (isConnectedToController()) {
            if (currentTick % 10 == 0) {
                updateAnalogSignal();
                pushPull();
                updated |= wasSignal != analogSignal;
                if (redstoneMode == ParticleChamberPortBE.SignalSource.INPUT) {
                    controller().toggleMultiblock(analogSignal > 0);
                }
            }
        }
        connected = getMultiblock() != null && getMultiblock().isFormed();
        updated |= wasConnected != connected;
        if (updated) {
            needToUpdate = false;
            if (connected) {
                MultiblockHandler.get(level.dimension()).addIgnoreToUpdate(getBlockPos());
            }
            setChanged();
            assert level != null;
            level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if(!isConnectedToController()) {
            if (cap == ForgeCapabilities.FLUID_HANDLER) {
                return DUMMY_FLUID_HANDLER.cast();
            }
            if (cap == ForgeCapabilities.ITEM_HANDLER) {
                return DUMMY_ITEM_HANDLER.cast();
            }
            if (cap == ENERGY) {
                return DUMMY_ENERGY_HANDLER.cast();
            }
            return super.getCapability(cap, side);
        }

        if (cap == ForgeCapabilities.FLUID_HANDLER) {
            return controller().getCapability(cap, side);
        }

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
        if(isCcLoaded()) {
            if(cap == dan200.computercraft.shared.Capabilities.CAPABILITY_PERIPHERAL) {
                return controller().getPeripheral(cap, side);
            }
        }
        return super.getCapability(cap, side);
    }

    public int getRedstoneSignal() {
        return Objects.requireNonNull(getLevel()).getBestNeighborSignal(worldPosition);
    }

    public int getEnergyStored() {
        if(!isConnectedToController()) return 0;
        return controller().energyStorage().getEnergyStored();
    }

    public int getMaxEnergyStored() {
        if(!isConnectedToController()) return 0;
        return controller().energyStorage().getMaxEnergyStored();
    }

    public int energyPerTick() {
        if(!isConnectedToController()) return 0;
        return controller().energyPerTick;
    }

    protected void transferEnergyToSide(Direction direction) {
        if (getEnergyStored() <= 0) {
            return; // No energy to transfer
        }
        int wasEnergy = getEnergyStored();
        BlockEntity be = level.getExistingBlockEntity(worldPosition.relative(direction));
        if (be == null || be instanceof TurbinePortBE || be instanceof TurbineControllerBE) {
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

    public int getEnergyRequired() {
        if(!isConnectedToController()) return 0;
        return controller().energyPerTick;
    }
}
