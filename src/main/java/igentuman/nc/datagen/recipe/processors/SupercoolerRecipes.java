package igentuman.nc.datagen.recipe.processors;

import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.level.material.Fluids;

import static igentuman.nc.datagen.recipe.ModRecipeProvider.*;
import static igentuman.nc.setup.entries.Processors.SUPERCOOLER;

/** Generates supercooler recipes that chill gases and fluids into liquefied or cooled forms. */
public class SupercoolerRecipes {

    public static void supercooler(RecipeOutput out) {
        String id = SUPERCOOLER;
        for (String gas : new String[]{"oxygen", "hydrogen", "nitrogen", "helium"}) {
            f2f(out, id, "liquid_" + gas, fluidOf(gas), 1000, fluidOf("liquid_" + gas), 100);
        }
        f2f(out, id, "cryotheum",         fluidOf("slurry_ice"),        1000, fluidOf("cryotheum"),         1000);
        f2f(out, id, "ice",               Fluids.WATER,                 1000, fluidOf("ice"),               500);
        f2f(out, id, "condensate_water",  fluidOf("exhaust_steam"),     1000, fluidOf("condensate_water"),  1000);
        f2f(out, id, "technical_water",   fluidOf("condensate_water"),  1000, fluidOf("technical_water"),   1000);
        f2f(out, id, "emergency_coolant", fluidOf("emergency_coolant_heated"), 1000, fluidOf("emergency_coolant"), 1000);
    }
}
