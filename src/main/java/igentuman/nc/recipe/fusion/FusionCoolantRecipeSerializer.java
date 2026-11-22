package igentuman.nc.recipe.fusion;

import igentuman.nc.recipe.RecipeIngredientCodecs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import igentuman.nc.api.recipe.FluidOutput;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

/** Codec and stream codec for reading and syncing {@link FusionCoolantRecipe} instances. */
public final class FusionCoolantRecipeSerializer {

    private static final MapCodec<FusionCoolantRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            RecipeIngredientCodecs.FLUID.fieldOf("input_fluid").forGetter(FusionCoolantRecipe::input),
            FluidOutput.CODEC.fieldOf("output_fluid").forGetter(FusionCoolantRecipe::output),
            Codec.INT.fieldOf("cooling_rate").forGetter(FusionCoolantRecipe::coolingRate)
    ).apply(inst, FusionCoolantRecipe::new));

    private static final StreamCodec<RegistryFriendlyByteBuf, FusionCoolantRecipe> STREAM_CODEC =
            StreamCodec.composite(
                    SizedFluidIngredient.STREAM_CODEC, FusionCoolantRecipe::input,
                    FluidOutput.STREAM_CODEC, FusionCoolantRecipe::output,
                    ByteBufCodecs.VAR_INT, FusionCoolantRecipe::coolingRate,
                    FusionCoolantRecipe::new);

    private FusionCoolantRecipeSerializer() {}

    public static RecipeSerializer<FusionCoolantRecipe> create() {
        return new RecipeSerializer<>(CODEC, STREAM_CODEC);
    }
}
