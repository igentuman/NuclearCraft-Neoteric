package igentuman.nc.datagen.recipes.recipes;

import igentuman.nc.recipes.ingredient.NcIngredient;
import igentuman.nc.setup.processors.Processors;
import igentuman.nc.setup.registration.Materials;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.function.Consumer;

import static net.minecraft.world.item.Items.*;

public class RockCrusherRecipes extends AbstractRecipeProvider {

    public static void generate(Consumer<FinishedRecipe> consumer) {
        RockCrusherRecipes.consumer = consumer;
        ID = Processors.ROCK_CRUSHER;
        add(
                (ingredient(GRANITE, 4)),
                List.of(dustStack(Materials.rhodochrosite, 2), dustStack(Materials.villiaumite))
        );

        add(
                ingredient(DIORITE, 4),
                List.of(dustStack(Materials.zirconium, 2), dustStack(Materials.fluorite), dustStack(Materials.carobbiite))
        );

        add(
                ingredient(ANDESITE, 4),
                List.of(dustStack(Materials.beryllium, 2), dustStack(Materials.arsenic))
        );

    }

    private static void add(NcIngredient input, List<NcIngredient> output, double...modifiers) {
        itemsToItems(List.of(input), output, modifiers);
    }

}
