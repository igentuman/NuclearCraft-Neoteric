package igentuman.nc.recipes.lookup;

import igentuman.nc.block.entity.IBlockEntityHandler;
import igentuman.nc.recipes.INcRecipeTypeProvider;
import igentuman.nc.recipes.NcRecipe;
import igentuman.nc.recipes.cache.CachedRecipe;
import igentuman.nc.recipes.cache.IInputRecipeCache;
import igentuman.nc.util.inventory.IContentsListener;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IRecipeLookupHandler<RECIPE extends NcRecipe> extends IContentsListener {

    /**
     * @return The world for this {@link IRecipeLookupHandler}.
     */
    @Nullable
    default Level getHandlerWorld() {
        if (this instanceof BlockEntity tile) {
            return tile.getLevel();
        } else if (this instanceof Entity entity) {
            return entity.level;
        } else if(this instanceof IBlockEntityHandler) {
            return ((IBlockEntityHandler)this).getBlockEntity().getLevel();
        }
        return null;
    }

    /**
     * @return The recipe type this {@link IRecipeLookupHandler} handles.
     */
    @NotNull
    INcRecipeTypeProvider<RECIPE, ?> getRecipeType();

    /**
     * Returns how many operating ticks were saved for purposes of persisting through saves how far a cached recipe is through processing.
     *
     * @param cacheIndex The "recipe index" for which cache to interact with.
     *
     * @return Number of operating ticks that had passed before saving.
     */
    default int getSavedOperatingTicks(int cacheIndex) {
        return 0;
    }

    @Nullable
    RECIPE getRecipe();

    /**
     * Called when the cached recipe changes at a given index before processing the new cached recipe.
     *
     * @param cachedRecipe New cached recipe, or null if there is none due to the caches being invalidated.
     * @param cacheIndex   The "recipe index" for which cache to interact with.
     */
    default void onCachedRecipeChanged(@Nullable CachedRecipe<RECIPE> cachedRecipe, int cacheIndex) {
        clearRecipeErrors(cacheIndex);
    }

    /**
     * Called by {@link #onCachedRecipeChanged(CachedRecipe, int)} when the list of cached errors should be reset due to the recipe not being valid any more.
     *
     * @param cacheIndex The "recipe index" for which cache to interact with.
     */
    default void clearRecipeErrors(int cacheIndex) {
    }

    /**
     * Helper class that specifies the input cache's type for the recipe type. The reason it isn't defined in the main {@link IRecipeLookupHandler} is it isn't needed and
     * would just make the class definitions a lot messier with very long generics that can be folded away into the helper interfaces we use anyway ofr actual lookup
     * purposes.
     */
    interface IRecipeTypedLookupHandler<RECIPE extends NcRecipe, INPUT_CACHE extends IInputRecipeCache> extends IRecipeLookupHandler<RECIPE> {

        @NotNull
        @Override
        INcRecipeTypeProvider<RECIPE, INPUT_CACHE> getRecipeType();
    }

    interface ConstantUsageRecipeLookupHandler {

        /**
         * Returns how much of the constant secondary input had been used for purposes of persisting through saves how far a cached recipe is through processing.
         *
         * @param cacheIndex The "recipe index" for which cache to interact with.
         *
         * @return Constant amount of secondary input that had been used before saving.
         */
        default long getSavedUsedSoFar(int cacheIndex) {
            return 0;
        }
    }
}