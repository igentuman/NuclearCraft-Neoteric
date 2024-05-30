
package igentuman.nc.datagen.recipes.recipes;

import igentuman.nc.block.entity.fission.FissionControllerBE;
import igentuman.nc.datagen.recipes.builder.NcRecipeBuilder;
import igentuman.nc.recipes.ingredient.NcIngredient;
import igentuman.nc.setup.registration.FissionFuel;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static igentuman.nc.NuclearCraft.rl;

public class FissionRecipes {


    public static Consumer<FinishedRecipe> consumer;
    public static void generate(Consumer<FinishedRecipe> consumer) {
        FissionRecipes.consumer = consumer;
        solidFissionRecipes();
    }

    private static void itemToItemRecipe(String id, NcIngredient input, Item output, double... params) {
        itemToItemRecipe(id, input, NcIngredient.of(output), params);
    }

    private static void itemToItemRecipe(String id, NcIngredient input, NcIngredient output, double... params) {

        double timeModifier = params.length>0 ? params[0] : 1.0;
        double powerModifier = params.length>1 ? params[1] : 1.0;
        double radiation = params.length>2 ? params[2] : 1.0;
        NcRecipeBuilder.get(id)
                .items(List.of(input), List.of(output))
                .modifiers(timeModifier, radiation, powerModifier)
                .build(consumer, rl(id+"/"+output.getName().replace("depleted_fuel_", "")));
    }

    private static void solidFissionRecipes() {
        for(List<String> name: FissionFuel.NC_FUEL.keySet()) {
            if(name.contains("tr")) continue;
            List<String> depleted = new ArrayList<>(name);
            depleted.set(0, "depleted");
            itemToItemRecipe(FissionControllerBE.NAME,
                    NcIngredient.of(FissionFuel.NC_FUEL.get(name).get()),
                    FissionFuel.NC_DEPLETED_FUEL.get(depleted).get());
        }
    }

}
