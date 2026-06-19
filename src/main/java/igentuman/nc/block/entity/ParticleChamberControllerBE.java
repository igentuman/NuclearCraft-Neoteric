package igentuman.nc.block.entity;

import igentuman.nc.NuclearCraft;
import igentuman.nc.block.accelerator.entity.AcceleratorBeamPortBE;
import igentuman.nc.content.particles.CapabilityParticleStackHandler;
import igentuman.nc.content.particles.IParticleStackHandler;
import igentuman.nc.content.particles.ParticleStack;
import igentuman.nc.content.particles.ParticleStorage;
import igentuman.nc.datagen.recipes.builder.TConstructRecipeBuilder;
import igentuman.nc.handler.sided.SidedContentHandler;
import igentuman.nc.handler.sided.SlotModePair;
import igentuman.nc.multiblock.AbstractMultiblock;
import igentuman.nc.multiblock.MultiblockHandler;
import igentuman.nc.multiblock.particle_chamber.ParticleChamberMultiblock;
import igentuman.nc.util.PortMode;
import igentuman.nc.util.annotation.NBTField;
import igentuman.nc.util.capability.CustomEnergyStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

import static igentuman.nc.NuclearCraft.currentTick;
import static igentuman.nc.compat.gregtech.GTUtils.getGTEnergy;
import static igentuman.nc.compat.gregtech.GTUtils.isOnlyGTCEUCapEnabled;
import static igentuman.nc.content.particles.CapabilityParticleStackHandler.PARTICLE_HANDLER_CAPABILITY;
import static igentuman.nc.handler.config.CommonConfig.GTCEU_CONFIG;
import static igentuman.nc.util.ModUtil.isGtLoaded;
import static igentuman.nc.util.PortMode.PORT_MODE;
import static net.minecraftforge.common.capabilities.ForgeCapabilities.ENERGY;
import static net.minecraftforge.common.capabilities.ForgeCapabilities.FLUID_HANDLER;
import static net.minecraftforge.common.capabilities.ForgeCapabilities.ITEM_HANDLER;

public abstract class ParticleChamberControllerBE extends MultiblockControllerBE {

    public final ParticleStorage particleStorage;
    protected final LazyOptional<IParticleStackHandler> particleHandler;

    @NBTField
    public boolean enabledByController = false;
    @NBTField
    public boolean hasRedstoneSignal = false;
    @NBTField
    public boolean hasParticle = false;
    @NBTField
    public int detectorsCount = 0;
    public int connectedPorts = 0;
    @NBTField
    public int allDetectors = 0;
    protected boolean forceShutdown = false;


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

    public LazyOptional<IEnergyStorage> getEnergy() {
        return energy;
    }

    public ParticleStack getParticleStack() {
        return particleStorage.getParticle();
    }

    public ParticleStack getClientParticleStack() {
        return particleStorage.getClientParticleStack();
    }

    public ParticleStack getOutputParticle(int i) {
        return null;
    }

    public <T> LazyOptional<T> getOCDevice(Capability<T> cap, Direction side) {
        return LazyOptional.empty();
    }

    public <T> LazyOptional<T> getPeripheral(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return LazyOptional.empty();
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
        particleStorage.outputParticles.clear();
        if (lastTickTime == currentTick) {
            return;
        }
        lastTickTime = currentTick;
        super.tickServer();
        processChamberTick();
        if (!controllerEnabled) {
            particleStorage.clearAll();
        } else {
            if(!hasRecipe()) {
                particleStorage.clearServer();
            }
        }
    }

    /**
     * Per-chamber tick body. Implementations handle validation, recipe selection,
     * recipe processing and block-state updates.
     */
    protected abstract void processChamberTick();

    public LazyOptional<IParticleStackHandler> getParticleHandler() {
        return particleHandler;
    }

    public ParticleStorage getParticleStorage() {
        return particleStorage;
    }

    @Override
    public ParticleChamberMultiblock getMultiblock() {
        return (ParticleChamberMultiblock) multiblock;
    }

    public List<Long> getSortedBeamPorts() {
        if (getMultiblock() == null) {
            return List.of();
        }
        List<Long> ports = new ArrayList<>(getMultiblock().getBeamPorts());
        ports.sort(Long::compare);
        return ports;
    }

    public List<Map<String, Object>> getBeamPortsInfo() {
        List<Map<String, Object>> infoList = new ArrayList<>();
        if (getMultiblock() == null) return infoList;
        List<Long> sortedPorts = getSortedBeamPorts();
        for (int i = 0; i < sortedPorts.size(); i++) {
            long packedPos = sortedPorts.get(i);
            BlockPos pos = BlockPos.of(packedPos);
            Map<String, Object> portInfo = new HashMap<>();
            portInfo.put("id", i);
            portInfo.put("x", pos.getX());
            portInfo.put("y", pos.getY());
            portInfo.put("z", pos.getZ());

            BlockState state = level.getBlockState(pos);
            String modeStr = "disabled";
            if (state.hasProperty(PORT_MODE)) {
                modeStr = state.getValue(PORT_MODE).getSerializedName();
            }
            portInfo.put("mode", modeStr);

            BlockEntity be = level.getBlockEntity(pos);
            Map<String, Object> particleInfo = null;
            if (be instanceof AcceleratorBeamPortBE beamPortBe) {
                ParticleStack particleStack = beamPortBe.clientParticle;
                if (particleStack != null && !particleStack.isEmpty() && particleStack.getParticle() != null) {
                    particleInfo = new HashMap<>();
                    particleInfo.put("energy", particleStack.getMeanEnergy());
                    particleInfo.put("focus", particleStack.getFocus());
                    particleInfo.put("amount", particleStack.getAmount());
                    particleInfo.put("particle", particleStack.getParticle().getName());
                }
            }
            portInfo.put("particle", particleInfo);

            infoList.add(portInfo);
        }
        return infoList;
    }

    public boolean setBeamPortMode(int id, String mode) {
        if (getMultiblock() == null) return false;
        List<Long> sortedPorts = getSortedBeamPorts();
        if (id < 0 || id >= sortedPorts.size()) return false;
        long packedPos = sortedPorts.get(id);
        BlockPos pos = BlockPos.of(packedPos);
        BlockState state = level.getBlockState(pos);
        if (!state.hasProperty(PORT_MODE)) return false;

        PortMode.Mode enumMode;
        if (mode.equalsIgnoreCase("input")) {
            enumMode = PortMode.Mode.INPUT;
        } else if (mode.equalsIgnoreCase("output")) {
            enumMode = PortMode.Mode.OUTPUT;
        } else if (mode.equalsIgnoreCase("disabled")) {
            enumMode = PortMode.Mode.DISABLED;
        } else {
            return false;
        }

        MultiblockHandler.get(level.dimension()).addIgnoreToUpdate(pos);
        level.setBlockAndUpdate(pos, state.setValue(PORT_MODE, enumMode));
        return true;
    }
}
