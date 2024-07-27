package igentuman.nc.datagen.recipes.recipes;

import igentuman.nc.block.entity.turbine.TurbineControllerBE;
import igentuman.nc.content.processors.Processors;
import igentuman.nc.recipes.ingredient.FluidStackIngredient;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;
import java.util.function.Consumer;

import static net.minecraft.world.level.material.Fluids.WATER;

public class TurbineControllerRecipes extends AbstractRecipeProvider {

    public static void generate(Consumer<FinishedRecipe> consumer) {
        TurbineControllerRecipes.consumer = consumer;
        ID = TurbineControllerBE.NAME;

        add(
                fluidIngredient("steam", 1000),
                fluidIngredient("minecraft:water", 1000),
                1.5D
        );

        add(
                fluidIngredient("high_pressure_steam", 1000),
                fluidIngredient("exhaust_steam", 1000),
                2D, 1.5D
        );

    }

    protected static void add(FluidStackIngredient input, FluidStackIngredient output, double...modifiers) {
        fluidsAndFluids(List.of(input), List.of(output), modifiers);
    }
}
