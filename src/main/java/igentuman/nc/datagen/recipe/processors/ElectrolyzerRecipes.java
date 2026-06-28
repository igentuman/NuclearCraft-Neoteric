package igentuman.nc.datagen.recipe.processors;

import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.level.material.Fluids;

import static igentuman.nc.datagen.recipe.ModRecipeProvider.*;
import static igentuman.nc.setup.entries.Processors.ELECTROLYZER;

public class ElectrolyzerRecipes {

    public static void electrolyzer(RecipeOutput out) {
        split(out, ELECTROLYZER, "water",             Fluids.WATER,             500, fl("hydrogen", 500), fl("oxygen", 250));
        split(out, ELECTROLYZER, "hydrofluoric_acid", fluidOf("hydrofluoric_acid"), 250, fl("hydrogen", 250), fl("fluorine", 250));
    }
}
