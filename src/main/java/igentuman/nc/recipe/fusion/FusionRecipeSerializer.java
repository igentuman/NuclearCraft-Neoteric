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

import java.util.List;

public class FusionRecipeSerializer implements RecipeSerializer<FusionRecipe> {

    private static final MapCodec<FusionRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            SizedFluidIngredient.FLAT_CODEC.fieldOf("input_a").forGetter(FusionRecipe::inputA),
            SizedFluidIngredient.FLAT_CODEC.fieldOf("input_b").forGetter(FusionRecipe::inputB),
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

    @Override
    public MapCodec<FusionRecipe> codec() {
        return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, FusionRecipe> streamCodec() {
        return STREAM_CODEC;
    }
}
