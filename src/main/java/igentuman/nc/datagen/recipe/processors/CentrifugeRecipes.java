package igentuman.nc.datagen.recipe.processors;

import net.minecraft.data.recipes.RecipeOutput;

import static igentuman.nc.datagen.recipe.ModRecipeProvider.*;
import static igentuman.nc.setup.entries.Processors.CENTRIFUGE;

public class CentrifugeRecipes {

    public static void centrifuge(RecipeOutput out) {
        split(out, CENTRIFUGE, "carbon_dioxide", fluidOf("carbon_dioxide"), 1000,
                fl("carbon_dioxide", 125), fl("carbon_monoxide", 125), fl("carbon", 375), fl("oxygen", 375));
    }
}
