package igentuman.nc.block.entity;

import igentuman.nc.NuclearCraft;
import igentuman.nc.content.particles.CapabilityParticleStackHandler;
import igentuman.nc.content.particles.IParticleStackHandler;
import igentuman.nc.content.particles.ParticleStack;
import igentuman.nc.content.particles.ParticleStorage;
import igentuman.nc.handler.sided.SidedContentHandler;
import igentuman.nc.handler.sided.SlotModePair;
import igentuman.nc.util.annotation.NBTField;
import igentuman.nc.util.capability.CustomEnergyStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

import static igentuman.nc.NuclearCraft.currentTick;
import static igentuman.nc.compat.gregtech.GTUtils.getGTEnergy;
import static igentuman.nc.compat.gregtech.GTUtils.isOnlyGTCEUCapEnabled;
import static igentuman.nc.content.particles.CapabilityParticleStackHandler.PARTICLE_HANDLER_CAPABILITY;
import static igentuman.nc.handler.config.CommonConfig.GTCEU_CONFIG;
import static igentuman.nc.util.ModUtil.isGtLoaded;
import static net.minecraftforge.common.capabilities.ForgeCapabilities.ENERGY;
import static net.minecraftforge.common.capabilities.ForgeCapabilities.FLUID_HANDLER;
import static net.minecraftforge.common.capabilities.ForgeCapabilities.ITEM_HANDLER;

public abstract class ParticleChamberControllerBE extends MultiblockControllerBE {

    public final ParticleStorage particleStorage;
    protected final LazyOptional<IParticleStackHandler> particleHandler;

    @NBTField
    public boolean powered = false;
    @NBTField
    public int energyPerTick = 0;
    @NBTField
    public boolean enabledByController = false;
    @NBTField
    public boolean hasRedstoneSignal = false;
    @NBTField
    public boolean hasParticle = false;
    @NBTField
    public int detectorsCount = 0;
    @NBTField
    public double efficiency = 0;
    public int connectedPorts = 0;
    @NBTField
    public int allDetectors = 0;

    protected boolean forceShutdown = false;
    private Direction facing;

    public ParticleChamberControllerBE(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
        if (energyStorage == null) {
            energyStorage = createEnergy();
            energyStorage
                    .setInputEnergyTier(getBaseGTEnergyTier())
                    .setOutputEnergyTier(0)
                    .setInputAmperage(16)
                    .setOutputAmperage(0);
        }
        if (energy == null) {
            energy = LazyOptional.of(() -> energyStorage);
        }
        particleStorage = new ParticleStorage();
        particleStorage.setTileEntity(this);
        particleHandler = CapabilityParticleStackHandler.createHandler(particleStorage);
        contentHandler = new SidedContentHandler(
                1, 1,
                1, 1);
        contentHandler.setBlockEntity(this);
        contentHandler.itemHandler.setGlobalMode(0, SlotModePair.SlotMode.PULL);
        contentHandler.itemHandler.setGlobalMode(1, SlotModePair.SlotMode.PUSH);
        contentHandler.fluidHandler.setGlobalMode(0, SlotModePair.SlotMode.PULL);
        contentHandler.fluidHandler.setGlobalMode(1, SlotModePair.SlotMode.PUSH);
    }

    protected CustomEnergyStorage createEnergy() {
        return new CustomEnergyStorage(100_000_000, 100_000_000, 0) {
            @Override
            protected void onEnergyChanged() {
                setChanged();
            }
        };
    }

    @Override
    public int getBaseGTEnergyTier() {
        return GTCEU_CONFIG.ACCELERATORS_ENERGY_TIER.get().ordinal();
    }

    public Direction getFacing() {
        if (facing == null) {
            facing = getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        }
        return facing;
    }

    public boolean hasRedstoneSignal() {
        if (currentTick % 10 == 0 && getLevel() != null) {
            hasRedstoneSignal = getLevel().hasNeighborSignal(getBlockPos());
        }
        return enabledByController || hasRedstoneSignal;
    }

