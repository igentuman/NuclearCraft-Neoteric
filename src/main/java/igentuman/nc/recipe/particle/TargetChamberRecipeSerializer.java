package igentuman.nc.recipe.particle;

import igentuman.nc.recipe.RecipeIngredientCodecs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import igentuman.nc.api.particle.ParticleIngredient;
import igentuman.nc.api.particle.ParticleStack;
import igentuman.nc.api.recipe.FluidOutput;
import igentuman.nc.api.recipe.ItemOutput;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import java.util.List;

/** JSON and network codecs for {@link TargetChamberRecipe}. */
public final class TargetChamberRecipeSerializer {

    private static final MapCodec<TargetChamberRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            RecipeIngredientCodecs.ITEM.listOf().optionalFieldOf("item_inputs", List.of())
                    .forGetter(TargetChamberRecipe::itemInputs),
            RecipeIngredientCodecs.FLUID.listOf().optionalFieldOf("fluid_inputs", List.of())
                    .forGetter(TargetChamberRecipe::fluidInputs),
            ParticleIngredient.CODEC.fieldOf("particle_input").forGetter(TargetChamberRecipe::particleInput),
            ItemOutput.CODEC.listOf().optionalFieldOf("item_outputs", List.of())
                    .forGetter(TargetChamberRecipe::itemOutputs),
            FluidOutput.CODEC.listOf().optionalFieldOf("fluid_outputs", List.of())
                    .forGetter(TargetChamberRecipe::fluidOutputs),
            ParticleStack.CODEC.listOf().optionalFieldOf("particle_outputs", List.of())
                    .forGetter(TargetChamberRecipe::particleOutputs),
            Codec.DOUBLE.fieldOf("cross_section").forGetter(TargetChamberRecipe::crossSection),
            Codec.LONG.optionalFieldOf("released_energy_kev", 0L).forGetter(TargetChamberRecipe::releasedEnergyKeV),
            Codec.INT.optionalFieldOf("priority", 0).forGetter(TargetChamberRecipe::priority)
    ).apply(inst, TargetChamberRecipe::new));

    private static final StreamCodec<RegistryFriendlyByteBuf, TargetChamberRecipe> STREAM_CODEC = new StreamCodec<>() {
        private final StreamCodec<RegistryFriendlyByteBuf, List<SizedIngredient>> ITEM_INPUTS =
                SizedIngredient.STREAM_CODEC.apply(ByteBufCodecs.list());
        private final StreamCodec<RegistryFriendlyByteBuf, List<SizedFluidIngredient>> FLUID_INPUTS =
                SizedFluidIngredient.STREAM_CODEC.apply(ByteBufCodecs.list());
        private final StreamCodec<RegistryFriendlyByteBuf, List<ItemOutput>> ITEM_OUTPUTS =
                ItemOutput.STREAM_CODEC.apply(ByteBufCodecs.list());
        private final StreamCodec<RegistryFriendlyByteBuf, List<FluidOutput>> FLUID_OUTPUTS =
                FluidOutput.STREAM_CODEC.apply(ByteBufCodecs.list());
        private final StreamCodec<RegistryFriendlyByteBuf, List<ParticleStack>> PARTICLE_OUTPUTS =
                ParticleStack.STREAM_CODEC.apply(ByteBufCodecs.list());

        @Override
        public TargetChamberRecipe decode(RegistryFriendlyByteBuf buf) {
            List<SizedIngredient> itemInputs = ITEM_INPUTS.decode(buf);
            List<SizedFluidIngredient> fluidInputs = FLUID_INPUTS.decode(buf);
            ParticleIngredient particleInput = ParticleIngredient.STREAM_CODEC.decode(buf);
            List<ItemOutput> itemOutputs = ITEM_OUTPUTS.decode(buf);
            List<FluidOutput> fluidOutputs = FLUID_OUTPUTS.decode(buf);
            List<ParticleStack> particleOutputs = PARTICLE_OUTPUTS.decode(buf);
            double crossSection = ByteBufCodecs.DOUBLE.decode(buf);
            long releasedEnergyKeV = ByteBufCodecs.VAR_LONG.decode(buf);
            int priority = ByteBufCodecs.VAR_INT.decode(buf);
            return new TargetChamberRecipe(itemInputs, fluidInputs, particleInput, itemOutputs, fluidOutputs,
                    particleOutputs, crossSection, releasedEnergyKeV, priority);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, TargetChamberRecipe recipe) {
            ITEM_INPUTS.encode(buf, recipe.itemInputs());
            FLUID_INPUTS.encode(buf, recipe.fluidInputs());
            ParticleIngredient.STREAM_CODEC.encode(buf, recipe.particleInput());
            ITEM_OUTPUTS.encode(buf, recipe.itemOutputs());
            FLUID_OUTPUTS.encode(buf, recipe.fluidOutputs());
            PARTICLE_OUTPUTS.encode(buf, recipe.particleOutputs());
            ByteBufCodecs.DOUBLE.encode(buf, recipe.crossSection());
            ByteBufCodecs.VAR_LONG.encode(buf, recipe.releasedEnergyKeV());
            ByteBufCodecs.VAR_INT.encode(buf, recipe.priority());
        }
    };

    private TargetChamberRecipeSerializer() {}

    public static RecipeSerializer<TargetChamberRecipe> create() {
        return new RecipeSerializer<>(CODEC, STREAM_CODEC);
    }
}
