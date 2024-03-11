package igentuman.nc.datagen.recipes.recipes;

import igentuman.nc.block.entity.turbine.TurbineControllerBE;
import igentuman.nc.recipes.ingredient.FluidStackIngredient;
import net.minecraft.data.IFinishedRecipe;
import net.minecraftforge.fluids.FluidStack;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import static net.minecraft.fluid.Fluids.WATER;

public class TurbineControllerRecipes extends AbstractRecipeProvider {

    public static void generate(Consumer<IFinishedRecipe> consumer) {
        TurbineControllerRecipes.consumer = consumer;
        ID = TurbineControllerBE.NAME;

        add(
                fluidIngredient("steam", 10),
                fluidStack(WATER, 10),
                1.5D
        );

        add(
                fluidIngredient("high_pressure_steam", 10),
                fluidStack("exhaust_steam", 10),
                2D, 2.5D
        );

    }

    protected static void add(FluidStackIngredient input, FluidStack output, double...modifiers) {
        fluidsAndFluids(Arrays.asList(input), Arrays.asList(output), modifiers);
    }
}
