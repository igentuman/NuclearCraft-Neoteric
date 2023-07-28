package igentuman.nc.datagen.recipes.recipes;

import com.google.common.collect.Lists;
import igentuman.nc.datagen.recipes.builder.ItemToItemRecipeBuilder;
import igentuman.nc.recipes.ingredient.NcIngredient;
import igentuman.nc.setup.processors.Processors;
import igentuman.nc.setup.registration.Materials;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

import java.util.function.Consumer;

import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.setup.registration.NCItems.*;

public class PressurizerRecipes extends ProcessorRecipeProvider {


    public static Consumer<FinishedRecipe> consumer;
    public static void generate(Consumer<FinishedRecipe> consumer) {
        PressurizerRecipes.consumer = consumer;
        for (String name : Materials.all().keySet()) {
            if(INGOTS_TAG.get(name) != null && NC_PLATES.get(name) != null) {
                recipe(INGOTS_TAG.get(name), plateItem(name));
            }
        }

        recipe(dustTag(Materials.graphite), Items.COAL);
       // recipe(ingotTag(Materials.graphite), ingotItem(Materials.pyroliticCarbon));
        recipe(dustTag(Materials.diamond), Items.DIAMOND);
        recipe(dustTag(Materials.rhodochrosite), gemItem(Materials.rhodochrosite));
        recipe(dustTag(Materials.quartz), Items.QUARTZ);
        recipe(new ItemStack(dustItem(Materials.obsidian), 4), Item.byBlock(Blocks.OBSIDIAN));
        recipe(dustTag(Materials.boron_nitride), gemItem(Materials.boron_nitride));
        recipe(dustTag(Materials.fluorite), gemItem(Materials.fluorite));
        recipe(dustTag(Materials.villiaumite), gemItem(Materials.villiaumite));
        recipe(dustTag(Materials.carobbiite), gemItem(Materials.carobbiite));

    }


    private static void recipe(Item input, ItemStack output, double... params) {
        itemToItemRecipe(Processors.PRESSURIZER, NcIngredient.of(input), output, params);

    }

    private static void recipe(ItemStack itemStack, Item out, double... params) {
        NcIngredient in = NcIngredient.stack(itemStack);
        itemToItemRecipe(Processors.PRESSURIZER, in, out, params);
    }

    private static void recipe(TagKey<Item> itemTagKey, Item item, double... params) {
        itemToItemRecipe(Processors.PRESSURIZER, NcIngredient.of(itemTagKey), item, params);
    }

    public static void recipe(Item input, Item output, double... params)
    {
        itemToItemRecipe(Processors.PRESSURIZER, NcIngredient.of(input), output, params);
    }

    public static void recipe(NcIngredient input, Item output, double... params)
    {
        itemToItemRecipe(Processors.PRESSURIZER, input, output, params);
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
                .build(consumer, rl(id+"/"+input.getName()+"-"+output.getItem().toString()));
    }
}
