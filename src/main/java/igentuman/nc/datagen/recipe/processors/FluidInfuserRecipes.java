package igentuman.nc.datagen.recipe.processors;

import igentuman.nc.registration.FissionFuelEntry;
import igentuman.nc.registration.IsotopeEntry;
import igentuman.nc.setup.ModEntries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;

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
        for (var e : ModEntries.FISSION_FUEL.entrySet()) {
            FissionFuelEntry fe = e.getValue();
            if (fe.base().isSpecial()) continue;
            for (String[] g : GASES) {
                infuse(out, fe.idStem.replace("fuel_", "") + g[1], fluidOf(g[0]), 1000,
                        fuel(e.getKey(), ""), 1, fuel(e.getKey(), g[1]), 1);
            }
        }
        infuse(out, "carbon_manganese",        fluidOf("carbon"), 1000, dust("manganese"),     1, dust("carbon_manganese"),   1);
        infuse(out, "manganese_dioxide_ingot", fluidOf("oxygen"), 1000, ingot("manganese_oxide"), 1, ingot("manganese_dioxide"), 1);
        infuse(out, "manganese_dioxide_dust",  fluidOf("oxygen"), 1000, dust("manganese_oxide"),  1, dust("manganese_dioxide"),  1);

        infuse(out, "supercold_ice", fluidOf("liquid_helium"), 50, Items.ICE, 1, blockItem("supercold_ice"), 1);

        infuse(out, "cryotheum_heat_sink",       fluidOf("cryotheum"),       1000, part("empty_heat_sink"), 1, part("cryotheum_heat_sink"),       1);
        infuse(out, "liquid_helium_heat_sink",   fluidOf("liquid_helium"),   1000, part("empty_heat_sink"), 1, part("liquid_helium_heat_sink"),   1);
        infuse(out, "liquid_nitrogen_heat_sink", fluidOf("liquid_nitrogen"), 1000, part("empty_heat_sink"), 1, part("liquid_nitrogen_heat_sink"), 1);
        infuse(out, "water_heat_sink",           Fluids.WATER,               1000, part("empty_heat_sink"), 1, part("water_heat_sink"),           1);

        infuse(out, "active_cryotheum_heat_sink",       fluidOf("cryotheum"),       1000, part("empty_active_heat_sink"), 1, part("active_cryotheum_heat_sink"),       1);
        infuse(out, "active_liquid_helium_heat_sink",   fluidOf("liquid_helium"),   1000, part("empty_active_heat_sink"), 1, part("active_liquid_helium_heat_sink"),   1);
        infuse(out, "active_liquid_nitrogen_heat_sink", fluidOf("liquid_nitrogen"), 1000, part("empty_active_heat_sink"), 1, part("active_liquid_nitrogen_heat_sink"), 1);
        infuse(out, "active_water_heat_sink",           Fluids.WATER,               1000, part("empty_active_heat_sink"), 1, part("active_water_heat_sink"),           1);

        infuse(out, "water_cooler",           Fluids.WATER,               1000, part("empty_cooler"), 1, part("water_cooler"),           1);
        infuse(out, "liquid_helium_cooler",   fluidOf("liquid_helium"),   1000, part("empty_cooler"), 1, part("liquid_helium_cooler"),   1);
        infuse(out, "liquid_nitrogen_cooler", fluidOf("liquid_nitrogen"), 1000, part("empty_cooler"), 1, part("liquid_nitrogen_cooler"), 1);
        infuse(out, "cryotheum_cooler",       fluidOf("cryotheum"),       1000, part("empty_cooler"), 1, part("cryotheum_cooler"),       1);
        infuse(out, "enderium_cooler",        fluidOf("enderium"),        576,  part("empty_cooler"), 1, part("enderium_cooler"),        1);

        infuse(out, "radaway",      fluidOf("radaway"),      250, part("bioplastic"), 2, part("radaway"),      1);
        infuse(out, "radaway_slow", fluidOf("radaway_slow"), 250, part("bioplastic"), 2, part("radaway_slow"), 1);
    }
}
