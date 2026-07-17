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
                    fluidIngredient("liquid_" + gas, 1),
                    fluidIngredient(gas, 1),
                    1000
            );
        }

        add(
                fluidIngredient("minecraft:water", 100),
                fluidIngredient("steam", 100),
                100
        );

        add(
                fluidIngredient("technical_water", 100),
                fluidIngredient("high_pressure_steam", 100),
                100
        );
    }

    protected static void add(FluidStackIngredient input, FluidStackIngredient output, double coolingRate) {
        coolantRecipe(List.of(input), List.of(output), coolingRate);
    }
}
