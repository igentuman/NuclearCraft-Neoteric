package igentuman.nc.recipe.particle;

import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Map;

public record ParticleRecipeIndex<T>(Map<Identifier, List<T>> recipesByInputSpecies) {
}
