package igentuman.nc.datagen.recipes.recipes;

import igentuman.nc.content.materials.Materials;
import igentuman.nc.content.processors.Processors;
import net.minecraft.data.recipes.FinishedRecipe;

import java.util.function.Consumer;

public class IrradiatorRecipes extends AbstractRecipeProvider {

    public static void generate(Consumer<FinishedRecipe> consumer) {
        IrradiatorRecipes.consumer = consumer;
        ID = Processors.IRRADIATOR;

        itemToItem(dustIngredient(Materials.thorium), dustStack(Materials.tbp), 1.5D);
        itemToItem(dustIngredient(Materials.tbp), dustStack(Materials.protactinium_233), 2.5D);
        itemToItem(dustIngredient(Materials.bismuth), dustStack(Materials.polonium), 2D);

    }
}
