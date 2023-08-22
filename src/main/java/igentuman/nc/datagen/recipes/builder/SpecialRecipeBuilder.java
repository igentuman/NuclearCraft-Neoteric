package igentuman.nc.datagen.recipes.builder;

import com.google.gson.JsonObject;
import igentuman.nc.setup.recipes.RecipeSerializerRegistryObject;
import igentuman.nc.util.annotation.NothingNullByDefault;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

@NothingNullByDefault
public class SpecialRecipeBuilder implements FinishedRecipe {

    private final RecipeSerializer<?> serializer;

    private SpecialRecipeBuilder(RecipeSerializer<?> serializer) {
        this.serializer = serializer;
    }

    public static void build(Consumer<FinishedRecipe> consumer, RecipeSerializerRegistryObject<?> serializer) {
        build(consumer, serializer.get());
    }

    public static void build(Consumer<FinishedRecipe> consumer, RecipeSerializer<?> serializer) {
        consumer.accept(new SpecialRecipeBuilder(serializer));
    }

    @Override
    public RecipeSerializer<?> getType() {
        return serializer;
    }

    @Override
    public void serializeRecipeData(JsonObject json) {
        //NO-OP
    }

    private static <T> ResourceLocation getName(IForgeRegistry<T> registry, T element) {
        return registry.getKey(element);
    }

    public static ResourceLocation getName(RecipeSerializer<?> element) {
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