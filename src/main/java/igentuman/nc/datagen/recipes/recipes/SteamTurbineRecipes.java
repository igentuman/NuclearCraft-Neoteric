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
                fluidIngredient("steam", 100),
                fluidIngredient("minecraft:water", 100),
                0.1D
        );

        add(
                fluidIngredient("high_pressure_steam", 100),
                fluidIngredient("exhaust_steam", 100),
                0.2D, 1.1D
        );

    }

    protected static void add(FluidStackIngredient input, FluidStackIngredient output, double...modifiers) {
        fluidsAndFluids(List.of(input), List.of(output), modifiers);
    }
}
