package igentuman.nc.datagen.recipes;

import igentuman.nc.NuclearCraft;
import igentuman.nc.block.entity.fission.FissionControllerBE;
import igentuman.nc.datagen.recipes.builder.ItemToItemRecipeBuilder;
import igentuman.nc.recipes.ingredient.NcIngredient;
import igentuman.nc.recipes.ingredient.creator.IIngredientCreator;
import igentuman.nc.recipes.ingredient.creator.IItemStackIngredientCreator;
import igentuman.nc.setup.processors.Processors;
import igentuman.nc.setup.registration.Fuel;
import igentuman.nc.setup.registration.Materials;
import igentuman.nc.setup.registration.NCItems;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static igentuman.nc.NuclearCraft.rl;
import static net.minecraft.world.item.Items.*;

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

    public static void manufactoryRecipes() {
        for(String name: Materials.all().keySet()) {
            if(NCItems.NC_DUSTS.containsKey(name) && NCItems.INGOTS_TAG.containsKey(name)) {
               manufactory(NCItems.INGOTS_TAG.get(name), NCItems.NC_DUSTS.get(name).get());
               continue;
            }
            if(NCItems.GEMS_TAG.containsKey(name) && NCItems.NC_DUSTS.containsKey(name)) {
                manufactory(NCItems.GEMS_TAG.get(name), NCItems.NC_DUSTS.get(name).get(), 1.5D);
            }
        }
        manufactory(COAL, NCItems.NC_DUSTS.get(Materials.coal).get(), 0.5D, 1D);
        manufactory(CHARCOAL, NCItems.NC_DUSTS.get(Materials.charcoal).get(), 0.5D, 0.5D);

        manufactory(DIAMOND, NCItems.NC_DUSTS.get(Materials.diamond).get(), 1.5D, 1.5D);
        manufactory(LAPIS_LAZULI, NCItems.NC_DUSTS.get(Materials.lapis).get(), 1D, 1D);
        manufactory(NCItems.DUSTS_TAG.get(Materials.villiaumite), NCItems.NC_DUSTS.get(Materials.sodium_fluoride).get(), 1D, 1D);
        manufactory(NCItems.DUSTS_TAG.get(Materials.carobbiite), NCItems.NC_DUSTS.get(Materials.potassium_fluoride).get(), 1D, 1D);
        manufactory(OBSIDIAN, NCItems.NC_DUSTS.get(Materials.obsidian).get(), 2D, 1D);
        manufactory(COBBLESTONE, SAND);
        manufactory(GRAVEL, FLINT);
        manufactory(END_STONE, NCItems.NC_DUSTS.get(Materials.end_stone).get());

        manufactory(BLAZE_ROD, new ItemStack(BLAZE_POWDER, 4));
        manufactory(BONE, new ItemStack(BONE_MEAL, 6));
        manufactory(new ItemStack(ROTTEN_FLESH, 4), LEATHER, 0.5D, 1D);
        manufactory(new ItemStack(SUGAR_CANE, 2), NCItems.NC_PARTS.get("bioplastic").get(), 1D, 0.5D);
    }

    private static void manufactory(Item input, ItemStack output, double... params) {
        itemToItemRecipe(Processors.MANUFACTORY, Ingredient.of(input), output, params);

    }

    private static void manufactory(ItemStack itemStack, Item out, double... params) {
        NcIngredient in = NcIngredient.stack(itemStack);
        itemToItemRecipe(Processors.MANUFACTORY, in, out, params);
    }

    private static void manufactory(TagKey<Item> itemTagKey, Item item, double... params) {
        itemToItemRecipe(Processors.MANUFACTORY, Ingredient.of(itemTagKey), item, params);
    }

    public static void manufactory(Item input, Item output, double... params)
    {
        itemToItemRecipe(Processors.MANUFACTORY, Ingredient.of(input), output, params);
    }

    public static void manufactory(Ingredient input, Item output, double... params)
    {
        itemToItemRecipe(Processors.MANUFACTORY, input, output, params);
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
                .build(consumer, rl(id+"/"+output.getItem().toString()));
    }

    private static void fissionReactorRecipes() {
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
