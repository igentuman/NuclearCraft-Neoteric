package igentuman.nc.recipe.fission;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import igentuman.nc.recipe.ItemOutput;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

/** Codec and stream codec for reading and syncing {@link FissionFuelRecipe} instances. */
public class FissionFuelRecipeSerializer implements RecipeSerializer<FissionFuelRecipe> {

    private static final MapCodec<FissionFuelRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            Ingredient.CODEC.fieldOf("input").forGetter(FissionFuelRecipe::input),
            ItemOutput.CODEC.fieldOf("output").forGetter(FissionFuelRecipe::output),
            Codec.INT.fieldOf("process_time").forGetter(FissionFuelRecipe::processTime),
            Codec.INT.fieldOf("power").forGetter(FissionFuelRecipe::power),
            Codec.INT.fieldOf("heat").forGetter(FissionFuelRecipe::heat)
    ).apply(inst, FissionFuelRecipe::new));

    private static final StreamCodec<RegistryFriendlyByteBuf, FissionFuelRecipe> STREAM_CODEC =
            StreamCodec.composite(
                    Ingredient.CONTENTS_STREAM_CODEC, FissionFuelRecipe::input,
                    ItemOutput.STREAM_CODEC, FissionFuelRecipe::output,
                    ByteBufCodecs.VAR_INT, FissionFuelRecipe::processTime,
                    ByteBufCodecs.VAR_INT, FissionFuelRecipe::power,
                    ByteBufCodecs.VAR_INT, FissionFuelRecipe::heat,
                    FissionFuelRecipe::new);

    @Override
    public MapCodec<FissionFuelRecipe> codec() {
        return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, FissionFuelRecipe> streamCodec() {
        return STREAM_CODEC;
    }
}
