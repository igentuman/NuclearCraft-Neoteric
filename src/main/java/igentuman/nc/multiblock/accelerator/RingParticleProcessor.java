package igentuman.nc.multiblock.accelerator;

import igentuman.nc.api.particle.ParticleDefinition;
import igentuman.nc.api.particle.ParticleStack;
import igentuman.nc.config.ParticleMachinesConfig;
import igentuman.nc.particle.ParticlePhysics;

public final class RingParticleProcessor {

    private RingParticleProcessor() {
    }

    public static ParticleStack accelerate(ParticleStack input, ParticleDefinition definition,
                                           AcceleratorStats stats,
                                           ParticleMachinesConfig.Accelerator config,
                                           double radius, int controlSignal) {
        if (input.isEmpty()) return ParticleStack.EMPTY;
        if (definition == null || stats == null || config == null || radius <= 0) {
            throw new IllegalArgumentException("Definition, stats, config and radius are required");
        }
        if (controlSignal < 0 || controlSignal > 15) {
            throw new IllegalArgumentException("Control signal must be within [0, 15]");
        }
        if (definition.charge() == 0 || definition.massMeV() <= 0) {
            throw new IllegalArgumentException("Ring acceleration requires a charged, positive-mass particle");
        }
        if (input.meanEnergyKeV() < config.ringMinimumInputEnergyKeV()) {
            throw new IllegalArgumentException("Particle energy is below the ring input minimum");
        }

        long maximumEnergy = ParticlePhysics.ringMaximumEnergyKeV(definition.charge(), definition.massMeV(),
                stats.dipoleField(), radius, stats.voltage());
        if (input.meanEnergyKeV() > maximumEnergy) {
            throw new IllegalArgumentException("Particle energy exceeds the ring limit");
        }
        long targetEnergy = checkedScale(maximumEnergy, controlSignal / 15D);
        long radiationLoss = ParticlePhysics.synchrotronLossKeV(input.meanEnergyKeV(), definition.massMeV(), radius);
        long outputEnergy = Math.max(0, subtractOrZero(targetEnergy, radiationLoss));
        double focusLoss = ParticlePhysics.focusLoss(input.amount(), definition.charge(), stats.beamLength(),
                config.beamAttenuation(), config.beamScaling());
        double focusGain = ParticlePhysics.focusGain(stats.quadrupoleField(), definition.charge());
        double outputFocus = Math.max(0, input.focus() - focusLoss + focusGain);
        return new ParticleStack(input.particleId(), input.amount(), outputEnergy, outputFocus);
    }

    public static long maximumEnergyKeV(ParticleDefinition definition, AcceleratorStats stats, double radius) {
        if (definition == null || stats == null || radius <= 0) return 0;
        if (definition.charge() == 0 || definition.massMeV() <= 0) return 0;
        return ParticlePhysics.ringMaximumEnergyKeV(definition.charge(), definition.massMeV(),
                stats.dipoleField(), radius, stats.voltage());
    }

    public static long requiredEnergy(AcceleratorStats stats, ParticleMachinesConfig.Accelerator config) {
        return Math.addExact(config.baseEnergyRequirement(), stats.energyPerTick());
    }

    private static long checkedScale(long value, double fraction) {
        double scaled = value * fraction;
        if (!Double.isFinite(scaled) || scaled > Long.MAX_VALUE) {
            throw new ArithmeticException("Scaled ring energy overflows long");
        }
        return (long) Math.floor(scaled);
    }

    private static long subtractOrZero(long value, long loss) {
        if (loss >= value) return 0;
        return value - loss;
    }
}
