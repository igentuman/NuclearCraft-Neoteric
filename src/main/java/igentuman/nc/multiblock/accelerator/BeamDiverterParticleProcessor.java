package igentuman.nc.multiblock.accelerator;

import igentuman.nc.api.particle.ParticleDefinition;
import igentuman.nc.api.particle.ParticleStack;
import igentuman.nc.config.ParticleMachinesConfig;
import igentuman.nc.multiblock.StructureRecord;
import igentuman.nc.particle.ParticlePhysics;
import net.minecraft.core.Direction;

public final class BeamDiverterParticleProcessor {

    private BeamDiverterParticleProcessor() {
    }

    public static ParticleStack route(ParticleStack input, ParticleDefinition definition,
                                      StructureRecord.StructureAggregates aggregates,
                                      ParticleMachinesConfig.Accelerator config,
                                      Direction inputFacing, Direction outputFacing) {
        if (input.isEmpty()) return ParticleStack.EMPTY;
        if (definition == null || aggregates == null || config == null
                || inputFacing == null || outputFacing == null) {
            throw new IllegalArgumentException("Definition, aggregates, config and port facings are required");
        }

        double focusLoss = ParticlePhysics.diverterStraightFocusLoss(input.amount(), definition.charge(),
                config.beamAttenuation(), config.beamScaling());
        long energy = input.meanEnergyKeV();
        if (isTurn(inputFacing, outputFacing)) {
            if (definition.charge() == 0 || definition.massMeV() <= 0) {
                throw new IllegalArgumentException("Magnetic turns require a charged, positive-mass particle");
            }
            double radius = ParticlePhysics.diverterTurnRadius(aggregates.dipoleField());
            long loss = ParticlePhysics.cornerEnergyLossKeV(energy, definition.charge(),
                    definition.massMeV(), radius);
            energy = loss >= energy ? 0 : energy - loss;
        }
        return new ParticleStack(input.particleId(), input.amount(), energy,
                Math.max(0, input.focus() - focusLoss));
    }

    public static boolean isTurn(Direction inputFacing, Direction outputFacing) {
        return inputFacing.getOpposite() != outputFacing;
    }

    public static long requiredEnergy(StructureRecord.StructureAggregates aggregates,
                                      ParticleMachinesConfig.Accelerator config) {
        return Math.addExact(config.baseEnergyRequirement(), aggregates.energyPerTick());
    }
}
