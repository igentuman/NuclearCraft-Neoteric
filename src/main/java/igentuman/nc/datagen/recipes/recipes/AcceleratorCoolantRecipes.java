package igentuman.nc.datagen.recipes.recipes;

import igentuman.nc.recipes.ingredient.FluidStackIngredient;
import net.minecraft.data.recipes.FinishedRecipe;

import java.util.List;
import java.util.function.Consumer;

public class AcceleratorCoolantRecipes extends AbstractRecipeProvider {

    public static void generate(Consumer<FinishedRecipe> consumer) {
        AcceleratorCoolantRecipes.consumer = consumer;
        ID = "accelerator_coolant";

        for(String gas: List.of("nitrogen", "helium")) {
            add(
                    fluidIngredient("liquid_" + gas, 10),
                    fluidIngredient(gas, 10),
                    25
            );
        }

        add(
                fluidIngredient("minecraft:water", 1000),
                fluidIngredient("steam", 1000),
                20
        );

        add(
                fluidIngredient("technical_water", 1000),
                fluidIngredient("high_pressure_steam", 1000),
                25
        );
    }

    protected static void add(FluidStackIngredient input, FluidStackIngredient output, double coolingRate) {
        coolantRecipe(List.of(input), List.of(output), coolingRate);
    }
}
