package igentuman.nc.datagen.recipes.recipes;

import igentuman.nc.content.processors.Processors;
import igentuman.nc.recipes.ingredient.FluidStackIngredient;
import net.minecraft.data.IFinishedRecipe;
import net.minecraftforge.fluids.FluidStack;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class GasScrubberRecipes extends AbstractRecipeProvider {

    public static void generate(Consumer<IFinishedRecipe> consumer) {
        GasScrubberRecipes.consumer = consumer;
        ID = Processors.GAS_SCRUBBER;

        add(
                fluidIngredient("borax_solution", 250),
                fluidStack("irradiated_borax_solution", 250), 1, 5, -10
        );
    }

    protected static void add(FluidStackIngredient input, FluidStack output, double...modifiers) {
        fluidsAndFluids(Arrays.asList(input), Arrays.asList(output), modifiers);
    }
}
