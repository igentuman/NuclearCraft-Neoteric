package igentuman.nc.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;

import java.util.function.Supplier;

public class RecipeSerializerDeferredRegister extends WrappedDeferredRegister<RecipeSerializer<?>> {

    public RecipeSerializerDeferredRegister(String modid) {
        super(modid, Registries.RECIPE_SERIALIZER);
    }

    public <RECIPE extends Recipe<?>> RecipeSerializerRegistryObject<RECIPE> register(String name, Supplier<RecipeSerializer<RECIPE>> sup) {
        return register(name, sup, RecipeSerializerRegistryObject::new);
    }
}
