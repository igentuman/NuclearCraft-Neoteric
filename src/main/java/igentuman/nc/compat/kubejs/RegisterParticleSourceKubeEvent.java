package igentuman.nc.compat.kubejs;

import igentuman.nc.particle.ParticleSourceData;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

public record RegisterParticleSourceKubeEvent(
        Map<ResourceLocation, ParticleSourceData> itemSources,
        Map<ResourceLocation, ParticleSourceData> fluidSources
) {
}
