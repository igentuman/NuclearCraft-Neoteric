package igentuman.nc.datagen.recipe.particle;

import net.minecraft.resources.ResourceLocation;

public record ParticleRecipeBuilder<T>(ResourceLocation id, T recipe) {
}
