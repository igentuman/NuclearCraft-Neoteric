package igentuman.nc.registry;

import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.registries.DeferredHolder;

public class RecipeSerializerRegistryObject<RECIPE extends Recipe<?>> extends WrappedRegistryObject<RecipeSerializer<RECIPE>> {

    public RecipeSerializerRegistryObject(DeferredHolder<?, ?> registryObject) {
        super(registryObject);
    }
}
