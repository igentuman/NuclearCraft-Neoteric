package igentuman.nc.datagen.recipes.recipes;

import igentuman.nc.content.processors.Processors;
import igentuman.nc.recipes.ingredient.FluidStackIngredient;
import net.minecraft.data.recipes.RecipeOutput;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.List;

public class GasScrubberRecipes extends AbstractRecipeProvider {

    public static void generate(RecipeOutput consumer) {
        GasScrubberRecipes.consumer = consumer;
        ID = Processors.GAS_SCRUBBER;

        add(
                fluidIngredient("borax_solution", 250),
                fluidIngredient("irradiated_borax_solution", 250), 1, 5, -10000
        );
    }

    protected static void add(FluidStackIngredient input, FluidStackIngredient output, double...modifiers) {
        fluidsAndFluids(List.of(input), List.of(output), modifiers);
    }
}
