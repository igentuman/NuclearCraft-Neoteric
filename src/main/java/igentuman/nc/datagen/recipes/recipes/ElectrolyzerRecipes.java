package igentuman.nc.datagen.recipes.recipes;

import igentuman.nc.content.processors.Processors;
import igentuman.nc.recipes.ingredient.FluidStackIngredient;
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

        add(fluidIngredient("minecraft:water", 500),
                List.of(
                        fluidIngredient("hydrogen", 500),
                        fluidIngredient("oxygen", 250)
                ), 0.5D
        );
        add(fluidIngredient("heavy_water", 500),
                List.of(
                        fluidIngredient("deuterium", 500),
                        fluidIngredient("oxygen", 250)
                ), 0.5D
        );
        add(fluidIngredient("hydrofluoric_acid", 250),
                List.of(
                        fluidIngredient("hydrogen", 250),
                        fluidIngredient("fluorine", 250)
                ), 0.5D
        );
    }

    protected static void add(FluidStackIngredient input, List<FluidStackIngredient> output, double...modifiers) {
        fluidsAndFluids(List.of(input), output, modifiers);
    }
}
