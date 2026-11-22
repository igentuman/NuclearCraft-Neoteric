package igentuman.nc.datagen.recipe.particle;

import net.minecraft.resources.Identifier;

public record ParticleRecipeBuilder<T>(Identifier id, T recipe) {
}
