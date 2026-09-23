package igentuman.nc.multiblock.accelerator;

import igentuman.nc.api.particle.ParticleDefinition;
import igentuman.nc.api.particle.ParticleStack;
import igentuman.nc.config.ParticleMachinesConfig;
import igentuman.nc.particle.ParticlePhysics;

public final class LinearParticleProcessor {

    private LinearParticleProcessor() {
    }

    public static ParticleStack accelerate(ParticleStack input, ParticleDefinition definition,
                                           AcceleratorStats aggregates,
                                           ParticleMachinesConfig.Accelerator config, int controlSignal) {
        if (input.isEmpty()) return ParticleStack.EMPTY;
        if (definition == null || aggregates == null || config == null) {
            throw new IllegalArgumentException("Definition, aggregates and config are required");
        }
        if (controlSignal < 0 || controlSignal > 15) {
            throw new IllegalArgumentException("Control signal must be within [0, 15]");
        }
        double controlFraction = controlSignal / 15D;
        long gain = ParticlePhysics.linearGainKeV(aggregates.voltage(), definition.charge(), controlFraction);
        long energy = Math.addExact(input.meanEnergyKeV(), gain);
        double loss = ParticlePhysics.focusLoss(input.amount(), definition.charge(), aggregates.beamLength(),
                config.beamAttenuation(), config.beamScaling());
        double gainFocus = ParticlePhysics.focusGain(aggregates.quadrupoleField(), definition.charge());
        double focus = Math.max(0D, input.focus() - loss + gainFocus);
        return new ParticleStack(input.particleId(), input.amount(), energy, focus);
    }

    public static ParticleStack attenuateConnection(ParticleStack input, ParticleDefinition definition,
                                                     ParticleMachinesConfig.Accelerator config, int distance) {
        if (input.isEmpty()) return ParticleStack.EMPTY;
        if (distance < 0) throw new IllegalArgumentException("Connection distance must not be negative");
        double loss = ParticlePhysics.focusLoss(input.amount(), definition.charge(), distance,
                config.beamAttenuation(), config.beamScaling());
        return new ParticleStack(input.particleId(), input.amount(), input.meanEnergyKeV(),
                Math.max(0D, input.focus() - loss));
    }

    public static long requiredEnergy(AcceleratorStats aggregates,
                                      ParticleMachinesConfig.Accelerator config) {
        return Math.addExact(config.baseEnergyRequirement(), aggregates.energyPerTick());
    }
}
