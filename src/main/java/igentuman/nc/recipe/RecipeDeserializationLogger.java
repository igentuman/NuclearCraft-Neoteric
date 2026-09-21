package igentuman.nc.recipe;

import igentuman.nc.NuclearCraft;
import igentuman.nc.config.Common;
import igentuman.nc.registration.ModEntry;
import igentuman.nc.setup.ModEntries;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.List;

/** Logs processor recipes whose decoded values resolve to no registered content. */
public final class RecipeDeserializationLogger {

    private RecipeDeserializationLogger() {
    }

    @SuppressWarnings("unchecked")
    public static void logProcessorRecipes(RecipeManager recipeManager) {
        if (!Common.DEBUG_LOGGING.get()) return;

        int loaded = 0;
        int incomplete = 0;
        for (ModEntry entry : ModEntries.ENTRIES.values()) {
            if (entry.recipeType() == null || entry.recipeSerializer() == null
                    || !(entry.recipeSerializer().get() instanceof UniversalProcessorRecipeSerializer)) {
                continue;
            }

            RecipeType<UniversalProcessorRecipe> recipeType =
                    (RecipeType<UniversalProcessorRecipe>) entry.recipeType().get();
            for (var holder : recipeManager.getAllRecipesFor(recipeType)) {
                loaded++;
                List<String> problems = holder.value().getResolutionProblems();
                if (problems.isEmpty()) continue;

                incomplete++;
                for (String problem : problems) {
                    NuclearCraft.LOGGER.debug(
                            "[Recipes] Incomplete recipe {} for processor '{}': {}",
                            holder.id(), holder.value().getProcessorName(), problem
                    );
                }
            }
        }
        NuclearCraft.LOGGER.debug(
                "[Recipes] Deserialized {} processor recipes; {} had unresolved inputs or outputs",
                loaded, incomplete
        );
    }
}
