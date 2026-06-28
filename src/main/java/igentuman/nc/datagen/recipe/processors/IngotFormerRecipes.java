package igentuman.nc.datagen.recipe.processors;

import igentuman.nc.registration.MaterialEntry;
import net.minecraft.data.recipes.RecipeOutput;

import static igentuman.nc.datagen.recipe.ModRecipeProvider.*;
import static igentuman.nc.setup.entries.Processors.INGOT_FORMER;

public class IngotFormerRecipes {

    public static void ingotFormer(RecipeOutput out) {
        for (MaterialEntry mat : materials()) {
            if (!(mat.hasIngot() && mat.hasFluid())) continue;
            rec(out, INGOT_FORMER, mat.name,
                    NI, new F[]{f(mat.materialFluid().source().get(), MOLTEN_INGOT)},
                    new I[]{i(mat.ingot().get(), 1)}, NF);
        }
    }
}
