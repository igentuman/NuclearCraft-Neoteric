package igentuman.nc.recipes.inputs;


import igentuman.nc.recipes.cache.CachedRecipe;
import igentuman.nc.recipes.ingredient.InputIngredient;
import igentuman.nc.util.annotation.NothingNullByDefault;

/**
 * Interface describing handling of an input.
 *
 * @param <INPUT> Type of input handled by this handler.
 */
@NothingNullByDefault
public interface IInputHandler<INPUT> {

    /**
     * Returns the currently stored input.
     *
     * <p>
     * <strong>IMPORTANT:</strong> This input <em>MUST NOT</em> be modified. This method is not for altering an input's contents. Any implementers who
     * are able to detect modification through this method should throw an exception.
     * </p>
     * <p>
     * <strong><em>SERIOUSLY: DO NOT MODIFY THE RETURNED INPUT</em></strong>
     * </p>
     *
     * @return Input stored.
     *
     * @apiNote <strong>IMPORTANT:</strong> Do not modify this value.
     */
    INPUT getInput();

    /**
     * Gets a copy of the recipe's ingredient that matches the stored input.
     *
     * @param recipeIngredient Recipe ingredient.
     *
     * @return Matching instance. The returned value can be safely modified after.
     */
    INPUT getRecipeInput(InputIngredient<INPUT> recipeIngredient);

    /**
     * Adds {@code operations} operations worth of {@code recipeInput} from the input.
     *
     * @param recipeInput Recipe input result.
     * @param operations  Operations to perform.
     */
    void use(INPUT recipeInput, int operations);
    default void calculateOperationsCanSupport(CachedRecipe.OperationTracker tracker, INPUT recipeInput) {
        calculateOperationsCanSupport(tracker, recipeInput, 1);
    }
    void calculateOperationsCanSupport(CachedRecipe.OperationTracker tracker, INPUT recipeInput, int usageMultiplier);
}