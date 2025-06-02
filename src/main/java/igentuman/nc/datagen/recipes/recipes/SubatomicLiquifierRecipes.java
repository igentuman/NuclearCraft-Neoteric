package igentuman.nc.datagen.recipes.recipes;

import igentuman.nc.content.processors.Processors;
import net.minecraft.data.recipes.FinishedRecipe;

import java.util.List;
import java.util.function.Consumer;

import static igentuman.nc.content.materials.Materials.subliquid_matter;
import static igentuman.nc.datagen.recipes.NCRecipes.MOLTEN_BLOCK;
import static igentuman.nc.datagen.recipes.NCRecipes.MOLTEN_INGOT;

public class SubatomicLiquifierRecipes extends AbstractRecipeProvider {

    public static void generate(Consumer<FinishedRecipe> consumer) {
        SubatomicLiquifierRecipes.consumer = consumer;
        ID = Processors.SUBATOMIC_LIQUIFIER;

        for(String type: List.of("iron", "copper", "tin", "lead", "gold", "silver", "aluminum", "aluminium", "uranium", "thorium", "cobalt", "magnesium", "boron", "lithium")) {
            itemsAndFluids(
                    List.of(dustIngredient(type)),
                    List.of(),
                    List.of(),
                    List.of(fluidIngredient(subliquid_matter, MOLTEN_INGOT)));
            itemsAndFluids(
                    List.of(ingotIngredient(type)),
                    List.of(),
                    List.of(),
                    List.of(fluidIngredient(subliquid_matter, MOLTEN_INGOT)), 1.2D);
            itemsAndFluids(
                    List.of(blockIngredient(type)),
                    List.of(),
                    List.of(),
                    List.of(fluidIngredient(subliquid_matter, MOLTEN_BLOCK)), 4.0D);
        }
    }
}
