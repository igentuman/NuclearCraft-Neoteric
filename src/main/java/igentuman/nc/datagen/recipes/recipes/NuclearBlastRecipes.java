package igentuman.nc.datagen.recipes.recipes;

import igentuman.nc.recipes.ingredient.NcIngredient;
import igentuman.nc.datagen.recipes.builder.NcRecipeBuilder;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.level.block.Blocks;
import java.util.List;
import java.util.function.Consumer;

import static igentuman.nc.content.materials.Materials.kumanderite;
import static igentuman.nc.setup.registration.NCBlocks.NC_BLOCKS;
import static igentuman.nc.setup.registration.NCBlocks.NC_MATERIAL_BLOCKS;
import static igentuman.nc.setup.registration.Tags.forgeOre;

public class NuclearBlastRecipes extends AbstractRecipeProvider {

    public static void generate(Consumer<FinishedRecipe> consumer) {
        NuclearBlastRecipes.consumer = consumer;
        ID = "nuclear_blast";

        NcRecipeBuilder.get(ID)
                .items(List.of(NcIngredient.of(ItemTags.COAL_ORES)), List.of(NcIngredient.stack(stack(Blocks.DEEPSLATE_DIAMOND_ORE, 1))))
                .modifiers(1.0, 1.0, 1.0, 1.0)
                .build(consumer);

        NcRecipeBuilder.get(ID)
                .items(List.of(NcIngredient.of(forgeOre("platinum"))), List.of(NcIngredient.stack(stack(NC_MATERIAL_BLOCKS.get(kumanderite).get(), 1))))
                .modifiers(1.0, 1.0, 1.0, 0.75)
                .build(consumer);
    }
}
