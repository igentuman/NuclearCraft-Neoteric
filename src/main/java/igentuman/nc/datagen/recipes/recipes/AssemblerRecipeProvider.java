package igentuman.nc.datagen.recipes.recipes;

import igentuman.nc.content.fuel.FuelManager;
import igentuman.nc.content.processors.Processors;
import igentuman.nc.item.ItemFuel;
import igentuman.nc.recipes.ingredient.NcIngredient;
import igentuman.nc.setup.registration.Fuel;
import igentuman.nc.setup.registration.Materials;
import net.minecraft.data.recipes.FinishedRecipe;

import java.util.List;
import java.util.function.Consumer;

import static igentuman.nc.setup.registration.Fuel.NC_FUEL;

public class AssemblerRecipeProvider extends AbstractRecipeProvider {

    public static void generate(Consumer<FinishedRecipe> consumer) {
        AssemblerRecipeProvider.consumer = consumer;
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
                        List.of(
                            ingredient(fuelItem(List.of("fuel", name, subType, "tr")), 9)
                        )
                );
            }
        }
    }
}
