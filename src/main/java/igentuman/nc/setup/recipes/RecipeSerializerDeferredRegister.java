package igentuman.nc.setup.recipes;

import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

public class RecipeSerializerDeferredRegister extends WrappedDeferredRegister<RecipeSerializer<?>> {

    public RecipeSerializerDeferredRegister(String modid) {
        super(modid, ForgeRegistries.RECIPE_SERIALIZERS);
    }

    public <RECIPE extends Recipe<?>> RecipeSerializerRegistryObject<RECIPE> register(String name, Supplier<RecipeSerializer<RECIPE>> sup) {
        return register(name, sup, RecipeSerializerRegistryObject::new);
    }
}