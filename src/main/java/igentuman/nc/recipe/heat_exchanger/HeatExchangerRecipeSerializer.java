package igentuman.nc.recipe.heat_exchanger;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import igentuman.nc.recipe.FluidOutput;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

public class HeatExchangerRecipeSerializer implements RecipeSerializer<HeatExchangerRecipe> {

    private static final MapCodec<HeatExchangerRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            SizedFluidIngredient.FLAT_CODEC.fieldOf("input_fluid").forGetter(HeatExchangerRecipe::input),
            FluidOutput.CODEC.fieldOf("output_fluid").forGetter(HeatExchangerRecipe::output),
            Codec.INT.fieldOf("heat").forGetter(HeatExchangerRecipe::heat)
    ).apply(inst, HeatExchangerRecipe::new));

    private static final StreamCodec<RegistryFriendlyByteBuf, HeatExchangerRecipe> STREAM_CODEC =
            StreamCodec.composite(
                    SizedFluidIngredient.STREAM_CODEC, HeatExchangerRecipe::input,
                    FluidOutput.STREAM_CODEC, HeatExchangerRecipe::output,
                    ByteBufCodecs.INT, HeatExchangerRecipe::heat,
                    HeatExchangerRecipe::new);

    @Override
    public MapCodec<HeatExchangerRecipe> codec() {
        return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, HeatExchangerRecipe> streamCodec() {
        return STREAM_CODEC;
    }
}
