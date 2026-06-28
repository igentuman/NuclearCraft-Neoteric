package igentuman.nc.datagen.recipe.processors;

import igentuman.nc.registration.MaterialEntry;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.Items;

import static igentuman.nc.datagen.recipe.ModRecipeProvider.*;
import static igentuman.nc.setup.entries.Processors.MANUFACTORY;

public class ManufactoryRecipes {

    public static void manufactory(RecipeOutput out) {
        String id = MANUFACTORY;
        for (MaterialEntry mat : materials()) {
            if (mat.hasIngot() && mat.hasDust()) {
                grind(out, id, mat.name, mat.ingot().get(), mat.dust().get(), 1);
            } else if (mat.hasGem() && mat.hasDust()) {
                grind(out, id, mat.name, mat.gem().get(), mat.dust().get(), 1);
            }
        }
        grind(out, id, "raw_iron",   Items.RAW_IRON,   dust("iron"),   2);
        grind(out, id, "raw_copper", Items.RAW_COPPER, dust("copper"), 2);
        grind(out, id, "raw_gold",   Items.RAW_GOLD,   dust("gold"),   2);
        grind(out, id, "netherite_scrap", Items.NETHERITE_SCRAP, dust("netherite"), 2);
        grind(out, id, "coal",      Items.COAL,        dust("coal"),      1);
        grind(out, id, "charcoal",  Items.CHARCOAL,    dust("charcoal"),  1);
        grind(out, id, "diamond",   Items.DIAMOND,     dust("diamond"),   1);
        grind(out, id, "emerald",   Items.EMERALD,     dust("emerald"),   1);
        grind(out, id, "quartz",    Items.QUARTZ,      dust("quartz"),    1);
        grind(out, id, "lapis",     Items.LAPIS_LAZULI, dust("lapis"),    1);
        grind(out, id, "obsidian",  Items.OBSIDIAN,    dust("obsidian"),  1);
        grind(out, id, "end_stone", Items.END_STONE,   dust("end_stone"), 1);
        grind(out, id, "cobblestone", Items.COBBLESTONE, Items.SAND,  1);
        grind(out, id, "gravel",      Items.GRAVEL,      Items.FLINT, 1);
        grind(out, id, "silicon_boule", part("silicon_boule"), part("silicon_wafer"), 2);
    }

}
