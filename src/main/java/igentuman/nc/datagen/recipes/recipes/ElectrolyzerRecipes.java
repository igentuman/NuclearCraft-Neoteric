package igentuman.nc.datagen.recipes.recipes;

import igentuman.nc.content.processors.Processors;
import igentuman.nc.recipes.ingredient.creator.IngredientCreatorAccess;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;
import java.util.function.Consumer;

import static net.minecraft.world.level.material.Fluids.WATER;

public class ElectrolyzerRecipes extends AbstractRecipeProvider {

    public static void generate(Consumer<FinishedRecipe> consumer) {
        ElectrolyzerRecipes.consumer = consumer;
        ID = Processors.ELECTROLYZER;

        add(fluidStack(WATER, 500),
                List.of(
                        fluidStack("hydrogen", 500),
                        fluidStack("oxygen", 250)
                ), 0.5D
        );
        add(fluidStack("heavy_water", 500),
                List.of(
                        fluidStack("deuterium", 500),
                        fluidStack("oxygen", 250)
                ), 0.5D
        );
        add(fluidStack("hydrofluoric_acid", 250),
                List.of(
                        fluidStack("hydrogen", 250),
                        fluidStack("fluorine", 250)
                ), 0.5D
        );
    }

    protected static void add(FluidStack input, List<FluidStack> output, double...modifiers) {
        fluidsAndFluids(List.of(IngredientCreatorAccess.fluid().from(input)), output, modifiers);
    }
}
