package igentuman.nc.datagen.recipes.recipes;

import com.google.common.collect.Lists;
import igentuman.nc.content.processors.Processors;
import igentuman.nc.content.materials.Materials;
import igentuman.nc.setup.registration.NCItems;
import net.minecraft.data.recipes.FinishedRecipe;

import java.util.function.Consumer;

import static igentuman.nc.setup.registration.NCItems.ALL_NC_ITEMS;
import static net.minecraft.world.item.Items.*;

public class ManufactoryRecipes extends AbstractRecipeProvider {

    public static void generate(Consumer<FinishedRecipe> consumer) {
        ManufactoryRecipes.consumer = consumer;
        ID = Processors.MANUFACTORY;
        for(String name: Materials.all().keySet()) {
            if(NCItems.NC_DUSTS.containsKey(name) && NCItems.INGOTS_TAG.containsKey(name)) {
                itemToItem(ingotIngredient(name), dustStack(name));
                continue;
            }
            if(NCItems.GEMS_TAG.containsKey(name) && NCItems.NC_DUSTS.containsKey(name)) {
                if(Materials.villiaumite.equals(name) || Materials.carobbiite.equals(name)) continue;
                itemToItem(gemIngredient(name), dustStack(name), 1.5D);
            }
        }
        itemToItem(dustIngredient(Materials.coal), dustStack(Materials.graphite), 0.5D, 1D);
        itemToItem(ingredient(COAL), dustStack(Materials.coal), 0.5D, 1D);
        itemToItem(ingredient(CHARCOAL), dustStack(Materials.charcoal), 0.5D, 0.5D);

        itemToItem(ingredient(DIAMOND), dustStack(Materials.diamond), 1.5D, 1.5D);
        itemToItem(ingotIngredient("iron"), dustStack(Materials.iron), 1.2D);
        itemToItem(ingotIngredient("netherite"), dustStack(Materials.netherite), 2.5D);
        itemToItem(ingotIngredient("gold"), dustStack(Materials.gold), 1.2D);
        itemToItem(ingotIngredient("copper"), dustStack(Materials.copper), 1.2D);
        itemToItem(ingredient(LAPIS_LAZULI), dustStack(Materials.lapis));
        itemToItem(gemIngredient(Materials.villiaumite), dustStack(Materials.sodium_fluoride));
        itemToItem(gemIngredient(Materials.carobbiite), dustStack(Materials.potassium_fluoride));
        itemToItem(ingredient(OBSIDIAN), dustStack(Materials.obsidian), 2D, 1D);
        itemToItem(ingredient(COBBLESTONE), ingredient(SAND));
        itemToItem(ingredient(GRAVEL), ingredient(FLINT));
        itemToItem(ingredient(END_STONE), dustStack(Materials.end_stone));

        itemToItem(ingredient(BLAZE_ROD), ingredient(BLAZE_POWDER, 4));
        itemToItem(ingredient(BONE), ingredient(BONE_MEAL, 6));
        itemToItem(ingredient(ROTTEN_FLESH, 4), ingredient(LEATHER), 0.5D, 1D);
        itemToItem(ingredient(SUGAR_CANE, 2), ingredient(NCItems.NC_PARTS.get("bioplastic").get()), 1D, 0.5D);

        itemToItem(ingredient(ALL_NC_ITEMS.get("roasted_cocoa_beans").get()), ingredient(ALL_NC_ITEMS.get("ground_cocoa_nibs").get()), 0.5D, 0.5D);
        itemToItem(ingredient(PORKCHOP), ingredient(NCItems.NC_ITEMS.get("gelatin").get(), 8), 0.5D, 0.5D);
        itemToItem(ingredient(SALMON), ingredient(NCItems.NC_ITEMS.get("gelatin").get(), 2), 0.5D, 0.5D);
        itemToItem(ingredient(COD), ingredient(NCItems.NC_ITEMS.get("gelatin").get(), 2), 0.5D, 0.5D);
    }
}
