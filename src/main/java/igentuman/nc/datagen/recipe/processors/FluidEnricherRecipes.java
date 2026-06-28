package igentuman.nc.datagen.recipe.processors;

import net.minecraft.data.recipes.RecipeOutput;

import static igentuman.nc.datagen.recipe.ModRecipeProvider.*;

public class FluidEnricherRecipes {

    public static void fluidEnricher(RecipeOutput out) {
        enrich(out, "potassium_iodide", fluidOf("potassium_hydroxide"), 4000, dst("iodine", 1), fluidOf("potassium_iodide"), 90);
        enrich(out, "baratol",          fluidOf("tnt"),                  200,  dst("barium_nitrate", 1), fluidOf("baratol"), 200);
    }
}
