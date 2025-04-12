package igentuman.nc.datagen.recipes.recipes;

import igentuman.nc.recipes.ingredient.FluidStackIngredient;
import igentuman.nc.recipes.ingredient.creator.FluidStackIngredientCreator;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.fluid.Fluid;
import net.minecraft.tags.ITag;
import net.minecraftforge.fluids.FluidStack;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import static net.minecraft.tags.FluidTags.WATER;

public class FissionBoilingRecipes extends AbstractRecipeProvider {

    public static void generate(Consumer<IFinishedRecipe> consumer) {
        FissionBoilingRecipes.consumer = consumer;
        ID = "fission_boiling";

        add(
                fluidIngredient(WATER, 10),
                fluidStack("steam", 10),
                1
        );

        add(
                fluidIngredient("technical_water", 10),
                fluidStack("high_pressure_steam", 10),
                1
        );
    }



    protected static void add(FluidStackIngredient input, FluidStack output, double heatRequired) {
        boilingRecipe(Arrays.asList(input), Arrays.asList(output), heatRequired);
    }
}
