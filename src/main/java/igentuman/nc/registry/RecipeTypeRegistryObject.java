package igentuman.nc.registry;

import igentuman.nc.recipes.AbstractRecipe;
import igentuman.nc.recipes.INcRecipeTypeProvider;
import igentuman.nc.recipes.NcRecipeType;
import igentuman.nc.recipes.type.NcRecipe;
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