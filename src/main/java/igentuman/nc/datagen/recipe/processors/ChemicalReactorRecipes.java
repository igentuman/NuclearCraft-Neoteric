package igentuman.nc.datagen.recipe.processors;

import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.level.material.Fluids;

import static igentuman.nc.datagen.recipe.ModRecipeProvider.*;
import static igentuman.nc.setup.entries.Processors.CHEMICAL_REACTOR;

public class ChemicalReactorRecipes {

    public static void chemicalReactor(RecipeOutput out) {
        gasReactions(out);
        acidChain(out);
        moltenAlloys(out);
        oxidationReductions(out);
    }

    private static void gasReactions(RecipeOutput out) {
        chem(out, "ammonia_synthesis",    fluidOf("nitrogen"),  250, fluidOf("hydrogen"),  750, fl("ammonia",        750));
        chem(out, "ammonia_oxidation",    fluidOf("ammonia"),   350, fluidOf("oxygen"),     650, fl("nitric_oxide",   750), fl("water",         250));
        chem(out, "no_oxidation",         fluidOf("nitric_oxide"), 500, fluidOf("oxygen"),  250, fl("nitrogen_dioxide", 750));
        chem(out, "water_synthesis",      fluidOf("hydrogen"),  500, fluidOf("oxygen"),     250, fl("water",          500));
        chem(out, "boron_hydride",        fluidOf("boron"),     MOLTEN_INGOT, fluidOf("hydrogen"), 666, fl("diborane", 500));
        chem(out, "diborane_hydrolysis",  fluidOf("diborane"),  250, Fluids.WATER,          750, fl("boric_acid",     500), fl("hydrogen",      500));
        chem(out, "o2f2_hydrolysis",      fluidOf("oxygen_difluoride"), 250, Fluids.WATER,  250, fl("oxygen",         250), fl("hydrofluoric_acid", 250));
        chem(out, "carbon_shift",         fluidOf("carbon_dioxide"), 250, fluidOf("hydrogen"), 250, fl("carbon_monoxide", 250), fl("water",        250));
    }

    private static void acidChain(RecipeOutput out) {
        chem(out, "nitric_acid",          fluidOf("nitrogen_dioxide"), 750, Fluids.WATER,   250, fl("nitric_acid",    1000));
        chem(out, "aqua_regia",           fluidOf("nitric_acid"),  250, fluidOf("hydrochloric_acid"), 750, fl("aqua_regia_acid", 1000));
        chem(out, "sulfuric_acid_1",      fluidOf("sulfur_dioxide"), 500, fluidOf("oxygen"), 250, fl("sulfur_trioxide", 500));
        chem(out, "sulfuric_acid_2",      fluidOf("sulfur_trioxide"), 250, Fluids.WATER,    250, fl("sulfuric_acid",  250));
    }

    private static void oxidationReductions(RecipeOutput out) {
        chem(out, "sulfur_dioxide",       fluidOf("sulfur"), MOLTEN_INGOT / 2, fluidOf("oxygen"), 500, fl("sulfur_dioxide", 500));
        chem(out, "manganese_reduction",  fluidOf("manganese_dioxide"), MOLTEN_INGOT / 2, fluidOf("carbon"), MOLTEN_INGOT,
                fl("manganese", MOLTEN_INGOT / 2), fl("carbon_monoxide", 750));
        chem(out, "arsenic_boride",       fluidOf("arsenic"), 333, fluidOf("boron"), MOLTEN_INGOT / 2, fl("boron_arsenide", MOLTEN_INGOT * 2));
    }

    private static void moltenAlloys(RecipeOutput out) {
        chem(out, "electrum_molten",      fluidOf("gold"),     MOLTEN_INGOT, fluidOf("silver"),    MOLTEN_INGOT, fl("electrum",    MOLTEN_INGOT * 2));
        chem(out, "thermoconducting_molten", fluidOf("extreme"), MOLTEN_INGOT, fluidOf("boron_arsenide"), MOLTEN_INGOT, fl("thermoconducting", MOLTEN_INGOT * 2));
        chem(out, "bronze_molten",        fluidOf("copper"),   MOLTEN_INGOT * 3, fluidOf("tin"),   MOLTEN_INGOT, fl("bronze",      MOLTEN_INGOT * 4));
        chem(out, "ferroboron_molten",    fluidOf("boron"),    MOLTEN_INGOT, fluidOf("steel"),     MOLTEN_INGOT, fl("ferroboron",  MOLTEN_INGOT * 2));
        chem(out, "tough_alloy_molten",   fluidOf("ferroboron"), MOLTEN_INGOT, fluidOf("lithium"), MOLTEN_INGOT, fl("tough_alloy", MOLTEN_INGOT * 2));
        chem(out, "magnesium_diboride_molten", fluidOf("magnesium"), MOLTEN_INGOT, fluidOf("boron"), MOLTEN_INGOT * 2, fl("magnesium_diboride", MOLTEN_INGOT * 3));
        chem(out, "lithium_manganese_dioxide_molten", fluidOf("lithium"), MOLTEN_INGOT, fluidOf("manganese_dioxide"), MOLTEN_INGOT, fl("lithium_manganese_dioxide", MOLTEN_INGOT * 2));
        chem(out, "shibuichi_molten",     fluidOf("copper"),   MOLTEN_INGOT * 3, fluidOf("silver"), MOLTEN_INGOT, fl("shibuichi",  MOLTEN_INGOT * 4));
        chem(out, "tin_silver_molten",    fluidOf("tin"),      MOLTEN_INGOT * 3, fluidOf("silver"), MOLTEN_INGOT, fl("tin_silver", MOLTEN_INGOT * 4));
        chem(out, "lead_platinum_molten", fluidOf("lead"),     MOLTEN_INGOT * 3, fluidOf("platinum"), MOLTEN_INGOT, fl("lead_platinum", MOLTEN_INGOT * 4));
        chem(out, "extreme_molten",       fluidOf("tough_alloy"), MOLTEN_INGOT, fluidOf("hard_carbon"), MOLTEN_INGOT, fl("extreme", MOLTEN_INGOT * 2));
        chem(out, "zircaloy_molten",      fluidOf("zirconium"), MOLTEN_INGOT * 7, fluidOf("tin"), MOLTEN_INGOT, fl("zircaloy",   MOLTEN_INGOT * 8));
        chem(out, "hsla_steel_molten",    fluidOf("iron"),     MOLTEN_INGOT * 15, fluidOf("carbon_manganese"), MOLTEN_INGOT, fl("hsla_steel", MOLTEN_INGOT * 16));
        chem(out, "zirconium_molybdenum_molten", fluidOf("molybdenum"), MOLTEN_INGOT * 15, fluidOf("zirconium"), MOLTEN_INGOT, fl("zirconium_molybdenum", MOLTEN_INGOT * 16));
        chem(out, "sic_sic_cmc_molten",   fluidOf("carbon_manganese"), MOLTEN_INGOT, fluidOf("titanium"), MOLTEN_INGOT * 11, fl("sic_sic_cmc", MOLTEN_INGOT * 12));
    }
}
