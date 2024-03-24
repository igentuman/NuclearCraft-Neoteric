package igentuman.nc.datagen.recipes.recipes;

import igentuman.nc.recipes.ingredient.NcIngredient;
import igentuman.nc.content.processors.Processors;
import igentuman.nc.content.materials.Materials;
import net.minecraft.data.recipes.FinishedRecipe;

import java.util.List;
import java.util.function.Consumer;

import static net.minecraft.world.item.Items.*;

public class RockCrusherRecipes extends AbstractRecipeProvider {

    public static void generate(Consumer<FinishedRecipe> consumer) {
        RockCrusherRecipes.consumer = consumer;
        ID = Processors.ROCK_CRUSHER;

        add(
                (ingredient(END_STONE, 3)),
                List.of(dustIngredient(Materials.borax, 1), dustIngredient(Materials.germanium, 1))
        );

        add(
                (ingredient(PURPUR_BLOCK, 2)),
                List.of(dustIngredient(Materials.purpur, 1))
        );

        add(
                (ingredient(NETHERRACK, 2)),
                List.of(dustIngredient(Materials.sulfur, 1))
        );

        add(
                (ingredient(BASALT, 7)),
                List.of(dustIngredient(Materials.tungsten, 1), dustIngredient(Materials.niobium, 1))
        );

        add(
                (ingredient(ANCIENT_DEBRIS, 1)),
                List.of(NcIngredient.stack(stack(NETHERITE_SCRAP, 2)), dustIngredient(Materials.titanium, 1))
        );

        add(
                (ingredient(GRANITE, 4)),
                List.of(dustIngredient(Materials.rhodochrosite, 2), dustIngredient(Materials.villiaumite))
        );

        add(
                ingredient(DIORITE, 4),
                List.of(dustIngredient(Materials.zirconium, 2), dustIngredient(Materials.fluorite), dustIngredient(Materials.carobbiite))
        );

        add(
                ingredient(ANDESITE, 4),
                List.of(dustIngredient(Materials.beryllium, 2), dustIngredient(Materials.arsenic))
        );
        add(
                ingredient(DEEPSLATE, 7),
                List.of(dustIngredient(Materials.iodine, 1), dustIngredient(Materials.obsidian))
        );
        add(
                ingredient(TUFF, 12),
                List.of(dustIngredient(Materials.chromium, 1), dustIngredient(Materials.coal))
        );
        add(
                ingredient(CALCITE, 5),
                List.of(dustIngredient(Materials.calcium, 2), dustIngredient(Materials.potassium))
        );
    }

    private static void add(NcIngredient input, List<NcIngredient> output, double...modifiers) {
        itemsToItems(List.of(input), output, modifiers);
    }

}
