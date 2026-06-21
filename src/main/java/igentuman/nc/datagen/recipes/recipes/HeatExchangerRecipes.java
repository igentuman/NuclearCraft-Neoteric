package igentuman.nc.datagen.recipes.recipes;

import net.minecraft.data.recipes.FinishedRecipe;

import java.util.List;
import java.util.function.Consumer;

public class HeatExchangerRecipes extends AbstractRecipeProvider {

    public static void generate(Consumer<FinishedRecipe> consumer) {
        HeatExchangerRecipes.consumer = consumer;
        ID = "heat_exchanger_controller";

        hxRecipe(
                List.of(fluidIngredient("flibe_hot_molten_salt", 1)),
                List.of(fluidIngredient("flibe_molten_salt", 1)),
                1D, 1D, 600D
        );

        hxRecipe(
                List.of(fluidIngredient("hot_helium", 1)),
                List.of(fluidIngredient("helium", 1)),
                1D, 1D, 400D
        );

        hxRecipe(
                List.of(fluidIngredient("technical_water", 1)),
                List.of(fluidIngredient("high_pressure_steam", 1)),
                1D, 1D, 300D
        );

        hxRecipe(
                List.of(fluidIngredient("low_quality_steam", 1)),
                List.of(fluidIngredient("technical_water", 1)),
                1D, 1D, 100D
        );

        hxRecipe(
                List.of(fluidIngredient("minecraft:water", 1)),
                List.of(fluidIngredient("steam", 1)),
                1D, 1D, -250D
        );

        hxRecipe(
                List.of(fluidIngredient("exhaust_steam", 1)),
                List.of(fluidIngredient("minecraft:water", 1)),
                1D, 1D, 100D
        );
    }
}
