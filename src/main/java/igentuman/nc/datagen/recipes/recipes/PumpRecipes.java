package igentuman.nc.datagen.recipes.recipes;

import igentuman.nc.content.materials.Materials;
import igentuman.nc.content.processors.Processors;
import igentuman.nc.recipes.ingredient.NcIngredient;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static net.minecraft.world.item.Items.*;
import static net.minecraft.world.level.material.Fluids.LAVA;
import static net.minecraft.world.level.material.Fluids.WATER;

public class PumpRecipes extends AbstractRecipeProvider {

    public static void generate(Consumer<FinishedRecipe> consumer) {
        PumpRecipes.consumer = consumer;
        ID = Processors.PUMP;
        add(ingredient(HEART_OF_THE_SEA), fluidStack(WATER, 1000));
        add(ingotIngredient(Materials.pyrolitic_carbon, 32), fluidStack("nitrogen", 100));
        add(ingotIngredient(Materials.enderium, 32), fluidStack("helium", 100), 1D, 1D, 1.1D);
        add(plateIngredient(Materials.thermoconducting, 32), fluidStack(LAVA, 500), 1D, 3D);
    }

    protected static void add(NcIngredient inputItem, FluidStack outputFluid, double...modifiers) {
        itemsAndFluids(List.of(inputItem), new ArrayList<>(), new ArrayList<>(), List.of(outputFluid), modifiers);
    }
}
