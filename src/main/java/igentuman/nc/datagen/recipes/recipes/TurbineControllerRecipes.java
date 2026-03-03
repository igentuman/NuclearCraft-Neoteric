package igentuman.nc.datagen.recipes.recipes;

import igentuman.nc.block.turbine.entity.TurbineControllerBE;
import igentuman.nc.recipes.ingredient.FluidStackIngredient;
import net.minecraft.data.recipes.RecipeOutput;

import java.util.List;

public class TurbineControllerRecipes extends AbstractRecipeProvider {

    public static void generate(RecipeOutput consumer) {
        TurbineControllerRecipes.consumer = consumer;
        ID = TurbineControllerBE.NAME;

        add(
                fluidIngredient("steam", 1000),
                fluidIngredient("minecraft:water", 1000),
                1D, 0.75D
        );

        add(
                fluidIngredient("high_pressure_steam", 1000),
                fluidIngredient("exhaust_steam", 1000),
                1D, 1D
        );

        add(
                fluidIngredient("exhaust_steam", 5000),
                fluidIngredient("technical_water", 5000),
                1D, 0.1D
        );

    }

    protected static void add(FluidStackIngredient input, FluidStackIngredient output, double...modifiers) {
        fluidsAndFluids(List.of(input), List.of(output), modifiers);
    }
}
