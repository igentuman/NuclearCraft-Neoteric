package igentuman.nc.recipe.turbine;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import igentuman.nc.recipe.FluidOutput;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

public class TurbineRecipeSerializer implements RecipeSerializer<TurbineRecipe> {

    private static final MapCodec<TurbineRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            SizedFluidIngredient.FLAT_CODEC.fieldOf("input_fluid").forGetter(TurbineRecipe::input),
            FluidOutput.CODEC.fieldOf("output_fluid").forGetter(TurbineRecipe::output),
            Codec.DOUBLE.fieldOf("power_modifier").forGetter(TurbineRecipe::powerModifier)
    ).apply(inst, TurbineRecipe::new));

    private static final StreamCodec<RegistryFriendlyByteBuf, TurbineRecipe> STREAM_CODEC =
            StreamCodec.composite(
                    SizedFluidIngredient.STREAM_CODEC, TurbineRecipe::input,
                    FluidOutput.STREAM_CODEC, TurbineRecipe::output,
                    ByteBufCodecs.DOUBLE, TurbineRecipe::powerModifier,
                    TurbineRecipe::new);

    @Override
    public MapCodec<TurbineRecipe> codec() {
        return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, TurbineRecipe> streamCodec() {
        return STREAM_CODEC;
    }
}
