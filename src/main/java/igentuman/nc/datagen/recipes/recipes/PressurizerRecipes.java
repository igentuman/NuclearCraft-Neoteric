package igentuman.nc.datagen.recipes.recipes;

import igentuman.nc.content.processors.Processors;
import igentuman.nc.content.materials.Materials;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

import java.util.function.Consumer;

import static igentuman.nc.setup.registration.NCBlocks.NC_BLOCKS;
import static igentuman.nc.setup.registration.NCItems.*;
import static igentuman.nc.setup.registration.Tags.INGOTS_TAG;
import static net.minecraft.world.item.Items.*;

public class PressurizerRecipes extends AbstractRecipeProvider {

    public static void generate(Consumer<FinishedRecipe> consumer) {
        ID = Processors.PRESSURIZER;
        PressurizerRecipes.consumer = consumer;
        for (String name : Materials.all().keySet()) {
            if(INGOTS_TAG.get(name) != null && NC_PLATES.get(name) != null) {
                if(Materials.graphite.equals(name)) continue;
                itemToItem(ingredient(ingotTag(name)), plateStack(name));
            }
        }
        itemToItem(ingredient(IRON_INGOT), plateStack(Materials.iron));
        itemToItem(ingredient(COPPER_INGOT), plateStack(Materials.copper));

        itemToItem(isotopeIngredient(Materials.americium241, 9), ingredient(NC_BLOCKS.get("americium241").get().asItem()));
        itemToItem(isotopeIngredient(Materials.uranium238, 9), ingredient(NC_BLOCKS.get("uranium238").get().asItem()));
        itemToItem(isotopeIngredient(Materials.californium250, 9), ingredient(NC_BLOCKS.get("californium250").get().asItem()));
        itemToItem(isotopeIngredient(Materials.plutonium238, 9), ingredient(NC_BLOCKS.get("plutonium238").get().asItem()));
        itemToItem(dustIngredient(Materials.graphite), plateStack(Materials.graphite));
        itemToItem(ingredient(ALL_NC_ITEMS.get("flour").get(), 2), ingredient(ALL_NC_ITEMS.get("graham_cracker").get()));
        itemToItem(ingredient(NC_FOOD.get("foursmore").get(), 2), ingredient(NC_FOOD.get("evenmoresmore").get()));
        itemToItem(ingredient(ingotTag(Materials.graphite)), ingotStack(Materials.pyrolitic_carbon));
        itemToItem(dustIngredient(Materials.diamond), ingredient(Items.DIAMOND));
        itemToItem(ingredient(NETHERITE_INGOT), plateStack(Materials.netherite));
        itemToItem(dustIngredient(Materials.rhodochrosite), gemStack(Materials.rhodochrosite));
        itemToItem(dustIngredient(Materials.quartz), ingredient(Items.QUARTZ));
        itemToItem(dustIngredient(Materials.obsidian, 4), ingredient(Item.byBlock(Blocks.OBSIDIAN)));
        itemToItem(dustIngredient(Materials.boron_nitride), gemStack(Materials.boron_nitride));
        itemToItem(dustIngredient(Materials.fluorite), gemStack(Materials.fluorite));
        itemToItem(dustIngredient(Materials.villiaumite), gemStack(Materials.villiaumite));
        itemToItem(dustIngredient(Materials.carobbiite), gemStack(Materials.carobbiite));

    }
}
