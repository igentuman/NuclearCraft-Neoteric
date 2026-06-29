package igentuman.nc.datagen.recipe.processors;

import net.minecraft.data.recipes.RecipeOutput;

import static igentuman.nc.datagen.recipe.ModRecipeProvider.*;
import static igentuman.nc.setup.entries.Processors.EXTRACTOR;

public class ExtractorRecipes {

    public static void extractor(RecipeOutput out) {
        rec(out, EXTRACTOR, "cocoa",
                new I[]{prt("ground_cocoa_nibs", 1)}, NF,
                new I[]{prt("cocoa_solids", 1)}, new F[]{f(fluidOf("cocoa_butter"), MOLTEN_INGOT)});
    }
}
