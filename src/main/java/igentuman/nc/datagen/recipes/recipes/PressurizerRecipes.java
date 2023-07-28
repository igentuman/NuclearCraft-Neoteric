package igentuman.nc.datagen.recipes.recipes;

import igentuman.nc.setup.processors.Processors;
import igentuman.nc.setup.registration.Materials;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

import java.util.function.Consumer;

import static igentuman.nc.setup.registration.NCItems.*;

public class PressurizerRecipes extends AbstractRecipeProvider {

    public static void generate(Consumer<FinishedRecipe> consumer) {
        ID = Processors.PRESSURIZER;
        PressurizerRecipes.consumer = consumer;
        for (String name : Materials.all().keySet()) {
            if(INGOTS_TAG.get(name) != null && NC_PLATES.get(name) != null) {
                add(INGOTS_TAG.get(name), plateItem(name));
            }
        }

        add(dustTag(Materials.graphite), Items.COAL);
        add(ingotTag(Materials.graphite), ingotItem(Materials.pyrolitic_carbon));
        add(dustTag(Materials.diamond), Items.DIAMOND);
        add(dustTag(Materials.rhodochrosite), gemItem(Materials.rhodochrosite));
        add(dustTag(Materials.quartz), Items.QUARTZ);
        add(new ItemStack(dustItem(Materials.obsidian), 4), Item.byBlock(Blocks.OBSIDIAN));
        add(dustTag(Materials.boron_nitride), gemItem(Materials.boron_nitride));
        add(dustTag(Materials.fluorite), gemItem(Materials.fluorite));
        add(dustTag(Materials.villiaumite), gemItem(Materials.villiaumite));
        add(dustTag(Materials.carobbiite), gemItem(Materials.carobbiite));

    }
}
