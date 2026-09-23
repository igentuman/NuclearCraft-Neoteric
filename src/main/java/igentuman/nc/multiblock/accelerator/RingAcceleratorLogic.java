package igentuman.nc.multiblock.accelerator;

import igentuman.nc.api.particle.ParticleAction;
import igentuman.nc.api.particle.ParticleDefinition;
import igentuman.nc.api.particle.ParticleStack;
import igentuman.nc.block_entity.accelerator.RingAcceleratorControllerBE;
import igentuman.nc.config.Multiblocks;
import igentuman.nc.config.ParticleMachinesConfig;
import igentuman.nc.setup.Registers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

public class RingAcceleratorLogic
        extends AbstractAcceleratorLogic<RingAcceleratorCache, RingAcceleratorControllerBE> {

    @Override
    protected Class<RingAcceleratorControllerBE> controllerClass() {
        return RingAcceleratorControllerBE.class;
    }

    @Override
    protected void tickBeam(ServerLevel level, BlockPos controllerPos, RingAcceleratorCache cache,
                            RingAcceleratorControllerBE controller) {
        int signal = updateControlSignal(level, controllerPos, cache, controller);
        tickThermal(level, controller, cache.stats, RingAcceleratorControllerBE.TANK_COOLANT_IN,
                RingAcceleratorControllerBE.TANK_COOLANT_OUT);
        if (signal <= 0 || overheatCooldown > 0) return;
        accelerateInput(controller, cache, signal);
        transferOutput(level, controllerPos, cache);
    }

    private void accelerateInput(RingAcceleratorControllerBE controller, RingAcceleratorCache cache, int signal) {
        ParticleStack stack = input.snapshot(0);
        if (stack.isEmpty() || controller.energyStorage == null) {
            controller.updateDiagnostics(0, false, false, false);
            return;
        }
        ParticleDefinition definition = Registers.PARTICLE_DEFINITION_REGISTRY.get(stack.particleId());
        if (definition == null) {
            controller.updateDiagnostics(0, false, false, true);
            return;
        }

        AcceleratorStats stats = cache.stats;
        double radius = cache.radius();
        ParticleMachinesConfig.Accelerator config = Multiblocks.particleMachines().accelerator();
        long maximum;
        try {
            maximum = RingParticleProcessor.maximumEnergyKeV(definition, stats, radius);
        } catch (ArithmeticException | IllegalArgumentException ignored) {
            controller.updateDiagnostics(0, false, false, true);
            return;
        }
        boolean incompatible = definition.charge() == 0 || definition.massMeV() <= 0;
        boolean tooLow = stack.meanEnergyKeV() < config.ringMinimumInputEnergyKeV();
        boolean tooHigh = stack.meanEnergyKeV() > maximum;
        controller.updateDiagnostics(maximum, tooLow, tooHigh, incompatible);
        if (incompatible || tooLow || tooHigh) return;

        long required;
        ParticleStack accelerated;
        try {
            required = RingParticleProcessor.requiredEnergy(stats, config);
            accelerated = RingParticleProcessor.accelerate(stack, definition, stats, config, radius, signal);
        } catch (ArithmeticException | IllegalArgumentException ignored) {
            return;
        }
        if (controller.energyStorage.getEnergyStoredL() < required) return;

        ParticleStack remainder = pendingOutput.insert(0, accelerated, ParticleAction.SIMULATE);
        long accepted = accelerated.amount() - remainder.amount();
        if (accepted <= 0) return;
        ParticleStack acceptedStack = accepted == accelerated.amount() ? accelerated
                : new ParticleStack(accelerated.particleId(), accepted, accelerated.meanEnergyKeV(),
                accelerated.focus());
        ParticleStack commitRemainder = pendingOutput.insert(0, acceptedStack, ParticleAction.EXECUTE);
        long committed = accepted - commitRemainder.amount();
        if (committed <= 0) return;

        input.extract(0, committed, ParticleAction.EXECUTE);
        controller.energyStorage.drainEnergy(required);
        addProcessingHeat(stats);
        controller.syncParticleDisplay(committed == acceptedStack.amount() ? acceptedStack
                : new ParticleStack(acceptedStack.particleId(), committed,
                acceptedStack.meanEnergyKeV(), acceptedStack.focus()));
    }

    private void transferOutput(ServerLevel level, BlockPos controllerPos, RingAcceleratorCache cache) {
        if (cache.beamOutputs.isEmpty()) return;
        ParticleStack pending = pendingOutput.snapshot(0);
        ParticleDefinition definition = pending.isEmpty() ? null
                : Registers.PARTICLE_DEFINITION_REGISTRY.get(pending.particleId());
        if (definition == null) return;
        ParticleMachinesConfig.Accelerator config = Multiblocks.particleMachines().accelerator();
        transferPendingOutput(level, controllerPos, cache.beamOutputs.getFirst(),
                (stack, distance) -> LinearParticleProcessor.attenuateConnection(stack, definition, config, distance));
    }
}
