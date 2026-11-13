package igentuman.nc.datagen.recipe.processors;

import igentuman.nc.datagen.recipe.ModRecipeProvider;
import igentuman.nc.registration.FissionFuelEntry;
import igentuman.nc.setup.ModEntries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.Items;

import static igentuman.nc.datagen.recipe.ModRecipeProvider.*;

/** Generates assembler recipes that build processors, circuits, collectors, and other multi-part components. */
public class AssemblerRecipes {

    public static void assembler(RecipeOutput out) {
        assemble(out, "basic_processor",       part("basic_processor"),       1,
                new I[]{ModRecipeProvider.part("silicon_n_doped", 1), van(Items.REDSTONE, 4), van(Items.GOLD_INGOT, 1), ModRecipeProvider.ingot("silver", 1)});
        assemble(out, "advanced_processor",    part("advanced_processor"),    1,
                new I[]{ModRecipeProvider.dust("hafnium", 1), ModRecipeProvider.part("basic_processor", 1), van(Items.REDSTONE, 4), ModRecipeProvider.part("silicon_p_doped", 1)});
        assemble(out, "elite_processor",       part("elite_processor"),       1,
                new I[]{ModRecipeProvider.dust("hafnium", 1), ModRecipeProvider.part("coil_bscco", 4), ModRecipeProvider.part("advanced_processor", 1), ModRecipeProvider.ingot("platinum", 1)});
        assemble(out, "crystal_binder",        dust("crystal_binder"),        2,
                new I[]{ModRecipeProvider.dust("rhodochrosite", 1), ModRecipeProvider.dust("calcium_sulfate", 1), ModRecipeProvider.dust("magnesium", 1), ModRecipeProvider.dust("obsidian", 1)});
        assemble(out, "spaxelhoe_tough",       part("spaxelhoe_tough"),       1,
                new I[]{ModRecipeProvider.plate("tough_alloy", 5), van(Items.IRON_INGOT, 2)});
        assemble(out, "multitool",             part("multitool"),             1,
                new I[]{ModRecipeProvider.plate("steel", 3), ModRecipeProvider.dust("lead", 1), ModRecipeProvider.part("motor", 1), ModRecipeProvider.part("actuator", 1)});
        assemble(out, "basic_electric_circuit", part("basic_electric_circuit"), 1,
                new I[]{ModRecipeProvider.plate("electrum", 1), ModRecipeProvider.part("bioplastic", 1), ModRecipeProvider.dust("energetic_blend", 1), van(Items.REDSTONE, 1), ModRecipeProvider.part("coil_copper", 1)});
        assemble(out, "energetic_blend",       dust("energetic_blend"),       2,
                new I[]{van(Items.GLOWSTONE_DUST, 1), ModRecipeProvider.dust("quartz", 1), van(Items.REDSTONE, 1), ModRecipeProvider.dust("emerald", 1)});
        assemble(out, "coil_bscco",            part("coil_bscco"),            3,
                new I[]{ModRecipeProvider.dust("bscco", 3), ModRecipeProvider.ingot("silver", 6)});
        assemble(out, "bscco",                 dust("bscco"),                 3,
                new I[]{ModRecipeProvider.dust("bismuth", 2), ModRecipeProvider.dust("strontium", 2), ModRecipeProvider.dust("calcium", 2), ModRecipeProvider.dust("copper", 2)});

        assemble(out, "stack_upgrade",         part("stack_upgrade"),         1,
                new I[]{ModRecipeProvider.part("speed_upgrade", 2), ModRecipeProvider.plate("cobalt", 1), ModRecipeProvider.dust("uranium", 1)});
        assemble(out, "dense_water_collector", part("dense_water_collector"), 1,
                new I[]{ModRecipeProvider.part("compact_water_collector", 4), van(Items.CONDUIT, 1), ModRecipeProvider.plate("platinum", 4), ModRecipeProvider.part("motor", 1)});
        assemble(out, "dense_nitrogen_collector", part("dense_nitrogen_collector"), 1,
                new I[]{ModRecipeProvider.part("compact_nitrogen_collector", 4), ModRecipeProvider.plate("beryllium", 1), ModRecipeProvider.plate("netherite", 4), ModRecipeProvider.part("motor", 1)});
        assemble(out, "dense_helium_collector", part("dense_helium_collector"), 1,
                new I[]{ModRecipeProvider.part("compact_helium_collector", 4), ModRecipeProvider.plate("zinc", 1), ModRecipeProvider.plate("cobalt", 4), ModRecipeProvider.part("motor", 1)});
        assemble(out, "q36",                   part("q36_quantite_disruptor"), 1,
                new I[]{ModRecipeProvider.part("basic_processor", 2), ModRecipeProvider.part("basic_electric_circuit", 1), van(Items.IRON_BARS, 8), ModRecipeProvider.part("coil_bscco", 4)});
        assemble(out, "qnp",                   part("qnp"),                   1,
                new I[]{ModRecipeProvider.plate("hsla_steel", 3), ModRecipeProvider.part("basic_electric_circuit", 1), ModRecipeProvider.part("lithium_ion_cell", 1), ModRecipeProvider.part("coil_magnesium_diboride", 1), ModRecipeProvider.part("actuator", 1)});

        for (var e : ModEntries.FISSION_FUEL.entrySet()) {
            FissionFuelEntry fe = e.getValue();
            if (fe.base().isSpecial()) continue;
            assemble(out, fe.idStem.replace("fuel_", "") + "_tr", fuel(e.getKey(), "_tr"), 9,
                    new I[]{i(fuel(e.getKey(), ""), 9), ModRecipeProvider.dust("graphite", 1), ModRecipeProvider.ingot("pyrolitic_carbon", 1), ModRecipeProvider.ingot("silicon_carbide", 1)});
        }
    }
}
