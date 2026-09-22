package igentuman.nc.multiblock.accelerator;

import igentuman.nc.api.particle.ParticleDefinition;
import igentuman.nc.api.particle.ParticleStack;
import igentuman.nc.config.ParticleMachinesConfig;
import igentuman.nc.multiblock.StructureRecord;
import igentuman.nc.multiblock.geometry.SquareRingFootprint;
import igentuman.nc.particle.ParticlePhysics;

public final class RingParticleProcessor {

    private RingParticleProcessor() {
    }

    public static ParticleStack accelerate(ParticleStack input, ParticleDefinition definition,
                                           StructureRecord.StructureAggregates aggregates,
                                           ParticleMachinesConfig.Accelerator config,
                                           SquareRingFootprint footprint, int controlSignal) {
        if (input.isEmpty()) return ParticleStack.EMPTY;
        if (definition == null || aggregates == null || config == null || footprint == null) {
            throw new IllegalArgumentException("Definition, aggregates, config and footprint are required");
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

        double radius = radius(footprint);
        long maximumEnergy = ParticlePhysics.ringMaximumEnergyKeV(definition.charge(), definition.massMeV(),
                aggregates.dipoleField(), radius, aggregates.voltage());
        if (input.meanEnergyKeV() > maximumEnergy) {
            throw new IllegalArgumentException("Particle energy exceeds the ring limit");
        }
        long targetEnergy = checkedScale(maximumEnergy, controlSignal / 15D);
        long radiationLoss = ParticlePhysics.synchrotronLossKeV(input.meanEnergyKeV(), definition.massMeV(), radius);
        long outputEnergy = Math.max(0, subtractOrZero(targetEnergy, radiationLoss));
        double focusLoss = ParticlePhysics.focusLoss(input.amount(), definition.charge(), aggregates.beamLength(),
                config.beamAttenuation(), config.beamScaling());
        double focusGain = ParticlePhysics.focusGain(aggregates.quadrupoleField(), definition.charge());
        double outputFocus = Math.max(0, input.focus() - focusLoss + focusGain);
        return new ParticleStack(input.particleId(), input.amount(), outputEnergy, outputFocus);
    }

    public static long maximumEnergyKeV(ParticleDefinition definition,
                                        StructureRecord.StructureAggregates aggregates,
                                        SquareRingFootprint footprint) {
        if (definition == null || aggregates == null || footprint == null) return 0;
        if (definition.charge() == 0 || definition.massMeV() <= 0) return 0;
        return ParticlePhysics.ringMaximumEnergyKeV(definition.charge(), definition.massMeV(),
                aggregates.dipoleField(), radius(footprint), aggregates.voltage());
    }

    public static double radius(SquareRingFootprint footprint) {
        return Math.max(1D, (footprint.outerSide() - 4) / 2D);
    }

    public static long requiredEnergy(StructureRecord.StructureAggregates aggregates,
                                      ParticleMachinesConfig.Accelerator config) {
        return Math.addExact(config.baseEnergyRequirement(), aggregates.energyPerTick());
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
