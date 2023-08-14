package igentuman.nc.datagen.recipes.recipes;

import igentuman.nc.content.fuel.FuelManager;
import igentuman.nc.content.processors.Processors;
import igentuman.nc.content.materials.Materials;
import net.minecraft.data.recipes.FinishedRecipe;

import java.util.List;
import java.util.function.Consumer;

public class AssemblerRecipes extends AbstractRecipeProvider {

    public static void generate(Consumer<FinishedRecipe> consumer) {
        AssemblerRecipes.consumer = consumer;
        ID = Processors.ASSEMBLER;

        for (String name: FuelManager.all().keySet()) {
            for(String subType: FuelManager.all().get(name).keySet()) {
                itemsToItems(
                        List.of(
                            ingredient(fuelItem(List.of("fuel", name, subType, "")), 9),
                            dustIngredient(Materials.graphite),
                            ingotIngredient(Materials.pyrolitic_carbon),
                            ingotIngredient(Materials.silicon_carbide)
                        ),
                        List.of(ingredient(fuelItem(List.of("fuel", name, subType, "tr")), 9))
                );
            }
        }

        itemsToItems(
                List.of(
                        dustIngredient(Materials.rhodochrosite),
                        dustIngredient(Materials.calcium_sulfate),
                        dustIngredient(Materials.magnesium),
                        dustIngredient(Materials.obsidian)
                ),
                List.of(ingredient(dustItem(Materials.crystal_binder), 2))
        );

    }
}
