package igentuman.nc.setup.recipes;

import igentuman.nc.recipes.INcRecipeTypeProvider;
import igentuman.nc.recipes.NcRecipe;
import igentuman.nc.recipes.NcRecipeType;
import igentuman.nc.recipes.cache.IInputRecipeCache;
import net.minecraftforge.registries.RegistryObject;

public class RecipeTypeRegistryObject<RECIPE extends NcRecipe, INPUT_CACHE extends IInputRecipeCache> extends
      WrappedRegistryObject<NcRecipeType<RECIPE, INPUT_CACHE>> implements INcRecipeTypeProvider<RECIPE, INPUT_CACHE> {

    public RecipeTypeRegistryObject(RegistryObject<NcRecipeType<RECIPE, INPUT_CACHE>> registryObject) {
        super(registryObject);
    }

    @Override
    public NcRecipeType<RECIPE, INPUT_CACHE> getRecipeType() {
        return get();
    }
}