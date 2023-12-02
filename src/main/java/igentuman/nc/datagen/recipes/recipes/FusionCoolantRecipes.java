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
                    fluidIngredient("liquid_" + gas, 1),
                    fluidStack(gas, 1),
                    2000
            );
        }

        add(
                fluidIngredient("minecraft:water", 1),
                fluidStack("steam", 1),
                500
        );
    }

    protected static void add(FluidStackIngredient input, FluidStack output, double coolingRate) {
        coolantRecipe(List.of(input), List.of(output), coolingRate);
    }
}
