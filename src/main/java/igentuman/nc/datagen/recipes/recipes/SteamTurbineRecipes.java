package igentuman.nc.datagen.recipes.recipes;

import igentuman.nc.content.processors.Processors;
import igentuman.nc.recipes.ingredient.FluidStackIngredient;
import net.minecraft.data.IFinishedRecipe;
import net.minecraftforge.fluids.FluidStack;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import static net.minecraft.fluid.Fluids.WATER;


public class SteamTurbineRecipes extends AbstractRecipeProvider {

    public static void generate(Consumer<IFinishedRecipe> consumer) {
        SteamTurbineRecipes.consumer = consumer;
        ID = Processors.STEAM_TURBINE;

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
