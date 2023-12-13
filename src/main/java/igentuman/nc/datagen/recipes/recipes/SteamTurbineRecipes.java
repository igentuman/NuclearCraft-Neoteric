package igentuman.nc.datagen.recipes.recipes;

import igentuman.nc.content.processors.Processors;
import igentuman.nc.recipes.ingredient.FluidStackIngredient;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;
import java.util.function.Consumer;

import static net.minecraft.world.level.material.Fluids.WATER;

public class SteamTurbineRecipes extends AbstractRecipeProvider {

    public static void generate(Consumer<FinishedRecipe> consumer) {
        SteamTurbineRecipes.consumer = consumer;
        ID = Processors.STEAM_TURBINE;

        add(
                fluidIngredient("steam", 10),
                fluidStack("water", 10),
                1.5D
        );

        add(
                fluidIngredient("high_pressure_steam", 10),
                fluidStack("exhaust_steam", 10),
                2D, 2.5D
        );

    }

    protected static void add(FluidStackIngredient input, FluidStack output, double...modifiers) {
        fluidsAndFluids(List.of(input), List.of(output), modifiers);
    }
}
