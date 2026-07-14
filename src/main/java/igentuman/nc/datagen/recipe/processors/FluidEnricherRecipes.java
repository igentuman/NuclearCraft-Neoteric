package igentuman.nc.datagen.recipe.processors;

import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;

import static igentuman.nc.datagen.recipe.ModRecipeProvider.*;

/** Generates fluid enricher recipes that combine a fluid with an item into an enriched fluid. */
public class FluidEnricherRecipes {

    public static void fluidEnricher(RecipeOutput out) {
        enrich(out, "redstone_ethanol",  fluidOf("ethanol"),             250,  van(Items.REDSTONE, 1),      fluidOf("redstone_ethanol"), 250);
        enrich(out, "potassium_iodide",  fluidOf("potassium_hydroxide"), 4000, dst("iodine", 1),            fluidOf("potassium_iodide"), MOLTEN_INGOT);
        enrich(out, "technical_water",   Fluids.WATER,                   5000, dst("coal", 1),              fluidOf("technical_water"),  5000);
        enrich(out, "chlorine",          Fluids.WATER,                   500,  prt("salt", 3),              fluidOf("chlorine"),         500, 400, 100);
        enrich(out, "uranium_oxide",     fluidOf("oxygen"),              MOLTEN_INGOT, dst("uranium", 1),   fluidOf("uranium_oxide"),    MOLTEN_INGOT * 3);
        enrich(out, "boron_nitride_solution", Fluids.WATER,              1000, dst("boron_nitride", 1),     fluidOf("boron_nitride_solution"), 250);
        enrich(out, "fluorite_water",    Fluids.WATER,                   1000, dst("fluorite", 1),          fluidOf("fluorite_water"),   250);
        enrich(out, "calcium_sulfate_solution", Fluids.WATER,            1000, dst("calcium_sulfate", 1),   fluidOf("calcium_sulfate_solution"), 250);
        enrich(out, "sodium_fluoride_solution", Fluids.WATER,            1000, dst("sodium_fluoride", 1),   fluidOf("sodium_fluoride_solution"), 250);
        enrich(out, "potassium_fluoride_solution", Fluids.WATER,         1000, dst("potassium_fluoride", 1), fluidOf("potassium_fluoride_solution"), 250);
        enrich(out, "sodium_hydroxide_solution", Fluids.WATER,           1000, dst("sodium_hydroxide", 1),  fluidOf("sodium_hydroxide_solution"), 250);
        enrich(out, "borax_solution",    Fluids.WATER,                   1000, dst("borax", 1),             fluidOf("borax_solution"),   250);
        enrich(out, "irradiated_borax_solution", Fluids.WATER,           1000, dst("irradiated_borax", 1),  fluidOf("irradiated_borax_solution"), 250);
        enrich(out, "radaway",           fluidOf("ethanol"),             250,  prt("mushroom", 3),          fluidOf("radaway"),          250);
        enrich(out, "radaway_slow",      fluidOf("redstone_ethanol"),    250,  prt("mushroom", 3),          fluidOf("radaway_slow"),     250);
        enrich(out, "baratol",           fluidOf("tnt"),                 200,  dst("barium_nitrate", 1),    fluidOf("baratol"),          200);
    }
}
