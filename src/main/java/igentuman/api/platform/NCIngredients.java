package igentuman.api.platform;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * Platform translation layer for NeoForge 1.21.1 Ingredient APIs.
 *
 * <p>In NeoForge 1.21.1, {@link Ingredient} became {@code final} and its serialization
 * methods were replaced with codec/stream codec patterns. This class wraps the new APIs
 * so that NC code does not need to touch codec internals directly.
 *
 * <h3>Ingredient serialization</h3>
 * <ul>
 *   <li>{@code Ingredient.fromJson(json)} → {@link #fromJson(JsonElement)}</li>
 *   <li>{@code Ingredient.fromNetwork(buf)} → {@link #fromNetwork(RegistryFriendlyByteBuf)}</li>
 *   <li>{@code ingredient.toNetwork(buf)} → {@link #toNetwork(RegistryFriendlyByteBuf, Ingredient)}</li>
 *   <li>{@code ingredient.toJson()} → {@link #toJson(Ingredient)}</li>
 * </ul>
 *
 * <h3>Ingredient creation</h3>
 * <ul>
 *   <li>{@code Ingredient.fromValues(Stream.of(new Ingredient.TagValue(tag)))} → {@link #ofTag(TagKey)}</li>
 * </ul>
 */
public final class NCIngredients {

    private NCIngredients() {}

    /**
     * Parse an Ingredient from JSON.
     * Replaces {@code Ingredient.fromJson(json)} (removed in 1.21.1).
     */
    public static Ingredient fromJson(JsonElement json) {
        return Ingredient.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow();
    }

    /**
     * Read an Ingredient from a network buffer.
     * Replaces {@code Ingredient.fromNetwork(buf)} (removed in 1.21.1).
     */
    public static Ingredient fromNetwork(RegistryFriendlyByteBuf buf) {
        return Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
    }

    /**
     * Write an Ingredient to a network buffer.
     * Replaces {@code ingredient.toNetwork(buf)} (removed in 1.21.1).
     */
    public static void toNetwork(RegistryFriendlyByteBuf buf, Ingredient ingredient) {
        Ingredient.CONTENTS_STREAM_CODEC.encode(buf, ingredient);
    }

    /**
     * Serialize an Ingredient to JSON.
     * Replaces {@code ingredient.toJson()} (removed in 1.21.1).
     */
    public static JsonElement toJson(Ingredient ingredient) {
        return Ingredient.CODEC.encodeStart(JsonOps.INSTANCE, ingredient).getOrThrow();
    }

    /**
     * Create an Ingredient from a tag.
     * Replaces {@code Ingredient.fromValues(Stream.of(new Ingredient.TagValue(tag)))} (removed in 1.21.1).
     */
    public static Ingredient ofTag(TagKey<Item> tag) {
        return Ingredient.of(tag);
    }
}
