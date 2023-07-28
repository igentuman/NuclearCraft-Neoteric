
package igentuman.nc.datagen.recipes.recipes;

import igentuman.nc.block.entity.fission.FissionControllerBE;
import igentuman.nc.datagen.recipes.CustomRecipes;
import igentuman.nc.datagen.recipes.NCRecipes;
import igentuman.nc.datagen.recipes.builder.ItemToItemRecipeBuilder;
import igentuman.nc.setup.registration.Fuel;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

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

    private static void itemToItemRecipe(String id, Ingredient input, Item output, double... params) {
        itemToItemRecipe(id, input, new ItemStack(output), params);
    }

    private static void itemToItemRecipe(String id, Ingredient input, ItemStack output, double... params) {

        double timeModifier = params.length>0 ? params[0] : 1.0;
        double powerModifier = params.length>1 ? params[1] : 1.0;
        double radiation = params.length>2 ? params[2] : 1.0;
        ItemToItemRecipeBuilder.create(id, input, output)
                .modifiers(timeModifier, radiation, powerModifier)
                .build(consumer, rl(id+"/"+output.getItem().toString().replace("depleted_fuel_", "")));
    }

    private static void solidFissionRecipes() {
        for(List<String> name: Fuel.NC_FUEL.keySet()) {
            if(name.contains("tr")) continue;
            List<String> depleted = new ArrayList<>(name);
            depleted.set(0, "depleted");
            itemToItemRecipe(FissionControllerBE.NAME,
                    Ingredient.of(Fuel.NC_FUEL.get(name).get()),
                    Fuel.NC_DEPLETED_FUEL.get(depleted).get());
        }
    }

}
