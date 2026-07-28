package igentuman.nc.block.accelerator.entity;

import igentuman.nc.NuclearCraft;
import igentuman.nc.block.entity.MultiblockPortBE;
import igentuman.nc.handler.config.CommonConfig;
import igentuman.nc.multiblock.AbstractMultiblock;
import igentuman.nc.multiblock.MultiblockHandler;
import igentuman.nc.multiblock.accelerator.AbstractAcceleratorMultiblock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static igentuman.nc.NuclearCraft.currentTick;
import static igentuman.nc.NuclearCraft.debugLog;
import static igentuman.nc.content.particles.CapabilityParticleStackHandler.PARTICLE_HANDLER_CAPABILITY;
import static igentuman.nc.handler.config.CommonConfig.GTCEU_CONFIG;
import static igentuman.nc.multiblock.accelerator.AcceleratorRegistration.ACCELERATOR_BE;

public class AcceleratorPortBE extends MultiblockPortBE {

    public static String NAME = "accelerator_port";

    protected AbstractAcceleratorMultiblock multiblock;
    public AbstractAcceleratorControllerBE controller;

    public AcceleratorPortBE(BlockPos pPos, BlockState pBlockState) {
        super(ACCELERATOR_BE.get(NAME).get(), pPos, pBlockState);
        redstoneMode = SignalSource.INPUT;
    }

    @Override
    public void setMultiblock(AbstractMultiblock multiblock) {
        if(multiblock == this.multiblock) return;
        this.multiblock = (AbstractAcceleratorMultiblock) multiblock;
        if (this.multiblock != null) {
            controllerPos = this.multiblock.controller().controllerBE().getBlockPos();
            controller = (AbstractAcceleratorControllerBE) this.multiblock.controller().controllerBE();
            markDirty();
        }
    }

    @Override
    public AbstractAcceleratorMultiblock getMultiblock() {
        return multiblock;
    }


    @Override
    public void tickServer() {
        if(lastTickTime == currentTick || NuclearCraft.instance.isNcBeStopped || isRemoved()) return;
        lastTickTime = currentTick;
        boolean updated = updateController();
        if(!isConnectedToController()) return;
        int wasSignal = analogSignal;
        updateAnalogSignal();
        updated |= wasSignal != analogSignal;
        updated |= pushPull();
        updateIfNeeded(updated);
    }

    public void updateAnalogSignal() {
        if(!isConnectedToController()) {
            analogSignal = 0;
            return;
        }
        switch (redstoneMode) {
            case SignalSource.INPUT:
                // Redstone control is pulled directly by AbstractAcceleratorControllerBE.tickServer()
                // from every connected INPUT-mode port, rather than pushed from here. Pushing from
                // each port independently raced against every other port on the same structure
                // (all default to INPUT mode), and whichever port ticked first each cycle silently
                // overwrote the others' signal.
                analogSignal = (byte) Math.max(0, getRedstoneSignal());
                break;
            case SignalSource.OUTPUT:
                analogSignal = (byte) (hasParticle() ? 15 : 0);
                break;
            case SignalSource.TEMPERATURE:
                int maxTemp = getMaxTemperature();
                analogSignal = maxTemp <= 0 ? 0 : (byte) Math.max(0, Math.min(15, getCurrentTemperature() * 15 / maxTemp));
                break;
        }
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if(!isConnectedToController()) return super.getCapability(cap, side);
        return controller().getCapability(cap, side);
    }

    @Override
    public AbstractAcceleratorControllerBE controller() {
        return (AbstractAcceleratorControllerBE)  super.controller();
    }

    public void toggleComparatorMode() {
        redstoneMode++;
        if(redstoneMode > SignalSource.TEMPERATURE) {
            redstoneMode = SignalSource.INPUT;
        }
        analogSignal = 0;
        MultiblockHandler.get(level.dimension()).addIgnoreToUpdate(getBlockPos());
        setChanged();
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
    }

    public boolean hasParticle() {
        if(!isConnectedToController()) return false;
        return controller().hasParticle;
    }

    public int getCurrentTemperature() {
        if(!isConnectedToController()) return 0;
        return controller().getTemperature();
    }

    public int getMaxTemperature() {
        if(!isConnectedToController()) return 0;
        return controller().maxTemperature;
    }

    public CommonConfig.GTCEUCompatibilityConfig.GTCEUTier getTier() {
        if(!isConnectedToController()) return GTCEU_CONFIG.ACCELERATORS_ENERGY_TIER.get();
        return controller().getTier();
    }

    public static class SignalSource {
        public static final byte INPUT = 1;
        public static final byte OUTPUT = 2;
        public static final byte TEMPERATURE = 3;
    }
}
