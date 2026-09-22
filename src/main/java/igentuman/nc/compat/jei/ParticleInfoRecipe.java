package igentuman.nc.compat.jei;

import igentuman.nc.api.particle.ParticleDefinition;
import igentuman.nc.api.particle.ParticleStack;
import net.minecraft.resources.ResourceLocation;

import static igentuman.nc.NuclearCraft.rl;

public class ParticleInfoRecipe {

    private final ResourceLocation particleId;
    private final ParticleDefinition definition;
    private final ParticleStack stack;

    public ParticleInfoRecipe(ResourceLocation particleId, ParticleDefinition definition) {
        this.particleId = particleId;
        this.definition = definition;
        this.stack = new ParticleStack(particleId, 1, 0, 0);
    }

    public ResourceLocation getId() {
        return rl("particle_info_" + particleId.getPath());
    }

    public ResourceLocation getParticleId() {
        return particleId;
    }

    public ParticleDefinition getDefinition() {
        return definition;
    }

    public ParticleStack getStack() {
        return stack;
    }

    public boolean interactsWithStrong() {
        return definition.interactions().contains(ParticleDefinition.Interaction.STRONG);
    }

    public boolean interactsWithWeak() {
        return definition.interactions().contains(ParticleDefinition.Interaction.WEAK);
    }
}
