package igentuman.nc.datagen.recipes.recipes;

import igentuman.nc.recipes.ingredient.NcIngredient;
import igentuman.nc.setup.processors.Processors;
import igentuman.nc.setup.registration.Fuel;
import igentuman.nc.setup.registration.Materials;
import igentuman.nc.setup.registration.fuel.FuelManager;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;
import java.util.function.Consumer;

import static igentuman.nc.NuclearCraft.MODID;
import static net.minecraft.world.item.Items.*;

public class IsotopeSeparatorRecipes extends AbstractRecipeProvider {

    public static void generate(Consumer<FinishedRecipe> consumer) {
        IsotopeSeparatorRecipes.consumer = consumer;
        ID = Processors.ISOTOPE_SEPARATOR;
        add(
                dustIngredient(Materials.boron, 12),
                List.of(isotopeStack(Materials.boron11, 9),
                        isotopeStack(Materials.boron10, 3)),
                6D
        );
        add(
                ingredient(dustItem(Materials.lithium), 10),
                List.of(isotopeStack(Materials.lithium7, 9),
                        isotopeStack(Materials.lithium6, 1)),
                5D
        );

        add(
                dustIngredient(Materials.uranium, 10),
                List.of(isotopeStack(Materials.uranium238, 9),
                        isotopeStack(Materials.uranium235, 1)),
                4D
        );

        for (String name: FuelManager.all().keySet()) {
            for(String subType: FuelManager.all().get(name).keySet()) {
                fuelSeparateRecipe(name, subType, "",
                        FuelManager.all().get(name).get(subType).getDefault().isotopes[0],
                        FuelManager.all().get(name).get(subType).getDefault().isotopes[1]);

            }
        }
    }

    private static void fuelSeparateRecipe(String name, String subType, String type, int isotope1, int isotope2)
    {
        int isotope1Cnt = 1;
        int isotope2Cnt = 8;
        if(subType.substring(0,1).equalsIgnoreCase("h")) {
            isotope1Cnt = 3;
            isotope2Cnt = 6;
        }
        add(ingredient(Fuel.NC_FUEL.get(List.of("fuel", name, subType, type)).get(), 9),
                List.of(ingredient(getIsotope(name, String.valueOf(isotope1), type), isotope1Cnt),
                        ingredient(getIsotope(name, String.valueOf(isotope2), type), isotope2Cnt)),
                2);
    }

    private static void add(NcIngredient input, List<NcIngredient> output, double...modifiers) {
        itemsToItems(List.of(input), output, modifiers);
    }
}
