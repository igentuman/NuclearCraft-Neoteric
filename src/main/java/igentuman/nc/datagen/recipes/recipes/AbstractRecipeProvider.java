package igentuman.nc.datagen.recipes.recipes;

import igentuman.nc.datagen.recipes.builder.ItemToItemRecipeBuilder;
import igentuman.nc.recipes.ingredient.NcIngredient;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;

import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.setup.registration.Fuel.NC_ISOTOPES;
import static igentuman.nc.setup.registration.NCItems.*;

public abstract class AbstractRecipeProvider {

    public static String ID;

    public static Consumer<FinishedRecipe> consumer;

    public static Item dustItem(String name)
    {
        if(NC_DUSTS.get(name) == null) {
            System.out.println("null dust: " + name);
        }
        return NC_DUSTS.get(name).get();
    }

    public static TagKey<Item> dustTag(String name)
    {
        if(DUSTS_TAG.get(name) == null) {
            System.out.println("null dust tag: " + name);
        }
        return DUSTS_TAG.get(name);
    }

    public static Item isotopeItem(String name)
    {
        if(NC_ISOTOPES.get(name) == null) {
            System.out.println("null isotope: " + name);
        }
        return NC_ISOTOPES.get(name).get();
    }

    public static Item plateItem(String name)
    {
        if(NC_PLATES.get(name) == null) {
            System.out.println("null plate: " + name);
        }
        return NC_PLATES.get(name).get();
    }

    public static TagKey<Item> plateTag(String name)
    {
        if(PLATES_TAG.get(name) == null) {
            System.out.println("null plate tag: " + name);
        }
        return PLATES_TAG.get(name);
    }

    public static Item ingotItem(String name)
    {
        if(NC_INGOTS.get(name) == null) {
            System.out.println("null ingot: " + name);
        }
        return NC_INGOTS.get(name).get();
    }

    public static TagKey<Item> ingotTag(String name)
    {
        if(INGOTS_TAG.get(name) == null) {
            System.out.println("null ingot tag: " + name);
        }
        return INGOTS_TAG.get(name);
    }

    public static TagKey<Item> gemTag(String name) {
        if(GEMS_TAG.get(name) == null) {
            System.out.println("null gem tag: " + name);
        }
        return GEMS_TAG.get(name);
    }

    public static Item gemItem(String name)
    {
        if(NC_GEMS.get(name) == null) {
            System.out.println("null gem: " + name);
        }
        return NC_GEMS.get(name).get();
    }


    protected static void add(Item input, ItemStack output, double... params) {
        itemToItemRecipe(ID, NcIngredient.of(input), output, params);

    }

    protected static void add(ItemStack itemStack, Item out, double... params) {
        NcIngredient in = NcIngredient.stack(itemStack);
        itemToItemRecipe(ID, in, out, params);
    }

    protected static void add(TagKey<Item> itemTagKey, Item item, double... params) {
        itemToItemRecipe(ID, NcIngredient.of(itemTagKey), item, params);
    }

    public static void add(Item input, Item output, double... params)
    {
        itemToItemRecipe(ID, NcIngredient.of(input), output, params);
    }

    public static void add(NcIngredient input, Item output, double... params)
    {
        itemToItemRecipe(ID, input, output, params);
    }

    protected static void itemToItemRecipe(String id, NcIngredient input, Item output, double... params) {
        itemToItemRecipe(id, input, new ItemStack(output), params);
    }

    protected static void itemToItemRecipe(String id, NcIngredient input, ItemStack output, double... params) {

        double timeModifier = params.length>0 ? params[0] : 1.0;
        double powerModifier = params.length>1 ? params[1] : 1.0;
        double radiation = params.length>2 ? params[2] : 1.0;
        ItemToItemRecipeBuilder.create(id, input, output)
                .modifiers(timeModifier, radiation, powerModifier)
                .build(consumer, rl(id+"/"+input.getName()+"-"+output.getItem().toString()));
    }
}
