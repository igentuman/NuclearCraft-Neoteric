package igentuman.nc.datagen.recipe.processors;

import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.Items;

import static igentuman.nc.datagen.recipe.ModRecipeProvider.*;
import static igentuman.nc.setup.entries.Processors.ALLOY_SMELTER;

public class AlloySmelterRecipes {

    public static void alloySmelter(RecipeOutput out) {
        String id = ALLOY_SMELTER;
        alloy(out, id, "netherite",             Items.NETHERITE_INGOT,    1, Items.NETHERITE_SCRAP, 3, dust("gold"),      3, 400);
        alloy(out, id, "nichrome",              ingot("nichrome"),         5, dust("iron"),          1, dust("chromium"),  4, 400);
        alloy(out, id, "osmiridium",            ingot("osmiridium"),       4, dust("osmium"),        3, dust("iridium"),   1, 400);
        alloy(out, id, "sic_sic_cmc",           ingot("sic_sic_cmc"),     12, dust("carbon_manganese"), 1, dust("titanium"), 11, 800);
        alloy(out, id, "niobium_titanium",      ingot("niobium_titanium"), 1, dust("niobium"),       1, dust("titanium"),  1, 800);
        alloy(out, id, "niobium_tin",           ingot("niobium_tin"),      3, dust("niobium"),       2, dust("tin"),       1, 600);
        alloy(out, id, "electrum",              ingot("electrum"),         2, dust("gold"),          1, dust("silver"),    1, 400);
        alloy(out, id, "stainless_steel",       ingot("stainless_steel"),  2, dust("steel"),         1, dust("chromium"),  1, 600);
        alloy(out, id, "thermoconducting",      ingot("thermoconducting"), 2, dust("extreme"),       1, dust("boron_arsenide"), 1, 400);
        alloy(out, id, "steel",                 ingot("steel"),            1, dust("coal"),          3, dust("iron"),      1);
        alloy(out, id, "bronze",                ingot("bronze"),           4, dust("copper"),        3, dust("tin"),       1);
        alloy(out, id, "ferroboron",            ingot("ferroboron"),       2, dust("boron"),         1, dust("steel"),     1);
        alloy(out, id, "hard_carbon",           ingot("hard_carbon"),      2, dust("graphite"),      1, dust("diamond"),   1, 200, 75);
        alloy(out, id, "tough_alloy",           ingot("tough_alloy"),      2, dust("ferroboron"),    1, dust("lithium"),   1, 300, 75);
        alloy(out, id, "magnesium_diboride",    ingot("magnesium_diboride"), 3, dust("magnesium"),   1, dust("boron"),     2);
        alloy(out, id, "lithium_manganese_dioxide", ingot("lithium_manganese_dioxide"), 2, dust("lithium"), 1, dust("manganese_dioxide"), 1, 300);
        alloy(out, id, "shibuichi",             ingot("shibuichi"),        4, dust("copper"),        3, dust("silver"),    1, 300);
        alloy(out, id, "tin_silver",            ingot("tin_silver"),       4, dust("tin"),           3, dust("silver"),    1);
        alloy(out, id, "lead_platinum",         ingot("lead_platinum"),    4, dust("lead"),          3, dust("platinum"),  1, 300);
        alloy(out, id, "extreme",               ingot("extreme"),          2, dust("tough_alloy"),   1, dust("hard_carbon"), 1);
        alloy(out, id, "silicon_carbide",       ingot("silicon_carbide"),  2, gem("silicon"),        1, dust("graphite"),  1, 400, 100);
        alloy(out, id, "carbon_manganese",      ingot("carbon_manganese"), 2, dust("manganese"),     1, dust("graphite"),  1, 1000, 250);
        alloy(out, id, "zircaloy",              ingot("zircaloy"),         8, dust("zirconium"),     7, dust("tin"),       1);
        alloy(out, id, "hsla_steel",            ingot("hsla_steel"),      16, dust("iron"),         15, dust("carbon_manganese"), 1, 1600, 100);
        alloy(out, id, "zirconium_molybdenum",  ingot("zirconium_molybdenum"), 16, dust("molybdenum"), 15, dust("zirconium"), 1, 1600, 100);

        alloy(out, id, "nichrome_ingots",          ingot("nichrome"),          5, Items.IRON_INGOT,         1, ingot("chromium"),  4, 500);
        alloy(out, id, "osmiridium_ingots",        ingot("osmiridium"),        4, ingot("osmium"),          3, ingot("iridium"),   1, 800);
        alloy(out, id, "sic_sic_cmc_ingots",       ingot("sic_sic_cmc"),      12, ingot("carbon_manganese"), 1, ingot("titanium"), 11, 1400);
        alloy(out, id, "niobium_titanium_ingots",  ingot("niobium_titanium"),  1, ingot("niobium"),         1, ingot("titanium"),  1, 1000);
        alloy(out, id, "niobium_tin_ingots",       ingot("niobium_tin"),       3, ingot("niobium"),         2, ingot("tin"),       1, 800);
        alloy(out, id, "electrum_ingots",          ingot("electrum"),          2, Items.GOLD_INGOT,         1, ingot("silver"),    1, 600);
        alloy(out, id, "stainless_steel_ingots",   ingot("stainless_steel"),   2, ingot("steel"),           1, ingot("chromium"),  1, 800);
        alloy(out, id, "thermoconducting_ingots",  ingot("thermoconducting"),  2, ingot("extreme"),         1, gem("boron_arsenide"), 1, 400, 75);
        alloy(out, id, "steel_ingots",             ingot("steel"),             1, dust("coal"),             3, Items.IRON_INGOT,   1, 300);
        alloy(out, id, "bronze_ingots",            ingot("bronze"),            4, Items.COPPER_INGOT,       3, ingot("tin"),       1, 300);
        alloy(out, id, "ferroboron_ingots",        ingot("ferroboron"),        2, ingot("boron"),           1, ingot("steel"),     1, 300);
        alloy(out, id, "tough_alloy_ingots",       ingot("tough_alloy"),       2, ingot("ferroboron"),      1, ingot("lithium"),   1, 340, 75);
        alloy(out, id, "magnesium_diboride_ingots", ingot("magnesium_diboride"), 3, ingot("magnesium"),     1, ingot("boron"),     2, 300);
        alloy(out, id, "lithium_manganese_dioxide_ingots", ingot("lithium_manganese_dioxide"), 2, ingot("lithium"), 1, ingot("manganese_dioxide"), 1, 360);
        alloy(out, id, "shibuichi_ingots",         ingot("shibuichi"),         4, Items.COPPER_INGOT,       3, ingot("silver"),    1, 360);
        alloy(out, id, "tin_silver_ingots",        ingot("tin_silver"),        4, ingot("tin"),             3, ingot("silver"),    1, 300);
        alloy(out, id, "lead_platinum_ingots",     ingot("lead_platinum"),     4, ingot("lead"),            3, ingot("platinum"),  1, 300);
        alloy(out, id, "extreme_ingots",           ingot("extreme"),           2, ingot("tough_alloy"),     1, ingot("hard_carbon"), 1, 300);
        alloy(out, id, "zircaloy_ingots",          ingot("zircaloy"),          8, ingot("zirconium"),       7, ingot("tin"),       1, 300);
        alloy(out, id, "hsla_steel_ingots",        ingot("hsla_steel"),       16, Items.IRON_INGOT,        15, ingot("carbon_manganese"), 1, 1700, 125);
    }
}
