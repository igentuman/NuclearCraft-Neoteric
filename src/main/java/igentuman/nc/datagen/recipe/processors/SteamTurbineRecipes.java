package igentuman.nc.datagen.recipe.processors;

import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.level.material.Fluids;

import static igentuman.nc.datagen.recipe.ModRecipeProvider.*;
import static igentuman.nc.setup.entries.Processors.STEAM_TURBINE;

/** Generates steam turbine recipes that convert steam into water and exhaust steam. */
public class SteamTurbineRecipes {

    public static void steamTurbine(RecipeOutput out) {
        f2f(out, STEAM_TURBINE, "steam",               fluidOf("steam"),               100, Fluids.WATER,          100, 20);
        f2f(out, STEAM_TURBINE, "high_pressure_steam",  fluidOf("high_pressure_steam"), 100, fluidOf("exhaust_steam"), 100, 40, 55);
    }
}
