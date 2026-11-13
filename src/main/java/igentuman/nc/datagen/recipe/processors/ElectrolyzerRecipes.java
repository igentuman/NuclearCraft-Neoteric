package igentuman.nc.datagen.recipe.processors;

import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.level.material.Fluids;

import static igentuman.nc.datagen.recipe.ModRecipeProvider.*;
import static igentuman.nc.setup.entries.Processors.ELECTROLYZER;

/** Generates electrolyzer recipes that split fluids into their constituent gases. */
public class ElectrolyzerRecipes {

    public static void electrolyzer(RecipeOutput out) {
        split(out, ELECTROLYZER, "water",             Fluids.WATER,             500, new F[]{fluid("hydrogen", 500), fluid("oxygen", 250)}, 100);
        split(out, ELECTROLYZER, "heavy_water",       fluidOf("heavy_water"),   500, new F[]{fluid("deuterium", 500), fluid("oxygen", 250)}, 100);
        split(out, ELECTROLYZER, "hydrofluoric_acid", fluidOf("hydrofluoric_acid"), 250, new F[]{fluid("hydrogen", 250), fluid("fluorine", 250)}, 100);
    }
}
