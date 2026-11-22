package igentuman.nc.compat.kubejs;

import dev.latvian.mods.kubejs.event.KubeEvent;
import igentuman.nc.api.content.ContentRegistration;
import igentuman.nc.particle.ParticleSourceCatalog;
import igentuman.nc.api.particle.ParticleSourceData;
import net.minecraft.resources.Identifier;

import static igentuman.nc.NuclearCraft.rl;

public class RegisterParticleSourceKubeEvent implements KubeEvent {

    public void item(String item, String particle, long capacity) {
        item(item, particle, capacity, ParticleSourceCatalog.DEFAULT_ENERGY_KEV, ParticleSourceCatalog.DEFAULT_FOCUS);
    }

    public void item(String item, String particle, long capacity, long energyKeV) {
        item(item, particle, capacity, energyKeV, ParticleSourceCatalog.DEFAULT_FOCUS);
    }

    public void item(String item, String particle, long capacity, long energyKeV, double focus) {
        ContentRegistration.registerItemParticleSource(Identifier.parse(item), source(particle, capacity, energyKeV, focus));
    }

    public void fluid(String fluid, String particle) {
        fluid(fluid, particle, ParticleSourceCatalog.FLUID_PARTICLES_PER_MILLIBUCKET);
    }

    public void fluid(String fluid, String particle, long particlesPerMillibucket) {
        fluid(fluid, particle, particlesPerMillibucket, ParticleSourceCatalog.DEFAULT_ENERGY_KEV, ParticleSourceCatalog.DEFAULT_FOCUS);
    }

    public void fluid(String fluid, String particle, long particlesPerMillibucket, long energyKeV, double focus) {
        ContentRegistration.registerFluidParticleSource(Identifier.parse(fluid),
                source(particle, particlesPerMillibucket, energyKeV, focus));
    }

    public void removeItem(String item) {
        ContentRegistration.removeItemParticleSource(Identifier.parse(item));
    }

    public void removeFluid(String fluid) {
        ContentRegistration.removeFluidParticleSource(Identifier.parse(fluid));
    }

    private static ParticleSourceData source(String particle, long capacity, long energyKeV, double focus) {
        Identifier particleId = particle.indexOf(':') >= 0 ? Identifier.parse(particle) : rl(particle);
        return ParticleSourceData.fullCharge(particleId, capacity, energyKeV, focus);
    }
}
