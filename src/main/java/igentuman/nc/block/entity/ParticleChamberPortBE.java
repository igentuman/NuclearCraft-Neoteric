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
    public byte redstoneMode = SignalSource.INPUT;
    @NBTField
    public BlockPos controllerPos;
    @NBTField
    public boolean connected = false;

    protected M multiblock;
    protected C controller;

    public ParticleChamberPortBE(BlockEntityType<?> type, BlockPos pPos, BlockState pBlockState) {
        super(type, pPos, pBlockState);
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


    public void updateAnalogSignal() {
        if (controller() == null) {
            analogSignal = 0;
            return;
        }
        switch (redstoneMode) {
            case SignalSource.INPUT -> analogSignal = (byte) Math.max(0, getRedstoneSignal());
            case SignalSource.OUTPUT -> analogSignal = (byte) (hasParticle() ? 15 : 0);
        }
    }

    @Override
    protected void transferEnergyToSide(Direction direction) {
        // ports do not push energy out — the controller owns energy
    }

    public int getEnergyStored() {
        if (!isConnectedToController()) return 0;
        return controller().energyStorage().getEnergyStored();
    }

    public int getMaxEnergyStored() {
        if (!isConnectedToController()) return 0;
        return controller().energyStorage().getMaxEnergyStored();
    }

    public int energyPerTick() {
        if (!isConnectedToController()) return 0;
        return controller().energyPerTick;
    }

    public double getDepletionProgress() {
        if (!isConnectedToController()) return 0;
        return controller().getRecipeProgress();
    }

    public boolean hasParticle() {
        if (!isConnectedToController()) return false;
        return controller().hasParticle;
    }

    public ParticleStack getParticleStack() {
        if (!isConnectedToController()) return null;
        return controller().particleStorage.getParticle();
    }

    public void toggleRedstoneMode() {
        redstoneMode++;
        if (redstoneMode > SignalSource.OUTPUT) {
            redstoneMode = SignalSource.INPUT;
        }
        analogSignal = 0;
        MultiblockHandler.get(level.dimension()).addIgnoreToUpdate(getBlockPos());
        setChanged();
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
    }

    public boolean hasFluidTanks() {
        if (!isConnectedToController()) return false;
        return controller().contentHandler().inputFluidSlots > 0 || controller().contentHandler().outputFluidSlots > 0;
    }

    public boolean hasItems() {
        if (!isConnectedToController()) return false;
        return controller().contentHandler().inputItemSlots > 0 || controller().contentHandler().outputItemSlots > 0;
    }

    public static class SignalSource {
        public static final byte INPUT = 1;
        public static final byte OUTPUT = 2;
    }
}