    public void enableReactor() {
        toggleReactor(true);
    }

    public void toggleReactor(boolean mode) {
        controllerEnabled = mode || getRedstoneSignal() > 0;
        enabledByController = mode;
    }

    public void forceShutdown() {
        forceShutdown = true;
    }

    public void disableForceShutdown() {
        forceShutdown = false;
    }

    @Override
    public int getRedstoneSignal() {
        return Objects.requireNonNull(getLevel()).getBestNeighborSignal(worldPosition);
    }

    public LazyOptional<IEnergyStorage> getEnergy() {
        return energy;
    }

    public ParticleStack getParticleStack() {
        return particleStorage.getParticle();
    }

    public ParticleStack getClientParticleStack() {
        return particleStorage.getClientParticleStack();
    }

    public boolean hasRecipe() {
        return recipeInfo() != null && recipeInfo().recipe() != null;
    }

    public boolean recipeIsStuck() {
        return recipeInfo() != null && recipeInfo().isStuck();
    }

    public double getRecipeProgress() {
        return recipeInfo() == null ? 0 : recipeInfo().getProgress();
    }

    public boolean isProcessing() {
        return hasRecipe() && recipeInfo().ticksProcessed > 0 && !recipeInfo().isCompleted();
    }

    public boolean isShutdown() {
        return forceShutdown;
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("Info")) {
            CompoundTag infoTag = tag.getCompound("Info");
            if (infoTag.contains("particle_storage")) {
                particleStorage.readFromNBT(infoTag.getCompound("particle_storage"));
            }
        }
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (tag.contains("Info")) {
            CompoundTag infoTag = tag.getCompound("Info");
            infoTag.put("particle_storage", particleStorage.writeToNBT(new CompoundTag()));
        }
    }

    @Override
    public void loadClientData(CompoundTag tag) {
        super.loadClientData(tag);
        if (tag.contains("Info")) {
            CompoundTag infoTag = tag.getCompound("Info");
            if (infoTag.contains("particle_storage")) {
                particleStorage.readFromNBT(infoTag.getCompound("particle_storage"));
            }
        }
    }

    @Override
    protected void saveClientData(CompoundTag tag) {
        super.saveClientData(tag);
        if (tag.contains("Info")) {
            CompoundTag infoTag = tag.getCompound("Info");
            infoTag.put("particle_storage", particleStorage.writeToNBT(new CompoundTag()));
        }
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == PARTICLE_HANDLER_CAPABILITY) {
            return particleHandler.cast();
        }
        if (cap == FLUID_HANDLER && contentHandler() != null) {
            return contentHandler().getFluidCapability(side);
        }
        if (cap == ITEM_HANDLER && contentHandler() != null) {
            return contentHandler().getItemCapability(side);
        }
        if (isGtLoaded()) {
            if (cap == com.gregtechceu.gtceu.api.capability.forge.GTCapability.CAPABILITY_ENERGY_CONTAINER) {
                if (isGTEUCapEnabled()) {
                    return getGTEnergy(this, side).cast();
                }
            }
        }
        if (cap == ENERGY) {
            if (!isOnlyGTCEUCapEnabled()) {
                return getEnergy().cast();
            }
            return LazyOptional.empty();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void tickServer() {
        if (NuclearCraft.instance.isNcBeStopped || isRemoved()) {
            controllerEnabled = false;
            return;
        }
        particleStorage.clearClient();
        if (lastTickTime == level.getGameTime()) {
            return;
        }
        lastTickTime = level.getGameTime();
        super.tickServer();
        processChamberTick();
        particleStorage.clearServer();
    }

    /**
     * Per-chamber tick body. Implementations handle validation, recipe selection,
     * recipe processing and block-state updates.
     */
    protected abstract void processChamberTick();
}
