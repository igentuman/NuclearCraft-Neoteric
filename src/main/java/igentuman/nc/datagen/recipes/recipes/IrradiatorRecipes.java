package igentuman.nc.datagen.recipes.recipes;

import igentuman.nc.content.materials.Materials;
import igentuman.nc.content.processors.Processors;
import igentuman.nc.recipes.ingredient.NcIngredient;
import net.minecraft.data.IFinishedRecipe;
import net.minecraftforge.common.Tags;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import static net.minecraft.block.Blocks.GLOWSTONE;


public class IrradiatorRecipes extends AbstractRecipeProvider {

    public static void generate(Consumer<IFinishedRecipe> consumer) {
        IrradiatorRecipes.consumer = consumer;
        ID = Processors.IRRADIATOR;

        itemToItem(dustIngredient(Materials.thorium), dustStack(Materials.tbp), 1.5D);
        itemToItem(ingredient(Tags.Items.SAND, 1), NcIngredient.stack(stack(GLOWSTONE, 1)), 3D);
        itemToItem(dustIngredient(Materials.tbp), dustStack(Materials.protactinium_233), 2.5D);
        itemToItem(dustIngredient(Materials.bismuth), dustStack(Materials.polonium), 2D);
        fluidsAndFluids(Arrays.asList(fluidIngredient("lithium", 500)), Arrays.asList(fluidStack("irradiated_lithium", 500)), 1.5D);
        fluidsAndFluids(Arrays.asList(fluidIngredient("boron", 500)), Arrays.asList(fluidStack("irradiated_boron", 500)), 2.5D);
        fluidsAndFluids(Arrays.asList(fluidIngredient(Materials.uranium238, 100)), Arrays.asList(fluidStack(Materials.uranium235, 100)), 5.5D);
    }
}
