package igentuman.nc.compat.jei;

import igentuman.nc.content.particles.Particle;
import igentuman.nc.content.particles.ParticleStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

import static igentuman.nc.NuclearCraft.rl;

public class ParticleSourceRecipe {
    private final ResourceLocation id;
    private final String name;
    public ParticleStack output;
    public ItemStack item;
    public FluidStack fluid;

    public ParticleSourceRecipe(ResourceLocation id, ItemStack item, FluidStack fluid, Particle particle) {
        this.id = rl("/"+id.getPath());
        this.name = "nuclearcraft.particle." + particle.getName() +".name";
        this.output = new ParticleStack(particle, 0, 0);
        this.item = item;
        this.fluid = fluid;
    }
    
    public ResourceLocation getId() {
        return id;
    }

    public ParticleStack getParticle() {
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