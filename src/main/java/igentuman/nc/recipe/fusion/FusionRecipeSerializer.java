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

import java.util.List;

/** Codec and stream codec for reading and syncing {@link FusionRecipe} instances. */
public final class FusionRecipeSerializer {

    private static final MapCodec<FusionRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            RecipeIngredientCodecs.FLUID.fieldOf("input_a").forGetter(FusionRecipe::inputA),
            RecipeIngredientCodecs.FLUID.fieldOf("input_b").forGetter(FusionRecipe::inputB),
            FluidOutput.CODEC.listOf().fieldOf("outputs").forGetter(FusionRecipe::outputs),
            Codec.INT.fieldOf("energy").forGetter(FusionRecipe::energy),
            Codec.DOUBLE.fieldOf("optimal_temperature").forGetter(FusionRecipe::optimalTemperature),
            Codec.INT.fieldOf("process_time").forGetter(FusionRecipe::processTime)
    ).apply(inst, FusionRecipe::new));

    private static final StreamCodec<RegistryFriendlyByteBuf, FusionRecipe> STREAM_CODEC =
            StreamCodec.composite(
                    SizedFluidIngredient.STREAM_CODEC, FusionRecipe::inputA,
                    SizedFluidIngredient.STREAM_CODEC, FusionRecipe::inputB,
                    FluidOutput.STREAM_CODEC.apply(ByteBufCodecs.list()), FusionRecipe::outputs,
                    ByteBufCodecs.VAR_INT, FusionRecipe::energy,
                    ByteBufCodecs.DOUBLE, FusionRecipe::optimalTemperature,
                    ByteBufCodecs.VAR_INT, FusionRecipe::processTime,
                    FusionRecipe::new);

    private FusionRecipeSerializer() {}

    public static RecipeSerializer<FusionRecipe> create() {
        return new RecipeSerializer<>(CODEC, STREAM_CODEC);
    }
}
