package igentuman.nc.datagen.recipe;

import igentuman.nc.recipe.ItemOutput;
import igentuman.nc.recipe.bomb.NuclearBlastRecipe;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Blocks;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.datagen.recipe.ModRecipeProvider.blockItem;

public class NuclearBlastRecipes {

    private static final TagKey<Item> PLATINUM_ORES =
            ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "ores/platinum"));

    public static void nuclearBlast(RecipeOutput out) {
        blast(out, "coal_ores",
                Ingredient.of(ItemTags.COAL_ORES),
                ItemOutput.of(Blocks.DEEPSLATE_DIAMOND_ORE, 1), 1.0);

        Item kumanderite = blockItem("kumanderite");
        if (kumanderite != null) {
            blast(out, "ores_platinum",
                    Ingredient.of(PLATINUM_ORES),
                    ItemOutput.of(kumanderite, 1), 0.75);
        }
    }

    private static void blast(RecipeOutput out, String name, Ingredient input, ItemOutput output, double chance) {
        NuclearBlastRecipe recipe = new NuclearBlastRecipe(input, output, chance);
        out.accept(ResourceLocation.fromNamespaceAndPath(MODID, "nuclear_blast/" + name), recipe, null);
    }
}
