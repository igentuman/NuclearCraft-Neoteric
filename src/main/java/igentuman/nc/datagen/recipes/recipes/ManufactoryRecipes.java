package igentuman.nc.datagen.recipes.recipes;

import igentuman.nc.setup.processors.Processors;
import igentuman.nc.setup.registration.Materials;
import igentuman.nc.setup.registration.NCItems;
import net.minecraft.data.recipes.FinishedRecipe;

import java.util.function.Consumer;

import static igentuman.nc.util.DataGenUtil.forgeIngot;
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
                itemToItem(gemIngredient(name), dustStack(name), 1.5D);
            }
        }
        itemToItem(ingredient(COAL), dustStack(Materials.coal), 0.5D, 1D);
        itemToItem(ingredient(CHARCOAL), dustStack(Materials.charcoal), 0.5D, 0.5D);

        itemToItem(ingredient(DIAMOND), dustStack(Materials.diamond), 1.5D, 1.5D);
        itemToItem(ingotIngredient("iron"), dustStack(Materials.iron), 1.2D);
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
    }
}
