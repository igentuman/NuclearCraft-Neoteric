package igentuman.nc.datagen.recipes.recipes;

import igentuman.nc.content.processors.Processors;
import igentuman.nc.recipes.ingredient.FluidStackIngredient;
import igentuman.nc.recipes.ingredient.creator.IngredientCreatorAccess;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;
import java.util.function.Consumer;

import static net.minecraft.world.level.material.Fluids.WATER;

public class ElectrolyzerRecipes extends AbstractRecipeProvider {

    public static void generate(Consumer<FinishedRecipe> consumer) {
        ElectrolyzerRecipes.consumer = consumer;
        ID = Processors.ELECTROLYZER;

        add(fluidIngredient("minecraft:water", 500),
                List.of(
                        fluidIngredient("hydrogen", 500),
                        fluidIngredient("oxygen", 250)
                ), 0.5D
        );
        add(fluidIngredient("heavy_water", 500),
                List.of(
                        fluidIngredient("deuterium", 500),
                        fluidIngredient("oxygen", 250)
                ), 0.5D
        );
        add(fluidIngredient("hydrofluoric_acid", 250),
                List.of(
                        fluidIngredient("hydrogen", 250),
                        fluidIngredient("fluorine", 250)
                ), 0.5D
        );
    }

    protected static void add(FluidStackIngredient input, List<FluidStackIngredient> output, double...modifiers) {
        double timeModifier = modifiers.length>0 ? modifiers[0] : 1.0;
        double powerModifier = modifiers.length>1 ? modifiers[1] : 1.5; //increased power consumption
        double radiation = modifiers.length>2 ? modifiers[2] : 1.0;
        fluidsAndFluids(List.of(input), output, timeModifier, powerModifier, radiation);
    }
}
