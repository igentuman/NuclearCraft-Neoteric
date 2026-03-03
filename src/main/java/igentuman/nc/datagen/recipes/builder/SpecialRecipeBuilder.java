package igentuman.nc.datagen.recipes.builder;

import igentuman.nc.registry.RecipeSerializerRegistryObject;
import igentuman.nc.util.annotation.NothingNullByDefault;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;

@NothingNullByDefault
public class SpecialRecipeBuilder {

    private final RecipeSerializer<?> serializer;

    private SpecialRecipeBuilder(RecipeSerializer<?> serializer) {
        this.serializer = serializer;
    }

    public static void build(RecipeOutput output, RecipeSerializerRegistryObject<?> serializer) {
        build(output, serializer.get());
    }

    public static void build(RecipeOutput output, RecipeSerializer<?> serializer) {
        ResourceLocation id = getName(serializer);
        // Special recipes are simple — no data, just the serializer type.
        // Create a minimal recipe via the serializer's codec.
        // The serializer knows how to create the recipe from empty data.
        net.minecraft.world.item.crafting.Recipe<?> recipe = serializer.codec().codec()
                .parse(com.mojang.serialization.JsonOps.INSTANCE, new com.google.gson.JsonObject())
                .getOrThrow(msg -> new IllegalStateException("Failed to create special recipe for " + id + ": " + msg));
        output.accept(id, recipe, null);
    }

    private static <T> ResourceLocation getName(Registry<T> registry, T element) {
        return registry.getKey(element);
    }

    public static ResourceLocation getName(RecipeSerializer<?> element) {
        return getName(BuiltInRegistries.RECIPE_SERIALIZER, element);
    }
}
