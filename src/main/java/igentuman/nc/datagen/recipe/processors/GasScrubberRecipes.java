package igentuman.nc.datagen.recipe.processors;

import net.minecraft.data.recipes.RecipeOutput;

import static igentuman.nc.datagen.recipe.ModRecipeProvider.*;
import static igentuman.nc.setup.entries.Processors.GAS_SCRUBBER;

/** Generates gas scrubber recipes that transform one fluid solution into another. */
public class GasScrubberRecipes {

    public static void gasScrubber(RecipeOutput out) {
        f2f(out, GAS_SCRUBBER, "borax_solution", fluidOf("borax_solution"), 250, fluidOf("irradiated_borax_solution"), 250, 200, 250);
    }
}
