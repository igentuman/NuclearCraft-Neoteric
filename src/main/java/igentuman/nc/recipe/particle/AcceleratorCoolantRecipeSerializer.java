package igentuman.nc.recipe.particle;

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

import java.util.Optional;

/** JSON and network codecs for {@link AcceleratorCoolantRecipe}. */
public final class AcceleratorCoolantRecipeSerializer {

    private static final MapCodec<AcceleratorCoolantRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            RecipeIngredientCodecs.FLUID.fieldOf("input_fluid").forGetter(AcceleratorCoolantRecipe::input),
            FluidOutput.CODEC.fieldOf("output_fluid").forGetter(AcceleratorCoolantRecipe::output),
            Codec.LONG.fieldOf("heat_per_mb").forGetter(AcceleratorCoolantRecipe::heatPerMb),
            Codec.LONG.optionalFieldOf("minimum_temperature_k")
                    .forGetter(r -> Optional.ofNullable(r.minimumTemperatureK()))
    ).apply(inst, (input, output, heatPerMb, minimumTemperatureK) ->
            new AcceleratorCoolantRecipe(input, output, heatPerMb, minimumTemperatureK.orElse(null))));

    private static final StreamCodec<RegistryFriendlyByteBuf, AcceleratorCoolantRecipe> STREAM_CODEC =
            StreamCodec.composite(
                    SizedFluidIngredient.STREAM_CODEC, AcceleratorCoolantRecipe::input,
                    FluidOutput.STREAM_CODEC, AcceleratorCoolantRecipe::output,
                    ByteBufCodecs.VAR_LONG, AcceleratorCoolantRecipe::heatPerMb,
                    ByteBufCodecs.optional(ByteBufCodecs.VAR_LONG),
                    r -> Optional.ofNullable(r.minimumTemperatureK()),
                    (input, output, heatPerMb, minimumTemperatureK) ->
                            new AcceleratorCoolantRecipe(input, output, heatPerMb, minimumTemperatureK.orElse(null)));

    private AcceleratorCoolantRecipeSerializer() {}

    public static RecipeSerializer<AcceleratorCoolantRecipe> create() {
        return new RecipeSerializer<>(CODEC, STREAM_CODEC);
    }
}
