package igentuman.nc.setup.recipes;

import igentuman.nc.recipes.INcRecipeTypeProvider;
import igentuman.nc.recipes.NcRecipe;
import igentuman.nc.recipes.NcRecipeType;
import net.minecraftforge.registries.RegistryObject;

public class RecipeTypeRegistryObject<RECIPE extends NcRecipe> extends
      WrappedRegistryObject<NcRecipeType<RECIPE>> implements INcRecipeTypeProvider<RECIPE> {

    public RecipeTypeRegistryObject(RegistryObject<NcRecipeType<RECIPE>> registryObject) {
        super(registryObject);
    }

    @Override
    public NcRecipeType<RECIPE> getRecipeType() {
        return get();
    }
}