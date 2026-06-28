package igentuman.nc.datagen.recipe.processors;

import igentuman.nc.registration.MaterialEntry;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.Item;

import static igentuman.nc.datagen.recipe.ModRecipeProvider.*;
import static igentuman.nc.setup.entries.Processors.PRESSURIZER;

public class PressurizerRecipes {

    public static void pressurizer(RecipeOutput out) {
        for (MaterialEntry mat : materials()) {
            if (!mat.hasPlate()) continue;
            Item input = mat.hasIngot() ? mat.ingot().get() : (mat.hasDust() ? mat.dust().get() : null);
            if (input == null) continue;
            rec(out, PRESSURIZER, mat.name,
                    new I[]{i(input, 1)}, NF, new I[]{i(mat.plate().get(), 1)}, NF);
        }
    }
}
