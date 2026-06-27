package igentuman.nc.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * A processor item output: either a concrete item or an item tag. A tag is resolved to one
 * concrete stack at production time via {@link TagOutputResolver}. {@code count} applies to both.
 *
 * JSON: {@code {"item":"minecraft:iron_ingot","count":1}} or {@code {"tag":"c:ingots/copper","count":2}}.
 */
public record ItemOutput(@Nullable Item item, @Nullable TagKey<Item> tag, int count) {

    public static ItemOutput of(ItemLike item, int count) {
        return new ItemOutput(item.asItem(), null, count);
    }

    public static ItemOutput of(TagKey<Item> tag, int count) {
        return new ItemOutput(null, tag, count);
    }

    public boolean isTag() {
        return tag != null;
    }

    /** One concrete stack (tag → highest-priority member); EMPTY if unresolvable. */
    public ItemStack resolve() {
        if (tag != null) return TagOutputResolver.resolveItem(tag, count);
        if (item != null) return new ItemStack(item, count);
        return ItemStack.EMPTY;
    }

    /** All candidate stacks for display: concrete → singleton, tag → members in priority order. */
    public List<ItemStack> members() {
        if (tag != null) return TagOutputResolver.membersItem(tag, count);
        ItemStack stack = resolve();
        return stack.isEmpty() ? List.of() : List.of(stack);
    }

    /** A tag output is complete only if it resolves to at least one member. */
    public boolean isComplete() {
        if (tag != null) return !TagOutputResolver.membersItem(tag, count).isEmpty();
        return item != null && count > 0;
    }

    public static final Codec<ItemOutput> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            BuiltInRegistries.ITEM.byNameCodec().optionalFieldOf("item").forGetter(o -> Optional.ofNullable(o.item)),
            TagKey.codec(Registries.ITEM).optionalFieldOf("tag").forGetter(o -> Optional.ofNullable(o.tag)),
            Codec.INT.optionalFieldOf("count", 1).forGetter(ItemOutput::count)
    ).apply(inst, (item, tag, count) -> new ItemOutput(item.orElse(null), tag.orElse(null), count)));

    public static final StreamCodec<RegistryFriendlyByteBuf, ItemOutput> STREAM_CODEC = StreamCodec.of(
            (buf, o) -> {
                buf.writeBoolean(o.isTag());
                if (o.isTag()) {
                    buf.writeResourceLocation(o.tag.location());
                } else {
                    buf.writeResourceLocation(BuiltInRegistries.ITEM.getKey(o.item == null ? Items.AIR : o.item));
                }
                buf.writeVarInt(o.count);
            },
            buf -> {
                boolean isTag = buf.readBoolean();
                ResourceLocation rl = buf.readResourceLocation();
                int count = buf.readVarInt();
                return isTag
                        ? ItemOutput.of(TagKey.create(Registries.ITEM, rl), count)
                        : new ItemOutput(BuiltInRegistries.ITEM.get(rl), null, count);
            }
    );
}
