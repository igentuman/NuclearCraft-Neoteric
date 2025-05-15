package igentuman.nc.compat.jei;

import igentuman.nc.content.particles.Particle;
import igentuman.nc.content.particles.ParticleStack;
import mezz.jei.api.ingredients.IIngredientType;
import net.minecraft.resources.ResourceLocation;

/**
 * source https://github.com/Lach01298/QMD
 */
public class ParticleRecipe {
    private final ResourceLocation id;
    private final String name;
    public ParticleStack output;

    public ParticleRecipe(ResourceLocation id,  Particle particle) {
        this.id = id;
        this.name = "nuclearcraft.particle." + particle.getName() +".name";
        this.output = new ParticleStack(particle, 0, 0);
    }
    
    public ResourceLocation getId() {
        return id;
    }


    public ParticleStack getIngredient() {
        return output;
    }

    public String getName() {
        return name;
    }

    public double getMass() {
        return output.getParticle().getMass();
    }

    public double getCharge() {
        return output.getParticle().getCharge();
    }

    public double getSpin() {
        return output.getParticle().getSpin();
    }

    public boolean interactsWithStrong() {
        return output.getParticle().interactsWithStrong();
    }

    public boolean interactsWithWeak() {
        return output.getParticle().interactsWithWeak();
    }
}