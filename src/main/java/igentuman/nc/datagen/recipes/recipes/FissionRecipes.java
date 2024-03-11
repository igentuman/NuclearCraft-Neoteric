
package igentuman.nc.datagen.recipes.recipes;

import igentuman.nc.block.entity.fission.FissionControllerBE;
import igentuman.nc.datagen.recipes.builder.NcRecipeBuilder;
import igentuman.nc.recipes.ingredient.NcIngredient;
import igentuman.nc.setup.registration.Fuel;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import static igentuman.nc.NuclearCraft.rl;

public class FissionRecipes {


    public static Consumer<IFinishedRecipe> consumer;
    public static void generate(Consumer<IFinishedRecipe> consumer) {
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
                .items(Arrays.asList(input), Arrays.asList(output))
                .modifiers(timeModifier, radiation, powerModifier)
                .build(consumer, rl(id+"/"+output.getName().replace("depleted_fuel_", "")));
    }

    private static void solidFissionRecipes() {
        for(List<String> name: Fuel.NC_FUEL.keySet()) {
            if(name.contains("tr")) continue;
            List<String> depleted = new ArrayList<>(name);
            depleted.set(0, "depleted");
            itemToItemRecipe(FissionControllerBE.NAME,
                    NcIngredient.of(Fuel.NC_FUEL.get(name).get()),
                    Fuel.NC_DEPLETED_FUEL.get(depleted).get());
        }
    }

}
