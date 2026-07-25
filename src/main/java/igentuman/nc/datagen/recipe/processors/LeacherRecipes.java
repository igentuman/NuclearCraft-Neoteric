package igentuman.nc.datagen.recipe.processors;

import igentuman.nc.datagen.recipe.UniversalProcessorRecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import static igentuman.nc.datagen.recipe.ModRecipeProvider.*;
import static igentuman.nc.setup.entries.Processors.LEACHER;

public class LeacherRecipes {

    private static final String[] SLURRY_MATERIALS = {
            "uranium", "thorium", "lead", "silver", "copper", "iron",
            "platinum", "gold", "cobalt", "nickel", "lithium", "aluminum",
            "magnesium", "zinc", "tin", "boron", "sodium"
    };

    public static void leacher(RecipeOutput out) {
        for (String material : SLURRY_MATERIALS) {
            TagKey<Item> oreTag = ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "ores/" + material));
            UniversalProcessorRecipeBuilder.processor(LEACHER)
                    .itemInput(oreTag)
                    .fluidInput(fluidOf("aqua_regia_acid"), 250)
                    .fluidOutput(fluidOf(material + "_slurry"), 1000)
                    .processTime(TIME)
                    .energyPerTick(ENERGY)
                    .save(out, material + "_slurry");
        }
    }
}
