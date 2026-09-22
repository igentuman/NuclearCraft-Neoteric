package igentuman.nc.recipe.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import igentuman.nc.api.particle.ParticleIngredient;
import igentuman.nc.api.particle.ParticleStack;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;

import java.util.List;

public final class CollisionChamberRecipeSerializer implements RecipeSerializer<CollisionChamberRecipe> {

    private static final MapCodec<CollisionChamberRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            ParticleIngredient.CODEC.listOf().fieldOf("particle_inputs").forGetter(CollisionChamberRecipe::particleInputs),
            ParticleStack.CODEC.listOf().fieldOf("particle_outputs").forGetter(CollisionChamberRecipe::particleOutputs),
            Codec.DOUBLE.fieldOf("cross_section").forGetter(CollisionChamberRecipe::crossSection),
            Codec.LONG.optionalFieldOf("released_energy_kev", 0L).forGetter(CollisionChamberRecipe::releasedEnergyKeV),
            Codec.BOOL.optionalFieldOf("ordered_inputs", false).forGetter(CollisionChamberRecipe::orderedInputs),
            Codec.INT.optionalFieldOf("priority", 0).forGetter(CollisionChamberRecipe::priority)
    ).apply(inst, CollisionChamberRecipe::new));

    private static final StreamCodec<RegistryFriendlyByteBuf, CollisionChamberRecipe> STREAM_CODEC = new StreamCodec<>() {
        private final StreamCodec<RegistryFriendlyByteBuf, List<ParticleIngredient>> PARTICLE_INPUTS =
                ParticleIngredient.STREAM_CODEC.apply(ByteBufCodecs.list());
        private final StreamCodec<RegistryFriendlyByteBuf, List<ParticleStack>> PARTICLE_OUTPUTS =
                ParticleStack.STREAM_CODEC.apply(ByteBufCodecs.list());

        @Override
        public CollisionChamberRecipe decode(RegistryFriendlyByteBuf buf) {
            List<ParticleIngredient> particleInputs = PARTICLE_INPUTS.decode(buf);
            List<ParticleStack> particleOutputs = PARTICLE_OUTPUTS.decode(buf);
            double crossSection = ByteBufCodecs.DOUBLE.decode(buf);
            long releasedEnergyKeV = ByteBufCodecs.VAR_LONG.decode(buf);
            boolean orderedInputs = ByteBufCodecs.BOOL.decode(buf);
            int priority = ByteBufCodecs.VAR_INT.decode(buf);
            return new CollisionChamberRecipe(particleInputs, particleOutputs, crossSection, releasedEnergyKeV,
                    orderedInputs, priority);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, CollisionChamberRecipe recipe) {
            PARTICLE_INPUTS.encode(buf, recipe.particleInputs());
            PARTICLE_OUTPUTS.encode(buf, recipe.particleOutputs());
            ByteBufCodecs.DOUBLE.encode(buf, recipe.crossSection());
            ByteBufCodecs.VAR_LONG.encode(buf, recipe.releasedEnergyKeV());
            ByteBufCodecs.BOOL.encode(buf, recipe.orderedInputs());
            ByteBufCodecs.VAR_INT.encode(buf, recipe.priority());
        }
    };

    @Override
    public MapCodec<CollisionChamberRecipe> codec() {
        return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, CollisionChamberRecipe> streamCodec() {
        return STREAM_CODEC;
    }
}
