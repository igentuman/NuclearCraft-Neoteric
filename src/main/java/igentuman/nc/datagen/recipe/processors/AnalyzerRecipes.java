package igentuman.nc.datagen.recipe.processors;

import igentuman.nc.datagen.recipe.UniversalProcessorRecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.Items;

import static igentuman.nc.datagen.recipe.ModRecipeProvider.*;
import static igentuman.nc.setup.entries.Processors.ANALYZER;

public class AnalyzerRecipes {

    public static void analyzer(RecipeOutput out) {
        UniversalProcessorRecipeBuilder.processor(ANALYZER)
                .itemInput(Items.PAPER)
                .itemOutput(part("research_paper"))
                .processTime(500)
                .energyPerTick(200)
                .save(out, "paper_to_research_paper");

        UniversalProcessorRecipeBuilder.processor(ANALYZER)
                .itemInput(Items.FILLED_MAP)
                .itemOutput(Items.FILLED_MAP)
                .processTime(1100)
                .energyPerTick(500)
                .save(out, "map_analysis");

        UniversalProcessorRecipeBuilder.processor(ANALYZER)
                .itemInput(part("resonite_crystal"))
                .itemOutput(part("resonite_crystal"))
                .processTime(1600)
                .energyPerTick(800)
                .save(out, "resonite_crystal_analysis");
    }
}
