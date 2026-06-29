package igentuman.nc.compat.jei;

import igentuman.nc.recipe.UniversalProcessorRecipe;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.runtime.IJeiRuntime;

import java.util.List;
import java.util.Map;

public class JeiHelper {

    private static IJeiRuntime runtime;
    private static Map<String, RecipeType<UniversalProcessorRecipe>> recipeTypes;

    public static void setRuntime(IJeiRuntime jeiRuntime) {
        runtime = jeiRuntime;
    }

    public static void setRecipeTypes(Map<String, RecipeType<UniversalProcessorRecipe>> types) {
        recipeTypes = types;
    }

    public static void displayRecipes(String entryName) {
        if (runtime == null || recipeTypes == null) return;
        RecipeType<UniversalProcessorRecipe> type = recipeTypes.get(entryName);
        if (type == null) return;
        runtime.getRecipesGui().showTypes(List.of(type));
    }
}
