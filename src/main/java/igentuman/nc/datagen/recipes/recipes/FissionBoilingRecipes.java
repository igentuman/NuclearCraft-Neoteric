package igentuman.nc.datagen.recipes.recipes;

import igentuman.nc.recipes.ingredient.FluidStackIngredient;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;
import java.util.function.Consumer;

public class FissionBoilingRecipes extends AbstractRecipeProvider {

    public static void generate(Consumer<FinishedRecipe> consumer) {
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
