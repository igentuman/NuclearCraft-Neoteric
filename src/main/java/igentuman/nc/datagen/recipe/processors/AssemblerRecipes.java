package igentuman.nc.datagen.recipe.processors;

import igentuman.nc.registration.FissionFuelEntry;
import igentuman.nc.setup.ModEntries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.Items;

import static igentuman.nc.datagen.recipe.ModRecipeProvider.*;

/** Generates assembler recipes that build processors, circuits, collectors, and other multi-part components. */
public class AssemblerRecipes {

    public static void assembler(RecipeOutput out) {
        assemble(out, "basic_processor",       part("basic_processor"),       1,
                new I[]{prt("silicon_n_doped", 1), van(Items.REDSTONE, 4), van(Items.GOLD_INGOT, 1), ing("silver", 1)});
        assemble(out, "advanced_processor",    part("advanced_processor"),    1,
                new I[]{dst("hafnium", 1), prt("basic_processor", 1), van(Items.REDSTONE, 4), prt("silicon_p_doped", 1)});
        assemble(out, "elite_processor",       part("elite_processor"),       1,
                new I[]{dst("hafnium", 1), prt("coil_bscco", 4), prt("advanced_processor", 1), ing("platinum", 1)});
        assemble(out, "crystal_binder",        dust("crystal_binder"),        2,
                new I[]{dst("rhodochrosite", 1), dst("calcium_sulfate", 1), dst("magnesium", 1), dst("obsidian", 1)});
        assemble(out, "spaxelhoe_tough",       part("spaxelhoe_tough"),       1,
                new I[]{plt("tough_alloy", 5), van(Items.IRON_INGOT, 2)});
        assemble(out, "multitool",             part("multitool"),             1,
                new I[]{plt("steel", 3), dst("lead", 1), prt("motor", 1), prt("actuator", 1)});
        assemble(out, "basic_electric_circuit", part("basic_electric_circuit"), 1,
                new I[]{plt("electrum", 1), prt("bioplastic", 1), dst("energetic_blend", 1), van(Items.REDSTONE, 1), prt("coil_copper", 1)});
        assemble(out, "energetic_blend",       dust("energetic_blend"),       2,
                new I[]{van(Items.GLOWSTONE_DUST, 1), dst("quartz", 1), van(Items.REDSTONE, 1), dst("emerald", 1)});
        assemble(out, "coil_bscco",            part("coil_bscco"),            3,
                new I[]{dst("bscco", 3), ing("silver", 6)});
        assemble(out, "bscco",                 dust("bscco"),                 3,
                new I[]{dst("bismuth", 2), dst("strontium", 2), dst("calcium", 2), dst("copper", 2)});

        assemble(out, "upgrade_stack",         part("upgrade_stack"),         1,
                new I[]{prt("upgrade_speed", 2), plt("cobalt", 1), dst("uranium", 1)});
        assemble(out, "dense_water_collector", part("dense_water_collector"), 1,
                new I[]{prt("compact_water_collector", 4), van(Items.CONDUIT, 1), plt("platinum", 4), prt("motor", 1)});
        assemble(out, "dense_nitrogen_collector", part("dense_nitrogen_collector"), 1,
                new I[]{prt("compact_nitrogen_collector", 4), plt("beryllium", 1), plt("netherite", 4), prt("motor", 1)});
        assemble(out, "dense_helium_collector", part("dense_helium_collector"), 1,
                new I[]{prt("compact_helium_collector", 4), plt("zinc", 1), plt("cobalt", 4), prt("motor", 1)});
        assemble(out, "q36",                   part("q36_quantite_disruptor"), 1,
                new I[]{prt("basic_processor", 2), prt("basic_electric_circuit", 1), van(Items.IRON_BARS, 8), prt("coil_bscco", 4)});
        assemble(out, "qnp",                   part("qnp"),                   1,
                new I[]{plt("hsla_steel", 3), prt("basic_electric_circuit", 1), prt("lithium_ion_cell", 1), prt("coil_magnesium_diboride", 1), prt("actuator", 1)});

        for (var e : ModEntries.FISSION_FUEL.entrySet()) {
            FissionFuelEntry fe = e.getValue();
            if (fe.base().isSpecial()) continue;
            assemble(out, fe.idStem.replace("fuel_", "") + "_tr", fuel(e.getKey(), "_tr"), 9,
                    new I[]{i(fuel(e.getKey(), ""), 9), dst("graphite", 1), ing("pyrolitic_carbon", 1), ing("silicon_carbide", 1)});
        }
    }
}
