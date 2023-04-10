package igentuman.nc.recipes.lookup.monitor;

import igentuman.nc.handler.CommonWorldTickHandler;
import igentuman.nc.recipes.NcRecipe;
import igentuman.nc.recipes.cache.CachedRecipe;
import igentuman.nc.recipes.cache.ICachedRecipeHolder;
import igentuman.nc.recipes.lookup.IRecipeLookupHandler;
import igentuman.nc.util.inventory.IContentsListener;
import igentuman.nc.util.math.FloatingLong;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RecipeCacheLookupMonitor<RECIPE extends NcRecipe> implements ICachedRecipeHolder<RECIPE>, IContentsListener {

    private final IRecipeLookupHandler<RECIPE> handler;
    protected final int cacheIndex;
    protected CachedRecipe<RECIPE> cachedRecipe;
    protected boolean hasNoRecipe;

    public RecipeCacheLookupMonitor(IRecipeLookupHandler<RECIPE> handler) {
        this(handler, 0);
    }

    public RecipeCacheLookupMonitor(IRecipeLookupHandler<RECIPE> handler, int cacheIndex) {
        this.handler = handler;
        this.cacheIndex = cacheIndex;
    }

    protected boolean cachedIndexMatches(int cacheIndex) {
        return this.cacheIndex == cacheIndex;
    }

    @Override
    public final void onContentsChanged() {
        handler.onContentsChanged();
        onChange();
    }

    public void onChange() {
        //Mark that we may have a recipe again
        hasNoRecipe = false;
    }



    public boolean updateAndProcess() {
        CachedRecipe<RECIPE> oldCache = cachedRecipe;
        cachedRecipe = getUpdatedCache(cacheIndex);
        if (cachedRecipe != oldCache) {
            handler.onCachedRecipeChanged(cachedRecipe, cacheIndex);
        }
        if (cachedRecipe != null) {
            cachedRecipe.process();
            return true;
        }
        return false;
    }

    @Override
    public void loadSavedData(@NotNull CachedRecipe<RECIPE> cached, int cacheIndex) {
        if (cachedIndexMatches(cacheIndex)) {
            ICachedRecipeHolder.super.loadSavedData(cached, cacheIndex);
        }
    }

    @Override
    public int getSavedOperatingTicks(int cacheIndex) {
        return cachedIndexMatches(cacheIndex) ? handler.getSavedOperatingTicks(cacheIndex) : ICachedRecipeHolder.super.getSavedOperatingTicks(cacheIndex);
    }

    @Nullable
    @Override
    public CachedRecipe<RECIPE> getCachedRecipe(int cacheIndex) {
        return cachedIndexMatches(cacheIndex) ? cachedRecipe : null;
    }

    @Nullable
    @Override
    public RECIPE getRecipe() {
        return cachedIndexMatches(cacheIndex) ? handler.getRecipe() : null;
    }

    @Nullable
    @Override
    public CachedRecipe<RECIPE> createNewCachedRecipe(@NotNull RECIPE recipe, int cacheIndex) {
        return null;
    }

    @Override
    public boolean invalidateCache() {
        return CommonWorldTickHandler.flushTagAndRecipeCaches;
    }

    @Override
    public void setHasNoRecipe(int cacheIndex) {
        if (cachedIndexMatches(cacheIndex)) {
            hasNoRecipe = true;
        }
    }

    @Override
    public boolean hasNoRecipe(int cacheIndex) {
        return cachedIndexMatches(cacheIndex) ? hasNoRecipe : ICachedRecipeHolder.super.hasNoRecipe(cacheIndex);
    }
}