package igentuman.nc.compat.jei;

import igentuman.nc.api.particle.ParticleDefinition;
import igentuman.nc.api.particle.ParticleStack;
import net.minecraft.resources.Identifier;

import static igentuman.nc.NuclearCraft.rl;

public class ParticleInfoRecipe {

    private final Identifier particleId;
    private final ParticleDefinition definition;
    private final ParticleStack stack;

    public ParticleInfoRecipe(Identifier particleId, ParticleDefinition definition) {
        this.particleId = particleId;
        this.definition = definition;
        this.stack = new ParticleStack(particleId, 1, 0, 0);
    }

    public Identifier getId() {
        return rl("particle_info_" + particleId.getPath());
    }

    public Identifier getParticleId() {
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
