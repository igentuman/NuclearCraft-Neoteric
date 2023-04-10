package igentuman.nc.recipes.inputs;


import igentuman.nc.recipes.cache.CachedRecipe;
import igentuman.nc.util.annotation.NothingNullByDefault;

/**
 * Interface describing handling of an input that can handle long values.
 *
 * @param <INPUT> Type of input handled by this handler.
 */
@NothingNullByDefault
public interface ILongInputHandler<INPUT> extends IInputHandler<INPUT> {

    @Override
    default void use(INPUT recipeInput, int operations) {
        //Wrap to the long implementation
        use(recipeInput, (long) operations);
    }

    /**
     * Adds {@code operations} operations worth of {@code recipeInput} from the input.
     *
     * @param recipeInput Recipe input result.
     * @param operations  Operations to perform.
     */
    void use(INPUT recipeInput, long operations);

    @Override
    default void calculateOperationsCanSupport(CachedRecipe.OperationTracker tracker, INPUT recipeInput, int usageMultiplier) {
        //Wrap to the long implementation
        calculateOperationsCanSupport(tracker, recipeInput, (long) usageMultiplier);
    }

    void calculateOperationsCanSupport(CachedRecipe.OperationTracker tracker, INPUT recipeInput, long usageMultiplier);
}