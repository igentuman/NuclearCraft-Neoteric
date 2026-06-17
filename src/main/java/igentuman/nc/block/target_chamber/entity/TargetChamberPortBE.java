package igentuman.nc.block.target_chamber.entity;

import igentuman.nc.NuclearCraft;
import igentuman.nc.block.entity.ParticleChamberControllerBE;
import igentuman.nc.block.entity.ParticleChamberPortBE;
import igentuman.nc.content.particles.ParticleStack;
import igentuman.nc.multiblock.particle_chamber.ParticleChamberMultiblock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static igentuman.nc.NuclearCraft.currentTick;
import static igentuman.nc.compat.oc2.TargetChamberDevice.DEVICE_CAPABILITY;
import static igentuman.nc.content.particles.CapabilityParticleStackHandler.PARTICLE_HANDLER_CAPABILITY;
import static igentuman.nc.multiblock.particle_chamber.ParticleChamberRegistration.TARGET_CHAMBER_BE;
import static igentuman.nc.util.ModUtil.isCcLoaded;
import static igentuman.nc.util.ModUtil.isOC2Loaded;

public class TargetChamberPortBE extends ParticleChamberPortBE<ParticleChamberControllerBE, ParticleChamberMultiblock> {

    public static final String NAME = "target_chamber_port";

    public TargetChamberPortBE(BlockPos pPos, BlockState pBlockState) {
        super(TARGET_CHAMBER_BE.get(NAME).get(), pPos, pBlockState);
    }

    @Override
    protected Class<ParticleChamberControllerBE> controllerClass() {
        return ParticleChamberControllerBE.class;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if(!isConnectedToController()) return LazyOptional.empty();
        if (cap == PARTICLE_HANDLER_CAPABILITY) {
            return LazyOptional.empty();
        }
        return controller().getCapability(cap, side);
    }

    @Override
    public void tickServer() {
        if(lastTickTime == currentTick || NuclearCraft.instance.isNcBeStopped || isRemoved()) return;
        lastTickTime = currentTick;
        boolean updated = updateController();
        if(!isConnectedToController()) return;
        int wasSignal = analogSignal;
        updateAnalogSignal();
        if(redstoneMode == SignalSource.INPUT && hasRedstoneSignal()) {
            controller().setRedstoneByPort(getRedstoneSignal());
        }
        updated |= wasSignal != analogSignal;
        updated |= pushPull();
        updateIfNeeded(updated);
    }
}
