package igentuman.nc.datagen.recipes.recipes;

import com.google.common.collect.Lists;
import igentuman.nc.content.processors.Processors;
import igentuman.nc.content.materials.Materials;
import igentuman.nc.setup.registration.NCItems;
import net.minecraft.data.recipes.FinishedRecipe;

import java.util.function.Consumer;

import static igentuman.nc.setup.registration.NCItems.ALL_NC_ITEMS;
import static igentuman.nc.setup.registration.Tags.GEMS_TAG;
import static igentuman.nc.setup.registration.Tags.INGOTS_TAG;
import static net.minecraft.world.item.Items.*;

public class ManufactoryRecipes extends AbstractRecipeProvider {

    public static void generate(Consumer<FinishedRecipe> consumer) {
        ManufactoryRecipes.consumer = consumer;
        ID = Processors.MANUFACTORY;
        for(String name: Materials.all().keySet()) {
            if(NCItems.NC_DUSTS.containsKey(name) && INGOTS_TAG.containsKey(name)) {
                itemToItem(ingotIngredient(name), dustIngredient(name));
                continue;
            }
            if(GEMS_TAG.containsKey(name) && NCItems.NC_DUSTS.containsKey(name)) {
                if(Materials.villiaumite.equals(name) || Materials.carobbiite.equals(name)) continue;
                itemToItem(gemIngredient(name), dustIngredient(name), 1.5D);
            }
        }
        itemToItem(dustIngredient(Materials.coal), dustIngredient(Materials.graphite), 0.5D, 1D);
        itemToItem(ingredient(COAL), dustIngredient(Materials.coal), 0.5D, 1D);
        itemToItem(ingredient(CHARCOAL), dustIngredient(Materials.charcoal), 0.5D, 0.5D);

        itemToItem(ingredient(DIAMOND), dustIngredient(Materials.diamond), 1.5D, 1.5D);
        itemToItem(ingredient(GLOWSTONE), ingredient(GLOWSTONE_DUST, 4), 0.5d, 0.5D);

        itemToItem(ingredient(EMERALD), dustIngredient(Materials.emerald), 1.2D, 1.2D);
        itemToItem(ingredient(ENDER_PEARL), dustIngredient(Materials.enderium), 1.2D, 0.5D);
        itemToItem(ingredient(QUARTZ), dustIngredient(Materials.quartz));
        itemToItem(ingotIngredient("iron"), dustIngredient(Materials.iron), 1.2D);
        itemToItem(ingotIngredient("netherite"), dustIngredient(Materials.netherite), 2.5D);
        itemToItem(ingotIngredient("gold"), dustIngredient(Materials.gold), 1.2D);
        itemToItem(ingotIngredient("copper"), dustIngredient(Materials.copper), 1.2D);
        itemToItem(ingredient(LAPIS_LAZULI), dustIngredient(Materials.lapis));
        itemToItem(gemIngredient(Materials.villiaumite), dustIngredient(Materials.sodium_fluoride));
        itemToItem(gemIngredient(Materials.carobbiite), dustIngredient(Materials.potassium_fluoride));
        itemToItem(ingredient(OBSIDIAN), dustIngredient(Materials.obsidian), 2D, 1D);
        itemToItem(ingredient(COBBLESTONE), ingredient(SAND));
        itemToItem(ingredient(GRAVEL), ingredient(FLINT));
        itemToItem(ingredient(END_STONE), dustIngredient(Materials.end_stone));

        itemToItem(ingredient(BLAZE_ROD), ingredient(BLAZE_POWDER, 4));
        itemToItem(ingredient(BONE), ingredient(BONE_MEAL, 6));
        itemToItem(ingredient(ROTTEN_FLESH, 4), ingredient(LEATHER), 0.5D, 1D);
        itemToItem(ingredient(SUGAR_CANE, 2), ingredient(NCItems.NC_PARTS.get("bioplastic").get()), 1D, 0.5D);

        itemToItem(ingredient(ALL_NC_ITEMS.get("roasted_cocoa_beans").get()), ingredient(ALL_NC_ITEMS.get("ground_cocoa_nibs").get()), 0.5D, 0.5D);
        itemToItem(ingredient(PORKCHOP), ingredient(NCItems.NC_ITEMS.get("gelatin").get(), 8), 0.5D, 0.5D);
        itemToItem(ingredient(SALMON), ingredient(NCItems.NC_ITEMS.get("gelatin").get(), 2), 0.5D, 0.5D);
        itemToItem(ingredient(COD), ingredient(NCItems.NC_ITEMS.get("gelatin").get(), 2), 0.5D, 0.5D);
        itemToItem(ingredient(WHEAT_SEEDS), ingredient(NCItems.NC_ITEMS.get("flour").get(), 2), 0.5D, 0.5D);
    }
}
