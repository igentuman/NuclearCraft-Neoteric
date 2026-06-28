package igentuman.nc.datagen.recipe.processors;

import igentuman.nc.registration.MaterialEntry;
import net.minecraft.data.recipes.RecipeOutput;

import static igentuman.nc.datagen.recipe.ModRecipeProvider.*;
import static igentuman.nc.setup.entries.Processors.MELTER;

public class MelterRecipes {

    public static void melter(RecipeOutput out) {
        for (MaterialEntry mat : materials()) {
            if (!(mat.hasIngot() && mat.hasFluid())) continue;
            rec(out, MELTER, mat.name,
                    new I[]{i(mat.ingot().get(), 1)}, NF,
                    NI, new F[]{f(mat.materialFluid().source().get(), MOLTEN_INGOT)});
        }
    }
}
