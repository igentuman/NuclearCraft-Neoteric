package igentuman.nc.compat.jei;

import igentuman.nc.recipe.UniversalProcessorRecipe;
import mezz.jei.api.recipe.types.IRecipeHolderType;
import mezz.jei.api.runtime.IJeiRuntime;

import java.util.List;
import java.util.Map;

/** JEI integration helper that opens the recipe view for a named processor entry. */
public class JeiHelper {

    private static IJeiRuntime runtime;
    private static Map<String, IRecipeHolderType<UniversalProcessorRecipe>> recipeTypes;

    public static void setRuntime(IJeiRuntime jeiRuntime) {
        runtime = jeiRuntime;
    }

    public static void setRecipeTypes(Map<String, IRecipeHolderType<UniversalProcessorRecipe>> types) {
        recipeTypes = types;
    }

    public static void clearRuntime() {
        runtime = null;
        recipeTypes = null;
    }

    public static void displayRecipes(String entryName) {
        if (runtime == null || recipeTypes == null) return;
        IRecipeHolderType<UniversalProcessorRecipe> type = recipeTypes.get(entryName);
        if (type == null) return;
        runtime.getRecipesGui().showTypes(List.of(type));
    }
}
