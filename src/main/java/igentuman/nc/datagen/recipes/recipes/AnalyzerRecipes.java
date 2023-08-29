package igentuman.nc.datagen.recipes.recipes;

import igentuman.nc.content.materials.Materials;
import igentuman.nc.content.processors.Processors;
import igentuman.nc.recipes.ingredient.NcIngredient;
import igentuman.nc.setup.registration.NCItems;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;

import static igentuman.nc.setup.registration.NCItems.ALL_NC_ITEMS;
import static igentuman.nc.setup.registration.NCItems.NC_PARTS;
import static net.minecraft.world.item.Items.*;

public class AnalyzerRecipes extends AbstractRecipeProvider {

    public static void generate(Consumer<FinishedRecipe> consumer) {
        AnalyzerRecipes.consumer = consumer;
        ID = Processors.ANALYZER;

        ItemStack dataPaper = new ItemStack(NC_PARTS.get("research_paper").get());
        dataPaper.getOrCreateTag().putBoolean("vein_data", true);

        itemToItem(ingredient(PAPER), NcIngredient.stack(dataPaper), 2.5D, 4D);

        ItemStack dataMap = new ItemStack(FILLED_MAP);
        dataMap.getOrCreateTag().putBoolean("is_nc_analyzed", true);

        itemToItem(ingredient(FILLED_MAP), NcIngredient.stack(dataMap), 5.5D, 10D);

    }
}
