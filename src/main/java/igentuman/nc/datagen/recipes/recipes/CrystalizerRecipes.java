package igentuman.nc.datagen.recipes.recipes;

import igentuman.nc.content.materials.Materials;
import igentuman.nc.content.processors.Processors;
import igentuman.nc.recipes.ingredient.FluidStackIngredient;
import igentuman.nc.recipes.ingredient.NcIngredient;
import net.minecraft.data.recipes.FinishedRecipe;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static igentuman.nc.datagen.recipes.NCRecipes.MOLTEN_INGOT;
import static igentuman.nc.setup.registration.NCItems.NC_ITEMS;
import static net.minecraft.world.item.Items.*;

public class CrystalizerRecipes extends AbstractRecipeProvider {

    public static void generate(Consumer<FinishedRecipe> consumer) {
        CrystalizerRecipes.consumer = consumer;
        ID = Processors.CRYSTALLIZER;
        itemsAndFluids(fluidIngredient("minecraft:water", 10000), ingredient(NC_ITEMS.get("salt").get()), 2.5D, 2.5D);
        itemsAndFluids(fluidIngredient("redstone", MOLTEN_INGOT), ingredient(REDSTONE), 0.5D, 0.5D);
        itemsAndFluids(fluidIngredient("glowstone", MOLTEN_INGOT), ingredient(GLOWSTONE_DUST), 0.5D, 0.5D);
        itemsAndFluids(fluidIngredient(Materials.lapis, MOLTEN_INGOT), ingredient(LAPIS_LAZULI));
        itemsAndFluids(fluidIngredient(Materials.sulfur, MOLTEN_INGOT), dustIngredient(Materials.sulfur));
        itemsAndFluids(fluidIngredient("boron_nitride_solution", 250), dustIngredient(Materials.boron_nitride));
        itemsAndFluids(fluidIngredient("uranium_oxide", MOLTEN_INGOT), dustIngredient(Materials.yellowcake));
        itemsAndFluids(fluidIngredient(Materials.polonium, 1000), NcIngredient.stack(stack("mekanism:pellet_polonium", 1)), 3D);
        itemsAndFluids(fluidIngredient(Materials.potassium_iodide, MOLTEN_INGOT), dustIngredient(Materials.potassium_iodide));
        itemsAndFluids(fluidIngredient("fluorite_water", 250), dustIngredient(Materials.fluorite));
        itemsAndFluids(fluidIngredient("calcium_sulfate_solution", 250), dustIngredient(Materials.calcium_sulfate));
        itemsAndFluids(fluidIngredient("sodium_fluoride_solution", 250), dustIngredient(Materials.sodium_fluoride));
        itemsAndFluids(fluidIngredient("potassium_fluoride_solution", 250), dustIngredient(Materials.potassium_fluoride));
        itemsAndFluids(fluidIngredient("sodium_hydroxide_solution", 250), dustIngredient(Materials.sodium_hydroxide), 0.5D, 0.5D);
        itemsAndFluids(fluidIngredient("potassium_hydroxide_solution", MOLTEN_INGOT), dustIngredient(Materials.potassium_hydroxide), 0.5D, 0.5D);
        itemsAndFluids(fluidIngredient("borax_solution", 250), dustIngredient(Materials.borax), 0.5D, 0.5D);
        itemsAndFluids(fluidIngredient("irradiated_borax_solution", 250), dustIngredient(Materials.irradiated_borax), 0.5D, 0.5D);

        for(String material: Materials.slurries()) {
            itemsAndFluids(
                    fluidIngredient(material+"_clean_slurry", 400),
                    dustIngredient(material, 2)
            );
        }
    }

    protected static void itemsAndFluids(FluidStackIngredient inputFluid, NcIngredient outputItem, double...modifiers) {
        itemsAndFluids(new ArrayList<>(), List.of(outputItem), List.of(inputFluid), new ArrayList<>(), modifiers);
    }
}
