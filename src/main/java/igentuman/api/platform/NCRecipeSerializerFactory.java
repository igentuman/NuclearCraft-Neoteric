package igentuman.api.platform;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.Recipe;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Factory for bridging NC's existing JSON/network recipe parsing logic into
 * NeoForge 1.21.1's {@link MapCodec}/{@link StreamCodec} interfaces.
 *
 * <p>In NeoForge 1.21.1, {@code RecipeSerializer} no longer declares
 * {@code fromJson}/{@code fromNetwork}/{@code toNetwork}. Instead it must provide:
 * <ul>
 *   <li>{@code MapCodec<T> codec()} — for JSON serialization/deserialization</li>
 *   <li>{@code StreamCodec<RegistryFriendlyByteBuf, T> streamCodec()} — for network sync</li>
 * </ul>
 *
 * <p>NC serializers keep their parsing code unchanged. This factory wraps it in the
 * NeoForge interfaces they never touch directly.
 */
public final class NCRecipeSerializerFactory {

    private NCRecipeSerializerFactory() {}

    /**
     * Create a {@link MapCodec} that delegates JSON parsing to the given reader function.
     *
     * <p>The codec reconstructs a {@link JsonObject} from the DFU map representation
     * and passes it to the existing parsing logic.
     *
     * @param jsonReader function that parses a recipe from a JsonObject
     * @return a MapCodec wrapping the reader
     */
    public static <T extends Recipe<?>> MapCodec<T> createCodec(
            Function<JsonObject, T> jsonReader) {
        return new MapCodec<>() {
            @Override
            public <O> RecordBuilder<O> encode(T recipe, DynamicOps<O> ops, RecordBuilder<O> prefix) {
                // NC recipes don't need JSON encoding at runtime.
                // Data generation uses its own serialization path.
                return prefix;
            }

            @Override
            public <O> DataResult<T> decode(DynamicOps<O> ops, MapLike<O> input) {
                // Reconstruct a JsonObject from the DFU map data.
                // When loading from JSON, ops is JsonOps.INSTANCE and values are JsonElements.
                JsonObject json = new JsonObject();
                input.entries().forEach(pair -> {
                    String key = ops.getStringValue(pair.getFirst())
                            .result().orElse("");
                    JsonElement element = new Dynamic<>(ops, pair.getSecond())
                            .convert(JsonOps.INSTANCE).getValue();
                    json.add(key, element);
                });
                try {
                    return DataResult.success(jsonReader.apply(json));
                } catch (Exception e) {
                    return DataResult.error(() -> "Failed to parse NC recipe: " + e.getMessage());
                }
            }

            @Override
            public <O> Stream<O> keys(DynamicOps<O> ops) {
                return Stream.empty();
            }
        };
    }

    /**
     * Create a {@link StreamCodec} wrapping existing network read/write functions.
     *
     * @param networkReader function that reads a recipe from a buffer
     * @param networkWriter function that writes a recipe to a buffer
     * @return a StreamCodec wrapping the reader/writer
     */
    public static <T extends Recipe<?>> StreamCodec<RegistryFriendlyByteBuf, T> createStreamCodec(
            Function<RegistryFriendlyByteBuf, T> networkReader,
            BiConsumer<RegistryFriendlyByteBuf, T> networkWriter) {
        return StreamCodec.of(
                (buf, recipe) -> networkWriter.accept(buf, recipe),
                buf -> networkReader.apply(buf)
        );
    }
}
