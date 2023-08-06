package igentuman.nc.setup.recipes;

import igentuman.nc.recipes.AbstractRecipe;
import igentuman.nc.recipes.INcRecipeTypeProvider;
import igentuman.nc.recipes.NcRecipeType;
import net.minecraftforge.registries.RegistryObject;

public class RecipeTypeRegistryObject<RECIPE extends AbstractRecipe> extends
      WrappedRegistryObject<NcRecipeType<RECIPE>> implements INcRecipeTypeProvider<RECIPE> {

    public RecipeTypeRegistryObject(RegistryObject<NcRecipeType<RECIPE>> registryObject) {
        super(registryObject);
    }

    @Override
    public NcRecipeType<RECIPE> getRecipeType() {
        return get();
    }
}