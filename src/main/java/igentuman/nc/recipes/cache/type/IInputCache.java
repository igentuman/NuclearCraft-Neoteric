package igentuman.nc.recipes.cache.type;

import igentuman.nc.recipes.NcRecipe;
import igentuman.nc.recipes.ingredient.IMultiIngredient;
import igentuman.nc.recipes.ingredient.InputIngredient;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

/**
 * Base interface describing how a specific input type is cached to allow for quick lookup of recipes by input both for finding the recipes and checking if any even exist
 * with the given input.
 */
public interface IInputCache<INPUT, INGREDIENT extends InputIngredient<INPUT>, RECIPE extends NcRecipe> {

    /**
     * Checks if this {@link IInputCache} knows about the given input.
     *
     * @param input Input to check.
     *
     * @return {@code true} if this cache does have the given input, {@code false} if there isn't.
     */
    boolean contains(INPUT input);

    /**
     * Checks if this {@link IInputCache} knows about the given input, and if it does, checks if any of the recipes that match that input type match the given recipe
     * predicate.
     *
     * @param input         Input to check.
     * @param matchCriteria Predicate to further validate recipes with.
     *
     * @return {@code true} if this cache does have the given input and a recipe that matches, {@code false} if there isn't.
     */
    boolean contains(INPUT input, Predicate<RECIPE> matchCriteria);

    /**
     * Finds the first recipe for the given input that matches the given match criteria. Note: that no validation is done here about the input matching the recipe's
     * criteria in regard to required amounts, all that is done regarding the input is that the type is used in the recipe.
     *
     * @param input         Input to check.
     * @param matchCriteria Predicate to further validate recipes with.
     *
     * @return Recipe for the given input that matches the given criteria, or {@code null} if no recipe matches.
     */
    @Nullable
    RECIPE findFirstRecipe(INPUT input, Predicate<RECIPE> matchCriteria);


    boolean mapInputs(RECIPE recipe, INGREDIENT inputIngredient);
    default boolean mapMultiInputs(RECIPE recipe, IMultiIngredient<INPUT, ? extends INGREDIENT> multi) {
        return multi.forEachIngredient(ingredient -> mapInputs(recipe, ingredient));
    }

    /**
     * Clears this {@link IInputCache}
     */
    void clear();

    /**
     * Helper method that is not actually used by the caches themselves, but allows for the broader {@link nuclearcraft.common.recipe.lookup.cache.IInputRecipeCache} to
     * easily check if an input is empty without requiring a bunch of extra dummy classes implementing empty checks based on the given generics.
     *
     * @param input Input to check
     *
     * @return {@code true} if the input is empty.
     */
    boolean isEmpty(INPUT input);
}