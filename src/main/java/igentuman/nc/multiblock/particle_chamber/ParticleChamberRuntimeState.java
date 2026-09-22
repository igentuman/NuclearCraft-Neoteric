package igentuman.nc.multiblock.particle_chamber;

import igentuman.nc.particle.ParticleReactionPlan;
import igentuman.nc.particle.ParticleStorage;

import java.util.List;

public record ParticleChamberRuntimeState(
        List<ParticleStorage> inputs,
        List<ParticleStorage> pendingOutputs,
        ParticleReactionPlan activeReaction,
        double fractionalWork,
        List<Double> fractionalYields,
        boolean enabled
) {
}
