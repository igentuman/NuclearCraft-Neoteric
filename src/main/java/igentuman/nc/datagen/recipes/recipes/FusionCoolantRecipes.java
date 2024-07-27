package igentuman.nc.datagen.recipes.recipes;

import igentuman.nc.content.processors.Processors;
import igentuman.nc.recipes.ingredient.FluidStackIngredient;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;
import java.util.function.Consumer;

public class FusionCoolantRecipes extends AbstractRecipeProvider {

    public static void generate(Consumer<FinishedRecipe> consumer) {
        FusionCoolantRecipes.consumer = consumer;
        ID = "fusion_coolant";

        for(String gas: List.of("nitrogen", "helium")) {
            add(
                    fluidIngredient("liquid_" + gas, 10),
                    fluidIngredient(gas, 10),
                    2500
            );
        }

        add(
                fluidIngredient("minecraft:water", 1000),
                fluidIngredient("steam", 1000),
                2000
        );

        add(
                fluidIngredient("technical_water", 1000),
                fluidIngredient("high_pressure_steam", 1000),
                2500
        );
    }

    protected static void add(FluidStackIngredient input, FluidStackIngredient output, double coolingRate) {
        coolantRecipe(List.of(input), List.of(output), coolingRate);
    }
}
