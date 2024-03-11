package igentuman.nc.datagen.recipes.recipes;

import igentuman.nc.content.materials.Materials;
import igentuman.nc.content.processors.Processors;
import igentuman.nc.recipes.ingredient.FluidStackIngredient;
import igentuman.nc.recipes.ingredient.NcIngredient;
import net.minecraft.data.IFinishedRecipe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import static net.minecraft.item.Items.LAPIS_LAZULI;

public class CrystalizerRecipes extends AbstractRecipeProvider {

    public static void generate(Consumer<IFinishedRecipe> consumer) {
        CrystalizerRecipes.consumer = consumer;
        ID = Processors.CRYSTALLIZER;
        itemsAndFluids(fluidIngredient(Materials.lapis, 144), ingredient(LAPIS_LAZULI));
        itemsAndFluids(fluidIngredient("boron_nitride_solution", 144), dustStack(Materials.boron_nitride));
        itemsAndFluids(fluidIngredient("uranium_oxide", 144), dustStack(Materials.yellowcake));
        itemsAndFluids(fluidIngredient(Materials.polonium, 1000), NcIngredient.stack(stack("mekanism:pellet_polonium", 1)), 3D);
        itemsAndFluids(fluidIngredient(Materials.potassium_iodide, 144), dustStack(Materials.potassium_iodide));
        itemsAndFluids(fluidIngredient("fluorite_water", 144), dustStack(Materials.fluorite));
        itemsAndFluids(fluidIngredient("calcium_sulfate_solution", 144), dustStack(Materials.calcium_sulfate));
        itemsAndFluids(fluidIngredient("sodium_fluoride_solution", 144), dustStack(Materials.sodium_fluoride));
        itemsAndFluids(fluidIngredient("potassium_fluoride_solution", 144), dustStack(Materials.potassium_fluoride));
        itemsAndFluids(fluidIngredient("sodium_hydroxide_solution", 144), dustStack(Materials.sodium_hydroxide), 0.5D, 0.5D);
        itemsAndFluids(fluidIngredient("potassium_hydroxide_solution", 144), dustStack(Materials.potassium_hydroxide), 0.5D, 0.5D);
        itemsAndFluids(fluidIngredient("borax_solution", 144), dustStack(Materials.borax), 0.5D, 0.5D);
        itemsAndFluids(fluidIngredient("irradiated_borax_solution", 144), dustStack(Materials.irradiated_borax), 0.5D, 0.5D);

        for(String material: Materials.slurries()) {
            itemsAndFluids(
                    fluidIngredient(material+"_clean_slurry", 400),
                    dustStack(material, 2)
            );
        }
    }

    protected static void itemsAndFluids(FluidStackIngredient inputFluid, NcIngredient outputItem, double...modifiers) {
        itemsAndFluids(new ArrayList<>(), Arrays.asList(outputItem), Arrays.asList(inputFluid), new ArrayList<>(), modifiers);
    }
}
