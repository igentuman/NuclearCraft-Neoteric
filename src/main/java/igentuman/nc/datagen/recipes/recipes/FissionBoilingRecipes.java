package igentuman.nc.datagen.recipes.recipes;

import igentuman.nc.recipes.ingredient.FluidStackIngredient;
import net.minecraft.data.recipes.RecipeOutput;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.List;

public class FissionBoilingRecipes extends AbstractRecipeProvider {

    public static void generate(RecipeOutput consumer) {
        FissionBoilingRecipes.consumer = consumer;
        ID = "fission_boiling";

        add(
                fluidIngredient("minecraft:water", 10),
                fluidIngredient("steam", 10),
                1
        );

        add(
                fluidIngredient("technical_water", 10),
                fluidIngredient("high_pressure_steam", 10),
                1
        );
    }

    protected static void add(FluidStackIngredient input, FluidStackIngredient output, double heatRequired) {
        boilingRecipe(List.of(input), List.of(output), heatRequired);
    }
}
