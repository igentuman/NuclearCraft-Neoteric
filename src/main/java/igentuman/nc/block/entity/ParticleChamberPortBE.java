package igentuman.nc.block.entity;

import igentuman.nc.NuclearCraft;
import igentuman.nc.content.particles.ParticleStack;
import igentuman.nc.handler.sided.capability.FluidCapabilityHandler;
import igentuman.nc.handler.sided.capability.ItemCapabilityHandler;
import igentuman.nc.multiblock.AbstractMultiblock;
import igentuman.nc.multiblock.MultiblockHandler;
import igentuman.nc.util.annotation.NBTField;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
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
import static igentuman.nc.compat.gregtech.GTUtils.getGTEnergy;
import static igentuman.nc.compat.gregtech.GTUtils.isOnlyGTCEUCapEnabled;
import static igentuman.nc.util.ModUtil.isGtLoaded;
import static net.minecraftforge.common.capabilities.ForgeCapabilities.ENERGY;

public abstract class ParticleChamberPortBE<C extends ParticleChamberControllerBE, M extends AbstractMultiblock> extends MultiblockPortBE {

    @NBTField
    public byte analogSignal = 0;
    @NBTField
    public byte redstoneMode = SignalSource.HEAT;
    @NBTField
    public BlockPos controllerPos;
    @NBTField
    public boolean connected = false;

    protected M multiblock;
    protected C controller;

