package igentuman.nc.datagen.recipes.builder;

import com.google.gson.JsonObject;
import igentuman.nc.util.annotation.NothingNullByDefault;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import javax.annotation.Nullable;

import java.util.function.Consumer;

@NothingNullByDefault
public class SpecialRecipeBuilder implements IFinishedRecipe {

    private final IRecipeSerializer<?> serializer;

    private SpecialRecipeBuilder(IRecipeSerializer<?> serializer) {
        this.serializer = serializer;
    }


    public static void build(Consumer<IFinishedRecipe> consumer, IRecipeSerializer<?> serializer) {
        consumer.accept(new SpecialRecipeBuilder(serializer));
    }

    @Override
    public IRecipeSerializer<?> getType() {
        return serializer;
    }

    @Override
    public void serializeRecipeData(JsonObject json) {
        //NO-OP
    }

    private static <T extends IForgeRegistryEntry<T>> ResourceLocation getName(IForgeRegistry<T> registry, T element) {
        return registry.getKey(element);
    }

    public static ResourceLocation getName(IRecipeSerializer<?> element) {
        return getName(ForgeRegistries.RECIPE_SERIALIZERS, element);
    }

    @Override
    public ResourceLocation getId() {
        return getName(getType());
    }

    @Nullable
    @Override
    public JsonObject serializeAdvancement() {
        return null;
    }

    @Nullable
    @Override
    public ResourceLocation getAdvancementId() {
        return null;
    }
}