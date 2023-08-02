package igentuman.nc.datagen.recipes.recipes;

import igentuman.nc.setup.processors.Processors;
import igentuman.nc.setup.registration.Materials;
import igentuman.nc.setup.registration.NCItems;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;

import static net.minecraft.world.item.Items.*;

public class ManufactoryRecipes extends AbstractRecipeProvider {

    public static void generate(Consumer<FinishedRecipe> consumer) {
        ManufactoryRecipes.consumer = consumer;
        ID = Processors.MANUFACTORY;
        for(String name: Materials.all().keySet()) {
            if(NCItems.NC_DUSTS.containsKey(name) && NCItems.INGOTS_TAG.containsKey(name)) {
                add(NCItems.INGOTS_TAG.get(name), dustItem(name));
                continue;
            }
            if(NCItems.GEMS_TAG.containsKey(name) && NCItems.NC_DUSTS.containsKey(name)) {
                add(NCItems.GEMS_TAG.get(name), dustItem(name), 1.5D);
            }
        }
        add(COAL, dustItem(Materials.coal), 0.5D, 1D);
        add(CHARCOAL, dustItem(Materials.charcoal), 0.5D, 0.5D);

        add(DIAMOND, dustItem(Materials.diamond), 1.5D, 1.5D);
        add(IRON_INGOT, dustItem(Materials.iron), 1.2D);
        add(GOLD_INGOT, dustItem(Materials.gold), 1.2D);
        add(COPPER_INGOT, dustItem(Materials.copper), 1.2D);
        add(LAPIS_LAZULI, dustItem(Materials.lapis));
        add(dustTag(Materials.villiaumite), dustItem(Materials.sodium_fluoride));
        add(NCItems.DUSTS_TAG.get(Materials.carobbiite), dustItem(Materials.potassium_fluoride));
        add(OBSIDIAN, dustItem(Materials.obsidian), 2D, 1D);
        add(COBBLESTONE, SAND);
        add(GRAVEL, FLINT);
        add(END_STONE, dustItem(Materials.end_stone));

        add(BLAZE_ROD, new ItemStack(BLAZE_POWDER, 4));
        add(BONE, new ItemStack(BONE_MEAL, 6));
        add(new ItemStack(ROTTEN_FLESH, 4), LEATHER, 0.5D, 1D);
        add(new ItemStack(SUGAR_CANE, 2), NCItems.NC_PARTS.get("bioplastic").get(), 1D, 0.5D);
    }
}
