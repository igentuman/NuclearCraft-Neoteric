package igentuman.nc.datagen.recipes;

import igentuman.nc.datagen.recipes.builder.ItemToItemRecipeBuilder;
import igentuman.nc.datagen.recipes.recipes.*;
import igentuman.nc.recipes.ingredient.NcIngredient;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.function.Consumer;

import static igentuman.nc.NuclearCraft.rl;

public class CustomRecipes extends NCRecipes {


    public CustomRecipes(DataGenerator generatorIn) {
        super(generatorIn);
    }
    public static Consumer<FinishedRecipe> consumer;
    public static void generate(Consumer<FinishedRecipe> consumer) {
        CustomRecipes.consumer = consumer;
        FissionRecipes.generate(consumer);
        ManufactoryRecipes.generate(consumer);
        DecayHastenerRecipes.generate(consumer);
        PressurizerRecipes.generate(consumer);
        AlloySmelterRecipeProvider.generate(consumer);
    }

    private static void itemToItemRecipe(String id, NcIngredient input, Item output, double... params) {
        itemToItemRecipe(id, input, new ItemStack(output), params);
    }

    private static void itemToItemRecipe(String id, NcIngredient input, ItemStack output, double... params) {

        double timeModifier = params.length>0 ? params[0] : 1.0;
        double powerModifier = params.length>1 ? params[1] : 1.0;
        double radiation = params.length>2 ? params[2] : 1.0;
        ItemToItemRecipeBuilder.create(id, input, output)
                .modifiers(timeModifier, radiation, powerModifier)
                .build(consumer, rl(id+"/"+output.getItem().toString()));
    }

}