    public ParticleChamberPortBE(BlockEntityType<?> type, BlockPos pPos, BlockState pBlockState) {
        super(type, pPos, pBlockState);
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

    @Override
    @SuppressWarnings("unchecked")
    public void setMultiblock(AbstractMultiblock multiblock) {
        if (multiblock == this.multiblock) return;
        this.multiblock = (M) multiblock;
        if (this.multiblock != null) {
            controllerPos = this.multiblock.controller().controllerBE().getBlockPos();
            controller = (C) this.multiblock.controller().controllerBE();
            MultiblockHandler.get(level.dimension()).addIgnoreToUpdate(getBlockPos());
            markDirty();
        }
    }

    @Override
    public M getMultiblock() {
        return multiblock;
    }

    @Override
    public boolean canInvalidateCache() {
        return true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public C controller() {
        if (NuclearCraft.instance.isNcBeStopped
                || (!getLevel().isClientSide() && getLevel().getServer() != null && !getLevel().getServer().isRunning())) {
            return null;
        }
        if (controller == null && getLevel().isClientSide && controllerPos != null) {
            BlockEntity be = getLevel().getExistingBlockEntity(controllerPos);
            if (controllerClass().isInstance(be)) {
                controller = (C) be;
                return controller;
            }
        }
        try {
            BlockEntity be = getMultiblock().controller().controllerBE();
            if (controllerClass().isInstance(be)) {
                controller = (C) be;
                return controller;
            }
        } catch (NullPointerException e) {
            if (controllerPos != null) {
                BlockEntity be = getLevel().getExistingBlockEntity(controllerPos);
                if (controllerClass().isInstance(be)) {
                    controller = (C) be;
                }
            }
        }
        return controller;
    }

    protected abstract Class<C> controllerClass();

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
        if (lastTickTime == level.getGameTime()) return;
        lastTickTime = level.getGameTime();
        byte wasSignal = analogSignal;
        boolean wasConnected = connected;
        boolean updated = updateController();
        if (controller() != null) {
            if (currentTick % 20 == 0) {
                pushPull();
            }
            if (currentTick % 10 == 0) {
                updateAnalogSignal();
                updated = wasSignal != analogSignal || updated;
                if (redstoneMode == SignalSource.SWITCH) {
                    controller().toggleReactor(analogSignal > 0);
                }
            }
        }
        connected = getMultiblock() != null && getMultiblock().isFormed();
        if (needToUpdate || updated || wasConnected != connected || currentTick % 20 == 0) {
            needToUpdate = false;
            if (connected) {
                MultiblockHandler.get(level.dimension()).addIgnoreToUpdate(getBlockPos());
            }
            setChanged();
            level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public boolean pushPull() {
        if (controller() == null || itemHandler() == null) return false;
        Direction dir = getFacing();
        boolean pushed = itemHandler().pushItems(dir, true, worldPosition);
        pushed = itemHandler().pullItems(dir, true, worldPosition) || pushed;
        return pushed;
    }

    protected void updateAnalogSignal() {
        if (controller() == null) {
            analogSignal = 0;
            return;
        }
        switch (redstoneMode) {
            case SignalSource.ENERGY -> analogSignal = (byte) (controller().energyStorage().getEnergyStored() * 15
                    / Math.max(1, controller().energyStorage().getMaxEnergyStored()));
            case SignalSource.PROGRESS -> {
                int ticks = controller().recipeInfo() == null ? 0 : controller().recipeInfo().ticks;
                double processed = controller().recipeInfo() == null ? 0 : controller().recipeInfo().ticksProcessed;
                analogSignal = (byte) (processed * 15 / Math.max(1, ticks));
            }
            case SignalSource.ITEMS -> {
                if (itemHandler() != null) {
                    int count = itemHandler().getStackInSlot(0).getCount();
                    int max = Math.max(1, itemHandler().getStackInSlot(0).getMaxStackSize());
                    analogSignal = (byte) (count * 15 / max);
                }
            }
            case SignalSource.SWITCH -> analogSignal = (byte) Math.max(0, getRedstoneSignal());
        }
    }

    @Override
    protected void transferEnergyToSide(Direction direction) {
        // ports do not push energy out — the controller owns energy
    }

    protected ItemCapabilityHandler itemHandler() {
        return controller() == null || controller().contentHandler() == null
                ? null : controller().contentHandler().itemHandler;
    }

    protected FluidCapabilityHandler fluidHandler() {
        return controller() == null || controller().contentHandler() == null
                ? null : controller().contentHandler().fluidHandler;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (controller() == null) return super.getCapability(cap, side);
        if (cap == ForgeCapabilities.ITEM_HANDLER && controller().contentHandler() != null) {
            return controller().getCapability(cap, side);
        }
        if (cap == ForgeCapabilities.FLUID_HANDLER && controller().contentHandler() != null) {
            return controller().getCapability(cap, side);
        }
        if (isGtLoaded()) {
            if (cap == com.gregtechceu.gtceu.api.capability.forge.GTCapability.CAPABILITY_ENERGY_CONTAINER) {
                if (NuclearCraftBE.isGTEUCapEnabled()) {
                    return getGTEnergy(controller(), side).cast();
                }
            }
        }
        if (cap == ENERGY) {
            if (!isOnlyGTCEUCapEnabled()) {
                return controller().getEnergy().cast();
            }
            return LazyOptional.empty();
        }
        return super.getCapability(cap, side);
    }

    public int getEnergyStored() {
        if (controller() == null) return 0;
        return controller().energyStorage().getEnergyStored();
    }

    public int getMaxEnergyStored() {
        if (controller() == null) return 0;
        return controller().energyStorage().getMaxEnergyStored();
    }

    public int energyPerTick() {
        if (controller() == null) return 0;
        return controller().energyPerTick;
    }

    public double getDepletionProgress() {
        if (controller() == null) return 0;
        return controller().getRecipeProgress();
    }

    public FluidTank getFluidTank(int i) {
        if (controller() == null || controller().contentHandler() == null) return null;
        return controller().getFluidTank(i);
    }

    public boolean hasParticle() {
        if (controller() == null) return false;
        return controller().hasParticle;
    }

    public ParticleStack getParticleStack() {
        if (controller() == null) return null;
        return controller().particleStorage.getParticle();
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

    public static class SignalSource {
        public static final byte ENERGY = 1;
        public static final byte HEAT = 2;
        public static final byte PROGRESS = 3;
        public static final byte ITEMS = 4;
        public static final byte SWITCH = 5;
        public static final byte MODERATOR = 6;
    }
}
