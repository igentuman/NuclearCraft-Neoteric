package igentuman.nc.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

/** JSON and network codecs for weighted virtual ore vein pools. */
public final class OreVeinRecipeSerializer {
    private static final Codec<OreVeinRecipe.OreEntry> ORE_ENTRY_CODEC = RecordCodecBuilder.create(e -> e.group(
            RecipeIngredientCodecs.ITEM.fieldOf("item").forGetter(OreVeinRecipe.OreEntry::ingredient),
            Codec.INT.fieldOf("weight").forGetter(OreVeinRecipe.OreEntry::weight)
    ).apply(e, OreVeinRecipe.OreEntry::new));

    private static final MapCodec<OreVeinRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            Identifier.CODEC.fieldOf("id").forGetter(OreVeinRecipe::getId),
            ORE_ENTRY_CODEC.listOf().fieldOf("ores").forGetter(OreVeinRecipe::getOres),
            Codec.DOUBLE.optionalFieldOf("rarity", 1.0).forGetter(OreVeinRecipe::getRarityModifier)
    ).apply(inst, OreVeinRecipe::new));

    private static final StreamCodec<RegistryFriendlyByteBuf, OreVeinRecipe.OreEntry> ENTRY_STREAM =
            StreamCodec.composite(
                    SizedIngredient.STREAM_CODEC, OreVeinRecipe.OreEntry::ingredient,
                    ByteBufCodecs.INT, OreVeinRecipe.OreEntry::weight,
                    OreVeinRecipe.OreEntry::new);

    private static final StreamCodec<RegistryFriendlyByteBuf, OreVeinRecipe> STREAM_CODEC =
            StreamCodec.composite(
                    Identifier.STREAM_CODEC, OreVeinRecipe::getId,
                    ENTRY_STREAM.apply(ByteBufCodecs.list()), OreVeinRecipe::getOres,
                    ByteBufCodecs.DOUBLE, OreVeinRecipe::getRarityModifier,
                    OreVeinRecipe::new);

    private OreVeinRecipeSerializer() {}

    public static RecipeSerializer<OreVeinRecipe> create() {
        return new RecipeSerializer<>(CODEC, STREAM_CODEC);
    }
}
