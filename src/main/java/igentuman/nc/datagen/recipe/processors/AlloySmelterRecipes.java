package igentuman.nc.datagen.recipe.processors;

import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.Items;

import static igentuman.nc.datagen.recipe.ModRecipeProvider.*;
import static igentuman.nc.setup.entries.Processors.ALLOY_SMELTER;

public class AlloySmelterRecipes {

    public static void alloySmelter(RecipeOutput out) {
        String id = ALLOY_SMELTER;
        alloy(out, id, "netherite",             Items.NETHERITE_INGOT,    1, Items.NETHERITE_SCRAP, 3, dust("gold"),      3);
        alloy(out, id, "nichrome",              ingot("nichrome"),         5, dust("iron"),          1, dust("chromium"),  4);
        alloy(out, id, "osmiridium",            ingot("osmiridium"),       4, dust("osmium"),        3, dust("iridium"),   1);
        alloy(out, id, "sic_sic_cmc",           ingot("sic_sic_cmc"),     12, dust("carbon_manganese"), 1, dust("titanium"), 11);
        alloy(out, id, "niobium_titanium",      ingot("niobium_titanium"), 1, dust("niobium"),       1, dust("titanium"),  1);
        alloy(out, id, "niobium_tin",           ingot("niobium_tin"),      3, dust("niobium"),       2, dust("tin"),       1);
        alloy(out, id, "electrum",              ingot("electrum"),         2, dust("gold"),          1, dust("silver"),    1);
        alloy(out, id, "stainless_steel",       ingot("stainless_steel"),  2, dust("steel"),         1, dust("chromium"),  1);
        alloy(out, id, "thermoconducting",      ingot("thermoconducting"), 2, dust("extreme"),       1, dust("boron_arsenide"), 1);
        alloy(out, id, "steel",                 ingot("steel"),            1, dust("coal"),          3, dust("iron"),      1);
        alloy(out, id, "bronze",                ingot("bronze"),           4, dust("copper"),        3, dust("tin"),       1);
        alloy(out, id, "ferroboron",            ingot("ferroboron"),       2, dust("boron"),         1, dust("steel"),     1);
        alloy(out, id, "hard_carbon",           ingot("hard_carbon"),      2, dust("graphite"),      1, dust("diamond"),   1);
        alloy(out, id, "tough_alloy",           ingot("tough_alloy"),      2, dust("ferroboron"),    1, dust("lithium"),   1);
        alloy(out, id, "magnesium_diboride",    ingot("magnesium_diboride"), 3, dust("magnesium"),   1, dust("boron"),     2);
        alloy(out, id, "lithium_manganese_dioxide", ingot("lithium_manganese_dioxide"), 2, dust("lithium"), 1, dust("manganese_dioxide"), 1);
        alloy(out, id, "shibuichi",             ingot("shibuichi"),        4, dust("copper"),        3, dust("silver"),    1);
        alloy(out, id, "tin_silver",            ingot("tin_silver"),       4, dust("tin"),           3, dust("silver"),    1);
        alloy(out, id, "lead_platinum",         ingot("lead_platinum"),    4, dust("lead"),          3, dust("platinum"),  1);
        alloy(out, id, "extreme",               ingot("extreme"),          2, dust("tough_alloy"),   1, dust("hard_carbon"), 1);
        alloy(out, id, "silicon_carbide",       ingot("silicon_carbide"),  2, gem("silicon"),        1, dust("graphite"),  1);
        alloy(out, id, "carbon_manganese",      ingot("carbon_manganese"), 2, dust("manganese"),     1, dust("graphite"),  1);
        alloy(out, id, "zircaloy",              ingot("zircaloy"),         8, dust("zirconium"),     7, dust("tin"),       1);
        alloy(out, id, "hsla_steel",            ingot("hsla_steel"),      16, dust("iron"),         15, dust("carbon_manganese"), 1);
        alloy(out, id, "zirconium_molybdenum",  ingot("zirconium_molybdenum"), 16, dust("molybdenum"), 15, dust("zirconium"), 1);
    }
}
