package igentuman.nc.datagen.recipe.processors;

import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.Items;

import static igentuman.nc.datagen.recipe.ModRecipeProvider.*;

public class CrystallizerRecipes {

    public static void crystallizer(RecipeOutput out) {
        crystal(out, "lapis",            fluidOf("lapis"),            90, van(Items.LAPIS_LAZULI, 1));
        crystal(out, "sulfur",           fluidOf("sulfur"),           90, dst("sulfur", 1));
        crystal(out, "potassium_iodide", fluidOf("potassium_iodide"), 90, dst("potassium_iodide", 1));
    }
}
