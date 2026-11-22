package igentuman.nc.recipe.fission;

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

/** Codec and stream codec for reading and syncing {@link BoilingRecipe} instances. */
public final class BoilingRecipeSerializer {

    private static final MapCodec<BoilingRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            RecipeIngredientCodecs.FLUID.fieldOf("input_fluid").forGetter(BoilingRecipe::input),
            FluidOutput.CODEC.fieldOf("output_fluid").forGetter(BoilingRecipe::output),
            Codec.INT.fieldOf("heat_required").forGetter(BoilingRecipe::heatRequired)
    ).apply(inst, BoilingRecipe::new));

    private static final StreamCodec<RegistryFriendlyByteBuf, BoilingRecipe> STREAM_CODEC =
            StreamCodec.composite(
                    SizedFluidIngredient.STREAM_CODEC, BoilingRecipe::input,
                    FluidOutput.STREAM_CODEC, BoilingRecipe::output,
                    ByteBufCodecs.VAR_INT, BoilingRecipe::heatRequired,
                    BoilingRecipe::new);

    private BoilingRecipeSerializer() {}

    public static RecipeSerializer<BoilingRecipe> create() {
        return new RecipeSerializer<>(CODEC, STREAM_CODEC);
    }
}
