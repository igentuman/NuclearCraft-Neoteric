package igentuman.nc.datagen.recipes.recipes;

import igentuman.nc.datagen.recipes.builder.ItemToItemRecipeBuilder;
import igentuman.nc.recipes.ingredient.NcIngredient;
import igentuman.nc.setup.processors.Processors;
import igentuman.nc.setup.registration.Materials;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;

import static igentuman.nc.NuclearCraft.rl;

public class AlloySmelterRecipeProvider extends AbstractRecipeProvider {

    public static void generate(Consumer<FinishedRecipe> consumer) {
        AlloySmelterRecipeProvider.consumer = consumer;
        ID = Processors.ALLOY_SMELTER;
        doubleToItem(ID, NcIngredient.stack(new ItemStack(dustItem(Materials.coal), 4)), NcIngredient.of(dustTag(Materials.iron)), new ItemStack(ingotItem(Materials.steel)));

    }

    protected static void doubleToItem(String id, NcIngredient input1, NcIngredient input2, ItemStack output, double... params) {

        double timeModifier = params.length>0 ? params[0] : 1.0;
        double powerModifier = params.length>1 ? params[1] : 1.0;
        double radiation = params.length>2 ? params[2] : 1.0;
        ItemToItemRecipeBuilder.create(id, input1, input2, output)
                .modifiers(timeModifier, radiation, powerModifier)
                .build(consumer, rl(id+"/"+input1.getName()+"-"+output.getItem().toString()));
    }
}
