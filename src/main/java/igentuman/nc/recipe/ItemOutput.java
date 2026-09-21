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

/** A processor item output: a concrete item, or a serialized tag resolved by the recipe serializer. */
public record ItemOutput(@Nullable Item item, @Nullable TagKey<Item> tag, int count) {

    public static ItemOutput of(ItemLike item, int count) {
        return new ItemOutput(item.asItem(), null, count);
    }

    public static ItemOutput of(TagKey<Item> tag, int count) {
        return new ItemOutput(null, tag, count);
    }

    private static ItemOutput decode(Optional<Item> item, Optional<TagKey<Item>> tag, int count) {
        if (tag.isPresent()) {
            ItemStack resolved = TagOutputResolver.resolveItem(tag.get(), count);
            return resolved.isEmpty() ? of(tag.get(), count) : of(resolved.getItem(), count);
        }
        return new ItemOutput(item.orElse(null), null, count);
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
        if (count <= 0) return false;
        if (tag != null) return !TagOutputResolver.membersItem(tag, count).isEmpty();
        return item != null && item != Items.AIR;
    }

    public static final Codec<ItemOutput> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            BuiltInRegistries.ITEM.byNameCodec().optionalFieldOf("item").forGetter(o -> Optional.ofNullable(o.item)),
            TagKey.codec(Registries.ITEM).optionalFieldOf("tag").forGetter(o -> Optional.ofNullable(o.tag)),
            Codec.INT.optionalFieldOf("count", 1).forGetter(ItemOutput::count)
    ).apply(inst, ItemOutput::decode));

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
                        ? decode(Optional.empty(), Optional.of(TagKey.create(Registries.ITEM, rl)), count)
                        : new ItemOutput(BuiltInRegistries.ITEM.get(rl), null, count);
            }
    );
}
