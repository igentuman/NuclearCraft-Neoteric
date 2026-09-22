package igentuman.nc.registration;

import igentuman.nc.api.particle.ParticleDefinition;
import net.neoforged.neoforge.registries.DeferredHolder;

public record ParticleEntry(
        String name,
        DeferredHolder<ParticleDefinition, ParticleDefinition> definition
) {
}
