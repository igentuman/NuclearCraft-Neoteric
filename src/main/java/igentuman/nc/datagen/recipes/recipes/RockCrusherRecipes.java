package igentuman.nc.datagen.recipes.recipes;

import igentuman.nc.recipes.ingredient.NcIngredient;
import igentuman.nc.content.processors.Processors;
import igentuman.nc.content.materials.Materials;
import net.minecraft.data.IFinishedRecipe;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import static net.minecraft.item.Items.*;

public class RockCrusherRecipes extends AbstractRecipeProvider {

    public static void generate(Consumer<IFinishedRecipe> consumer) {
        RockCrusherRecipes.consumer = consumer;
        ID = Processors.ROCK_CRUSHER;


        add(
                (ingredient(BASALT, 7)),
                Arrays.asList(dustStack(Materials.tungsten, 1), dustStack(Materials.niobium, 1))
        );

        add(
                (ingredient(ANCIENT_DEBRIS, 1)),
                Arrays.asList(NcIngredient.stack(stack(NETHERITE_SCRAP, 2)), dustStack(Materials.titanium, 1))
        );

        add(
                (ingredient(GRANITE, 4)),
                Arrays.asList(dustStack(Materials.rhodochrosite, 2), dustStack(Materials.villiaumite))
        );

        add(
                ingredient(DIORITE, 4),
                Arrays.asList(dustStack(Materials.zirconium, 2), dustStack(Materials.fluorite), dustStack(Materials.carobbiite))
        );

        add(
                ingredient(ANDESITE, 4),
                Arrays.asList(dustStack(Materials.beryllium, 2), dustStack(Materials.arsenic))
        );
/*        add(
                ingredient(DEEPSLATE, 7),
                Arrays.asList(dustStack(Materials.iodine, 1), dustStack(Materials.obsidian))
        );
        add(
                ingredient(TUFF, 12),
                Arrays.asList(dustStack(Materials.chromium, 1), dustStack(Materials.coal))
        );
        add(
                ingredient(CALCITE, 5),
                Arrays.asList(dustStack(Materials.calcium, 2), dustStack(Materials.potassium))
        );*/
    }

    private static void add(NcIngredient input, List<NcIngredient> output, double...modifiers) {
        itemsToItems(Arrays.asList(input), output, modifiers);
    }

}
