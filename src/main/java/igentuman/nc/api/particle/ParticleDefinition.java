package igentuman.nc.api.particle;

import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

import java.util.List;
import java.util.Set;

import static igentuman.nc.NuclearCraft.MODID;

/** Properties of a particle species registered under {@link #REGISTRY_KEY}. */
public record ParticleDefinition(
        double massMeV,
        double charge,
        double spin,
        Set<Interaction> interactions,
        Identifier antiparticleId,
        List<Component> composition,
        Identifier textureId,
        String translationKey
) {

    /** Addons can use this key with their own deferred register and namespace. */
    public static final ResourceKey<Registry<ParticleDefinition>> REGISTRY_KEY =
            ResourceKey.createRegistryKey(Identifier.fromNamespaceAndPath(MODID, "particle"));

    public enum Interaction {
        ELECTROMAGNETIC,
        STRONG,
        WEAK,
        GRAVITY
    }

    public record Component(Identifier particleId, int count) {
    }
}
