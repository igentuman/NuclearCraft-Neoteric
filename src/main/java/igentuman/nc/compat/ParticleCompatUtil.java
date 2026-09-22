package igentuman.nc.compat;

import igentuman.nc.api.particle.ParticleIngredient;
import igentuman.nc.api.particle.ParticleStack;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public final class ParticleCompatUtil {

    private ParticleCompatUtil() {
    }

    public static List<ParticleStack> stacksOf(ParticleIngredient ingredient) {
        List<ParticleStack> out = new ArrayList<>(ingredient.species().size());
        for (ResourceLocation species : ingredient.species()) {
            out.add(new ParticleStack(species, ingredient.amount(), ingredient.minimumEnergyKeV(), ingredient.minimumFocus()));
        }
        return out;
    }
}
