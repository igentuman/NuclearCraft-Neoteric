package igentuman.nc.particle;

import igentuman.nc.api.particle.ParticleDefinition;
import igentuman.nc.setup.Registers;
import net.minecraft.resources.Identifier;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ParticleCatalog {

    private final Map<Identifier, ParticleDefinition> definitions;

    public ParticleCatalog(Map<Identifier, ParticleDefinition> definitions) {
        this.definitions = definitions;
    }

    public static ParticleCatalog fromRegistry() {
        Map<Identifier, ParticleDefinition> definitions = new LinkedHashMap<>();
        for (ParticleDefinition definition : Registers.PARTICLE_DEFINITION_REGISTRY) {
            definitions.put(Registers.PARTICLE_DEFINITION_REGISTRY.getKey(definition), definition);
        }
        return new ParticleCatalog(definitions);
    }

    public Map<Identifier, ParticleDefinition> definitions() {
        return definitions;
    }
}
