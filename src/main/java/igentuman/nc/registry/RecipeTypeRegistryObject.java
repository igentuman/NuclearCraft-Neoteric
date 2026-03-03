package igentuman.nc.registry;

import igentuman.nc.recipes.INcRecipeTypeProvider;
import igentuman.nc.recipes.NcRecipeType;
import igentuman.nc.recipes.type.NcRecipe;
import net.neoforged.neoforge.registries.DeferredHolder;

public class RecipeTypeRegistryObject<RECIPE extends NcRecipe> extends
        WrappedRegistryObject<NcRecipeType<RECIPE>> implements INcRecipeTypeProvider<RECIPE> {

    public RecipeTypeRegistryObject(DeferredHolder<?, ?> registryObject) {
        super(registryObject);
    }

    @Override
    public NcRecipeType<RECIPE> getRecipeType() {
        return get();
    }
}
