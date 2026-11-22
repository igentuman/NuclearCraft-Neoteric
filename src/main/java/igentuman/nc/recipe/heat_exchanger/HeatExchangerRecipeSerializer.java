package igentuman.nc.recipe.heat_exchanger;

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

/** JSON and network codecs for {@link HeatExchangerRecipe}. */
public final class HeatExchangerRecipeSerializer {

    private static final MapCodec<HeatExchangerRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            RecipeIngredientCodecs.FLUID.fieldOf("input_fluid").forGetter(HeatExchangerRecipe::input),
            FluidOutput.CODEC.fieldOf("output_fluid").forGetter(HeatExchangerRecipe::output),
            Codec.INT.fieldOf("heat").forGetter(HeatExchangerRecipe::heat)
    ).apply(inst, HeatExchangerRecipe::new));

    private static final StreamCodec<RegistryFriendlyByteBuf, HeatExchangerRecipe> STREAM_CODEC =
            StreamCodec.composite(
                    SizedFluidIngredient.STREAM_CODEC, HeatExchangerRecipe::input,
                    FluidOutput.STREAM_CODEC, HeatExchangerRecipe::output,
                    ByteBufCodecs.INT, HeatExchangerRecipe::heat,
                    HeatExchangerRecipe::new);

    private HeatExchangerRecipeSerializer() {}

    public static RecipeSerializer<HeatExchangerRecipe> create() {
        return new RecipeSerializer<>(CODEC, STREAM_CODEC);
    }
}
