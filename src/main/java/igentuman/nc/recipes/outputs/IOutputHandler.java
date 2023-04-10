package igentuman.nc.recipes.outputs;


import igentuman.nc.recipes.cache.CachedRecipe;
import igentuman.nc.util.annotation.ParametersAreNotNullByDefault;

/**
 * Interface describing handling of an output.
 *
 * @param <OUTPUT> Type of output handled by this handler.
 */
@ParametersAreNotNullByDefault
public interface IOutputHandler<OUTPUT> {

    /**
     * Adds {@code operations} operations worth of {@code toOutput} to the output.
     *
     * @param toOutput   Output result.
     * @param operations Operations to perform.
     */
    void handleOutput(OUTPUT toOutput, int operations);

    void calculateOperationsCanSupport(CachedRecipe.OperationTracker tracker, OUTPUT toOutput);
}