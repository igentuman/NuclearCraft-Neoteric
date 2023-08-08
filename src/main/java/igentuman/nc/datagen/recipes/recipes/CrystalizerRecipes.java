package igentuman.nc.datagen.recipes.recipes;

import igentuman.nc.content.materials.Materials;
import igentuman.nc.content.processors.Processors;
import igentuman.nc.recipes.ingredient.FluidStackIngredient;
import igentuman.nc.recipes.ingredient.NcIngredient;
import net.minecraft.data.recipes.FinishedRecipe;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class CrystalizerRecipes extends AbstractRecipeProvider {

    public static void generate(Consumer<FinishedRecipe> consumer) {
        CrystalizerRecipes.consumer = consumer;
        ID = Processors.CRYSTALLIZER;

        itemsAndFluids(fluidIngredient("boron_nitride_solution", 144), dustStack(Materials.boron_nitride));
        itemsAndFluids(fluidIngredient("fluorite_water", 144), dustStack(Materials.fluorite));
        itemsAndFluids(fluidIngredient("calcium_sulfate_solution", 144), dustStack(Materials.calcium_sulfate));
        itemsAndFluids(fluidIngredient("sodium_fluoride_solution", 144), dustStack(Materials.sodium_fluoride));
        itemsAndFluids(fluidIngredient("potassium_fluoride_solution", 144), dustStack(Materials.potassium_fluoride));
        itemsAndFluids(fluidIngredient("sodium_hydroxide_solution", 144), dustStack(Materials.sodium_hydroxide), 0.5D, 0.5D);
        itemsAndFluids(fluidIngredient("potassium_hydroxide_solution", 144), dustStack(Materials.potassium_hydroxide), 0.5D, 0.5D);
        itemsAndFluids(fluidIngredient("borax_solution", 144), dustStack(Materials.borax), 0.5D, 0.5D);
        itemsAndFluids(fluidIngredient("irradiated_borax_solution", 144), dustStack(Materials.borax), 0.5D, 0.5D);
    }

    protected static void itemsAndFluids(FluidStackIngredient inputFluid, NcIngredient outputItem, double...modifiers) {
        itemsAndFluids(new ArrayList<>(), List.of(outputItem), List.of(inputFluid), new ArrayList<>(), modifiers);
    }
}
