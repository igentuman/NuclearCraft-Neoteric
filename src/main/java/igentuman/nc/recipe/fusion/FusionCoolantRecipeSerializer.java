package igentuman.nc.recipe.fusion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import igentuman.nc.recipe.FluidOutput;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

/** Codec and stream codec for reading and syncing {@link FusionCoolantRecipe} instances. */
public class FusionCoolantRecipeSerializer implements RecipeSerializer<FusionCoolantRecipe> {

    private static final MapCodec<FusionCoolantRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            SizedFluidIngredient.FLAT_CODEC.fieldOf("input_fluid").forGetter(FusionCoolantRecipe::input),
            FluidOutput.CODEC.fieldOf("output_fluid").forGetter(FusionCoolantRecipe::output),
            Codec.INT.fieldOf("cooling_rate").forGetter(FusionCoolantRecipe::coolingRate)
    ).apply(inst, FusionCoolantRecipe::new));

    private static final StreamCodec<RegistryFriendlyByteBuf, FusionCoolantRecipe> STREAM_CODEC =
            StreamCodec.composite(
                    SizedFluidIngredient.STREAM_CODEC, FusionCoolantRecipe::input,
                    FluidOutput.STREAM_CODEC, FusionCoolantRecipe::output,
                    ByteBufCodecs.VAR_INT, FusionCoolantRecipe::coolingRate,
                    FusionCoolantRecipe::new);

    @Override
    public MapCodec<FusionCoolantRecipe> codec() {
        return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, FusionCoolantRecipe> streamCodec() {
        return STREAM_CODEC;
    }
}
