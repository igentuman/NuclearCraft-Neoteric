package igentuman.nc.datagen.recipes.recipes;

import igentuman.nc.recipes.ingredient.NcIngredient;
import igentuman.nc.setup.processors.Processors;
import igentuman.nc.setup.registration.Materials;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.function.Consumer;

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

    }

    private static void add(NcIngredient input, List<NcIngredient> output, double...modifiers) {
        itemsToItems(List.of(input), output, modifiers);
    }
}
