package igentuman.nc.datagen.recipes.recipes;

import igentuman.nc.datagen.recipes.builder.ItemToItemRecipeBuilder;
import igentuman.nc.recipes.ingredient.NcIngredient;
import igentuman.nc.setup.processors.Processors;
import igentuman.nc.setup.registration.Fuel;
import igentuman.nc.setup.registration.Materials;
import igentuman.nc.setup.registration.NCItems;
import igentuman.nc.setup.registration.fuel.NCFuel;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.function.Consumer;

import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.setup.registration.Fuel.NC_ISOTOPES;
import static igentuman.nc.setup.registration.NCItems.NC_DUSTS;
import static net.minecraft.world.item.Items.*;

public class DecayHastenerRecipes extends ProcessorRecipeProvider {


    public static Consumer<FinishedRecipe> consumer;
    public static void generate(Consumer<FinishedRecipe> consumer) {
        DecayHastenerRecipes.consumer = consumer;

        recipe(dustItem(Materials.thorium), dustItem(Materials.lead));
        recipe(dustItem(Materials.radium), dustItem(Materials.lead));
        recipe(dustItem(Materials.polonium), dustItem(Materials.lead));
        recipe(dustItem(Materials.protactinium_233), isotopeItem("uranium/233"));
        recipe(dustItem(Materials.bismuth), dustItem(Materials.thallium));
        recipe(dustItem(Materials.tbp), isotopeItem("thorium/230"));
        recipe(dustItem(Materials.strontium_90), dustItem(Materials.zirconium));
        recipe(dustItem(Materials.ruthenium_106), dustItem(Materials.palladium));
        recipe(dustItem(Materials.caesium_137), dustItem(Materials.barium));
        recipe(dustItem(Materials.promethium_147), dustItem(Materials.neodymium));
        recipe(dustItem(Materials.europium_155), dustItem(Materials.gadolinium));
        recipe(isotopeItem("uranium/233"), dustItem(Materials.bismuth));
        recipe(isotopeItem("uranium/235"), dustItem(Materials.lead));
        recipe(isotopeItem("uranium/238"), dustItem(Materials.radium));
        recipe(isotopeItem("neptunium/236"), dustItem(Materials.thorium));
        recipe(isotopeItem("neptunium/237"), isotopeItem("uranium/233"));
        recipe(isotopeItem("plutonium/238"), dustItem(Materials.lead));
        recipe(isotopeItem("plutonium/239"), isotopeItem("uranium/233"));
        recipe(isotopeItem("plutonium/241"), isotopeItem("neptunium/237"));
        recipe(isotopeItem("plutonium/242"), isotopeItem("uranium/238"));
        recipe(isotopeItem("americium/241"), isotopeItem("neptunium/237"));
        recipe(isotopeItem("americium/242"), dustItem(Materials.lead));
        recipe(isotopeItem("americium/243"), isotopeItem("plutonium/239"));
        recipe(isotopeItem("curium/243"), isotopeItem("plutonium/239"));
        recipe(isotopeItem("curium/245"), isotopeItem("plutonium/241"));
        recipe(isotopeItem("curium/246"), isotopeItem("plutonium/242"));
        recipe(isotopeItem("curium/247"), isotopeItem("americium/243"));
        recipe(isotopeItem("berkelium/247"), isotopeItem("americium/243"));
        recipe(isotopeItem("berkelium/248"), dustItem(Materials.thorium));
        recipe(isotopeItem("californium/249"), isotopeItem("curium/245"));
        recipe(isotopeItem("californium/250"), isotopeItem("curium/246"));
        recipe(isotopeItem("californium/251"), isotopeItem("curium/247"));
        recipe(isotopeItem("californium/252"), dustItem(Materials.thorium));
    }

    private static void recipe(Item input, ItemStack output, double... params) {
        itemToItemRecipe(Processors.DECAY_HASTENER, NcIngredient.of(input), output, params);

    }

    private static void recipe(ItemStack itemStack, Item out, double... params) {
        NcIngredient in = NcIngredient.stack(itemStack);
        itemToItemRecipe(Processors.DECAY_HASTENER, in, out, params);
    }

    private static void recipe(TagKey<Item> itemTagKey, Item item, double... params) {
        itemToItemRecipe(Processors.DECAY_HASTENER, NcIngredient.of(itemTagKey), item, params);
    }

    public static void recipe(Item input, Item output, double... params)
    {
        itemToItemRecipe(Processors.DECAY_HASTENER, NcIngredient.of(input), output, params);
    }

    public static void recipe(NcIngredient input, Item output, double... params)
    {
        itemToItemRecipe(Processors.DECAY_HASTENER, input, output, params);
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
