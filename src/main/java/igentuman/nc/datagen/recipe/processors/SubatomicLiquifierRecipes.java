package igentuman.nc.datagen.recipe.processors;

import net.minecraft.data.recipes.RecipeOutput;

import static igentuman.nc.datagen.recipe.ModRecipeProvider.*;
import static igentuman.nc.setup.entries.Processors.SUBATOMIC_LIQUIFIER;

public class SubatomicLiquifierRecipes {

    private static final String[] TYPES = {
            "potassium", "iron", "copper", "tin", "lead", "gold", "silver",
            "aluminum", "uranium", "thorium", "cobalt", "magnesium", "boron", "lithium"
    };

    public static void subatomicLiquifier(RecipeOutput out) {
        String id = SUBATOMIC_LIQUIFIER;
        for (String type : TYPES) {
            melt(out, id, type + "_dust",  dust(type),       1, fluidOf("subliquid_matter"), MOLTEN_INGOT * 5);
            melt(out, id, type + "_ingot", ingot(type),      1, fluidOf("subliquid_matter"), MOLTEN_INGOT * 5, 240);
            melt(out, id, type + "_block", blockItem(type),  1, fluidOf("subliquid_matter"), MOLTEN_INGOT * 45, 800);
        }
        f2f(out, id, "quantite", fluidOf("quantite"), MOLTEN_INGOT, fluidOf("subliquid_matter"), 10000, 1000);
    }
}
