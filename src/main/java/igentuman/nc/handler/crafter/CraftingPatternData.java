package igentuman.nc.handler.crafter;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public record CraftingPatternData(List<ItemStack> inputs, ItemStack output) {

    public static final Codec<CraftingPatternData> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            ItemStack.OPTIONAL_CODEC.listOf().fieldOf("inputs").forGetter(CraftingPatternData::inputs),
            ItemStack.CODEC.fieldOf("output").forGetter(CraftingPatternData::output)
    ).apply(inst, CraftingPatternData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, CraftingPatternData> STREAM_CODEC = StreamCodec.composite(
            ItemStack.OPTIONAL_STREAM_CODEC.apply(ByteBufCodecs.list()), CraftingPatternData::inputs,
            ItemStack.OPTIONAL_STREAM_CODEC, CraftingPatternData::output,
            CraftingPatternData::new);
}
