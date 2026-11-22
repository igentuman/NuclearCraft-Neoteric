package igentuman.nc.recipe.turbine;

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

/** JSON and network codecs for {@link TurbineRecipe}. */
public final class TurbineRecipeSerializer {

    private static final MapCodec<TurbineRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            RecipeIngredientCodecs.FLUID.fieldOf("input_fluid").forGetter(TurbineRecipe::input),
            FluidOutput.CODEC.fieldOf("output_fluid").forGetter(TurbineRecipe::output),
            Codec.DOUBLE.fieldOf("power_modifier").forGetter(TurbineRecipe::powerModifier)
    ).apply(inst, TurbineRecipe::new));

    private static final StreamCodec<RegistryFriendlyByteBuf, TurbineRecipe> STREAM_CODEC =
            StreamCodec.composite(
                    SizedFluidIngredient.STREAM_CODEC, TurbineRecipe::input,
                    FluidOutput.STREAM_CODEC, TurbineRecipe::output,
                    ByteBufCodecs.DOUBLE, TurbineRecipe::powerModifier,
                    TurbineRecipe::new);

    private TurbineRecipeSerializer() {}

    public static RecipeSerializer<TurbineRecipe> create() {
        return new RecipeSerializer<>(CODEC, STREAM_CODEC);
    }
}
