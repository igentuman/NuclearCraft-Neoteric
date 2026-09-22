package igentuman.nc.recipe.particle;

import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Map;

public record ParticleRecipeIndex<T>(Map<ResourceLocation, List<T>> recipesByInputSpecies) {
}
