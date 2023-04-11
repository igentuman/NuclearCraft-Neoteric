package igentuman.nc.datagen.recipes;

import igentuman.nc.datagen.recipes.builder.ItemToItemRecipeBuilder;
import igentuman.nc.setup.registration.Fuel;
import igentuman.nc.setup.registration.Materials;
import igentuman.nc.setup.registration.NCItems;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static igentuman.nc.NuclearCraft.rl;

public class CustomRecipes extends NCRecipes {


    public CustomRecipes(DataGenerator generatorIn) {
        super(generatorIn);
    }
    public static Consumer<FinishedRecipe> consumer;
    public static void generate(Consumer<FinishedRecipe> consumer) {
        CustomRecipes.consumer = consumer;
        manufactoryRecipes();
        fissionReactorRecipes();
    }

    private static void manufactoryRecipes() {
        for(String name: Materials.all().keySet()) {
            if(NCItems.NC_DUSTS.containsKey(name) && NCItems.INGOTS_TAG.containsKey(name)) {
                itemToItemRecipe("manufactory",
                        Ingredient.of(NCItems.INGOTS_TAG.get(name)),
                        NCItems.NC_DUSTS.get(name).get());
            }
        }
    }

    private static void itemToItemRecipe(String id, Ingredient input, Item output, double... params) {

        double timeModifier = params.length>0 ? params[0] : 1.0;
        double radiation = params.length>1 ? params[1] : 1.0;
        double powerModifier = params.length>2 ? params[2] : 1.0;

        ItemToItemRecipeBuilder.create(id, input, new ItemStack(output))
                .modifiers(timeModifier, radiation, powerModifier)
                .build(consumer, rl(id+"_"+input.getItems()[0].getItem().toString()+"_to_"+output.toString()));
    }

    private static void fissionReactorRecipes() {
        for(List<String> name: Fuel.NC_FUEL.keySet()) {
            if(name.contains("tr")) continue;
            List<String> depleted = new ArrayList<>(name);
            depleted.set(0, "depleted");
            itemToItemRecipe("fission_reactor",
                    Ingredient.of(Fuel.NC_FUEL.get(name).get()),
                    Fuel.NC_DEPLETED_FUEL.get(depleted).get(),
                    1.0, 1.1, 1.0);
        }
    }

}
