package igentuman.nc.datagen.recipe.processors;

import igentuman.nc.registration.MaterialEntry;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.Items;

import static igentuman.nc.datagen.recipe.ModRecipeProvider.*;
import static igentuman.nc.setup.entries.Processors.MANUFACTORY;

/** Generates manufactory grinding recipes turning ingots, ores, and blocks into dusts and byproducts. */
public class ManufactoryRecipes {

    public static void manufactory(RecipeOutput out) {
        String id = MANUFACTORY;
        for (MaterialEntry mat : materials()) {
            if (mat.hasIngot() && mat.hasDust()) {
                grind(out, id, mat.name, mat.ingot().get(), mat.dust().get(), 1);
                if (mat.hasRawOre()) {
                    grind(out, id, mat.name + "_raw", mat.rawOre().get(), mat.dust().get(), 2);
                }
            } else if (mat.hasGem() && mat.hasDust()) {
                if (mat.name.equals("villiaumite") || mat.name.equals("carobbiite")) continue;
                grind(out, id, mat.name, mat.gem().get(), mat.dust().get(), 1, 300);
            }
        }

        grind(out, id, "raw_iron",   Items.RAW_IRON,   dust("iron"),   2);
        grind(out, id, "raw_copper", Items.RAW_COPPER, dust("copper"), 2);
        grind(out, id, "raw_gold",   Items.RAW_GOLD,   dust("gold"),   2);
        grind(out, id, "netherite_scrap", Items.NETHERITE_SCRAP, dust("netherite"), 2);

        grind(out, id, "coal",          Items.COAL,     dust("coal"),     1, 100);
        grind(out, id, "coal_graphite", dust("coal"),   dust("graphite"), 1, 100);
        grind(out, id, "charcoal",      Items.CHARCOAL, dust("charcoal"), 1, 100, 25);
        grind(out, id, "diamond",       Items.DIAMOND,  dust("diamond"),  1, 300, 75);
        grind(out, id, "emerald",       Items.EMERALD,  dust("emerald"),  1, 240, 60);
        grind(out, id, "ender_pearl",   Items.ENDER_PEARL, dust("enderium"), 1, 240, 25);
        grind(out, id, "quartz",        Items.QUARTZ,   dust("quartz"),   1);
        grind(out, id, "lapis",         Items.LAPIS_LAZULI, dust("lapis"), 1);
        grind(out, id, "obsidian",      Items.OBSIDIAN, dust("obsidian"), 1, 400);
        grind(out, id, "end_stone",     Items.END_STONE, dust("end_stone"), 1);
        grind(out, id, "glowstone",     Items.GLOWSTONE, Items.GLOWSTONE_DUST, 4, 100, 25);
        grind(out, id, "cobblestone",   Items.COBBLESTONE, Items.SAND,  1);
        grind(out, id, "gravel",        Items.GRAVEL,      Items.FLINT, 1);
        grind(out, id, "blaze_rod",     Items.BLAZE_ROD,   Items.BLAZE_POWDER, 4);
        grind(out, id, "bone",          Items.BONE,        Items.BONE_MEAL,    6);

        grind(out, id, "iron_ingot",      Items.IRON_INGOT,      dust("iron"),      1, 240);
        grind(out, id, "copper_ingot",    Items.COPPER_INGOT,    dust("copper"),    1, 240);
        grind(out, id, "gold_ingot",      Items.GOLD_INGOT,      dust("gold"),      1, 240);
        grind(out, id, "netherite_ingot", Items.NETHERITE_INGOT, dust("netherite"), 1, 500);

        grind(out, id, "villiaumite", gem("villiaumite"), dust("sodium_fluoride"),    1);
        grind(out, id, "carobbiite",  gem("carobbiite"),  dust("potassium_fluoride"), 1);

        grind(out, id, "silicon_boule", part("silicon_boule"), part("silicon_wafer"), 2);
        grind(out, id, "roasted_cocoa_beans", part("roasted_cocoa_beans"), part("ground_cocoa_nibs"), 1, 100, 25);

        rec(out, id, "rotten_flesh", new I[]{van(Items.ROTTEN_FLESH, 4)}, NF, new I[]{van(Items.LEATHER, 1)}, NF, 100);
        rec(out, id, "sugar_cane",   new I[]{van(Items.SUGAR_CANE, 2)},   NF, new I[]{prt("bioplastic", 1)}, NF, 200, 25);
        rec(out, id, "porkchop",     new I[]{van(Items.PORKCHOP, 1)},     NF, new I[]{prt("gelatin", 8)},    NF, 100, 25);
        rec(out, id, "salmon",       new I[]{van(Items.SALMON, 1)},       NF, new I[]{prt("gelatin", 2)},    NF, 100, 25);
        rec(out, id, "cod",          new I[]{van(Items.COD, 1)},          NF, new I[]{prt("gelatin", 2)},    NF, 100, 25);
        rec(out, id, "wheat_seeds",  new I[]{van(Items.WHEAT_SEEDS, 1)},  NF, new I[]{prt("flour", 2)},      NF, 100, 25);
    }
}
