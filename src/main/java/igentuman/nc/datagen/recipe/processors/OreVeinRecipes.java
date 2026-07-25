package igentuman.nc.datagen.recipe.processors;

import igentuman.nc.recipe.OreVeinRecipe;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

import java.util.ArrayList;
import java.util.List;

import static igentuman.nc.NuclearCraft.MODID;

public class OreVeinRecipes {

    public static void oreVeins(RecipeOutput out) {
        vein(out, "uraninite", new Object[][]{
                {"uranium", 70}, {"thorium", 20}, {"lead", 7}, {"silver", 3}
        }, 2.0);

        vein(out, "bornite", new Object[][]{
                {"copper", 70}, {"iron", 30}
        }, 1.0);

        vein(out, "platinum", new Object[][]{
                {"platinum", 70}, {"gold", 30}
        }, 2.0);

        vein(out, "cobaltite", new Object[][]{
                {"cobalt", 99}, {"nickel", 1}
        }, 1.0);

        vein(out, "spodumene", new Object[][]{
                {"lithium", 95}, {"aluminum", 5}
        }, 1.0);

        vein(out, "magnesite", new Object[][]{
                {"magnesium", 95}
        }, 1.0);

        vein(out, "sphalerite", new Object[][]{
                {"lead", 10}, {"zinc", 90}
        }, 1.0);

        vein(out, "cassiterite", new Object[][]{
                {"lead", 10}, {"tin", 90}
        }, 1.0);

        vein(out, "borax", new Object[][]{
                {"boron", 90}, {"sodium", 10}
        }, 1.0);
    }

    private static void vein(RecipeOutput out, String name, Object[][] ores, double rarity) {
        List<OreVeinRecipe.OreEntry> entries = new ArrayList<>();
        for (Object[] ore : ores) {
            String material = (String) ore[0];
            int weight = (int) ore[1];
            TagKey<Item> tag = ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "ores/" + material));
            SizedIngredient ingredient = new SizedIngredient(Ingredient.of(tag), 1);
            entries.add(new OreVeinRecipe.OreEntry(ingredient, weight));
        }
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(MODID, name);
        OreVeinRecipe recipe = new OreVeinRecipe(id, entries, rarity);
        out.accept(id, recipe, null);
    }
}
