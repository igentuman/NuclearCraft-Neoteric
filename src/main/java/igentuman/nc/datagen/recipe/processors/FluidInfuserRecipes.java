package igentuman.nc.datagen.recipe.processors;

import igentuman.nc.registration.FuelEntry;
import igentuman.nc.registration.IsotopeEntry;
import igentuman.nc.setup.ModEntries;
import net.minecraft.data.recipes.RecipeOutput;

import static igentuman.nc.datagen.recipe.ModRecipeProvider.*;

public class FluidInfuserRecipes {

    private static final String[][] GASES = {{"oxygen", "_ox"}, {"nitrogen", "_ni"}, {"zircaloy", "_za"}};

    public static void fluidInfuser(RecipeOutput out) {
        for (var e : ModEntries.ISOTOPES.entrySet()) {
            IsotopeEntry iso = e.getValue();
            for (String[] g : GASES) {
                var var = isotopeVar(iso, g[1]);
                if (var == null) continue;
                infuse(out, iso.itemId + g[1], fluidOf(g[0]), 100, iso.base().get(), 1, var, 1);
            }
        }
        for (var e : ModEntries.FUELS.entrySet()) {
            FuelEntry fe = e.getValue();
            if (fe.base().isSpecial()) continue;
            for (String[] g : GASES) {
                infuse(out, fe.idStem.replace("fuel_", "") + g[1], fluidOf(g[0]), 1000,
                        fuel(e.getKey(), ""), 1, fuel(e.getKey(), g[1]), 1);
            }
        }
        infuse(out, "carbon_manganese",        fluidOf("carbon"), 1000, dust("manganese"),     1, dust("carbon_manganese"),   1);
        infuse(out, "manganese_dioxide_ingot", fluidOf("oxygen"), 1000, ingot("manganese_oxide"), 1, ingot("manganese_dioxide"), 1);
        infuse(out, "manganese_dioxide_dust",  fluidOf("oxygen"), 1000, dust("manganese_oxide"),  1, dust("manganese_dioxide"),  1);
    }
}
