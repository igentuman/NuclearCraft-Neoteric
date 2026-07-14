package igentuman.nc.datagen.recipe.processors;

import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.level.material.Fluids;

import static igentuman.nc.datagen.recipe.ModRecipeProvider.*;
import static igentuman.nc.setup.entries.Processors.ELECTROLYZER;

/** Generates electrolyzer recipes that split fluids into their constituent gases. */
public class ElectrolyzerRecipes {

    public static void electrolyzer(RecipeOutput out) {
        split(out, ELECTROLYZER, "water",             Fluids.WATER,             500, new F[]{fl("hydrogen", 500), fl("oxygen", 250)}, 100);
        split(out, ELECTROLYZER, "heavy_water",       fluidOf("heavy_water"),   500, new F[]{fl("deuterium", 500), fl("oxygen", 250)}, 100);
        split(out, ELECTROLYZER, "hydrofluoric_acid", fluidOf("hydrofluoric_acid"), 250, new F[]{fl("hydrogen", 250), fl("fluorine", 250)}, 100);
    }
}
