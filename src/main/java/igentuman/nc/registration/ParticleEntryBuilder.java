package igentuman.nc.registration;

import igentuman.nc.api.particle.ParticleDefinition;
import igentuman.nc.setup.ModEntries;
import igentuman.nc.setup.Registers;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class ParticleEntryBuilder {

    private final String name;
    private ParticleDefinition definition;

    private ParticleEntryBuilder(String name) {
        this.name = name;
    }

    public static ParticleEntryBuilder add(String name) {
        return new ParticleEntryBuilder(name);
    }

    public ParticleEntryBuilder definition(ParticleDefinition definition) {
        this.definition = definition;
        return this;
    }

    public String name() {
        return name;
    }

    public ParticleDefinition definition() {
        return definition;
    }

    public ParticleEntry build() {
        if (definition == null) {
            throw new IllegalStateException("Particle entry '" + name + "' has no definition");
        }
        DeferredHolder<ParticleDefinition, ParticleDefinition> holder =
                Registers.PARTICLE_DEFINITIONS.register(name, () -> definition);
        ParticleEntry entry = new ParticleEntry(name, holder);
        ModEntries.PARTICLE_ENTRIES.put(name, entry);
        return entry;
    }
}
