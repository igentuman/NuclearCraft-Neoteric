package igentuman.nc.api.particle;

import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Set;

public record ParticleDefinition(
        double massMeV,
        double charge,
        double spin,
        Set<Interaction> interactions,
        ResourceLocation antiparticleId,
        List<Component> composition,
        ResourceLocation textureId,
        String translationKey
) {

    public enum Interaction {
        ELECTROMAGNETIC,
        STRONG,
        WEAK,
        GRAVITY
    }

    public record Component(ResourceLocation particleId, int count) {
    }
}
