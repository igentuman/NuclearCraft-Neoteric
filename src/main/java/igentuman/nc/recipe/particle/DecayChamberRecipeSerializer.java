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

/** JSON and network codecs for {@link DecayChamberRecipe}. */
public final class DecayChamberRecipeSerializer {

    private static final MapCodec<DecayChamberRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            ParticleIngredient.CODEC.fieldOf("particle_input").forGetter(DecayChamberRecipe::particleInput),
            ParticleStack.CODEC.listOf().fieldOf("particle_outputs").forGetter(DecayChamberRecipe::particleOutputs),
            Codec.DOUBLE.fieldOf("cross_section").forGetter(DecayChamberRecipe::crossSection),
            Codec.LONG.optionalFieldOf("released_energy_kev", 0L).forGetter(DecayChamberRecipe::releasedEnergyKeV),
            Codec.INT.optionalFieldOf("priority", 0).forGetter(DecayChamberRecipe::priority)
    ).apply(inst, DecayChamberRecipe::new));

    private static final StreamCodec<RegistryFriendlyByteBuf, DecayChamberRecipe> STREAM_CODEC = new StreamCodec<>() {
        private final StreamCodec<RegistryFriendlyByteBuf, List<ParticleStack>> PARTICLE_OUTPUTS =
                ParticleStack.STREAM_CODEC.apply(ByteBufCodecs.list());

        @Override
        public DecayChamberRecipe decode(RegistryFriendlyByteBuf buf) {
            ParticleIngredient particleInput = ParticleIngredient.STREAM_CODEC.decode(buf);
            List<ParticleStack> particleOutputs = PARTICLE_OUTPUTS.decode(buf);
            double crossSection = ByteBufCodecs.DOUBLE.decode(buf);
            long releasedEnergyKeV = ByteBufCodecs.VAR_LONG.decode(buf);
            int priority = ByteBufCodecs.VAR_INT.decode(buf);
            return new DecayChamberRecipe(particleInput, particleOutputs, crossSection, releasedEnergyKeV, priority);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, DecayChamberRecipe recipe) {
            ParticleIngredient.STREAM_CODEC.encode(buf, recipe.particleInput());
            PARTICLE_OUTPUTS.encode(buf, recipe.particleOutputs());
            ByteBufCodecs.DOUBLE.encode(buf, recipe.crossSection());
            ByteBufCodecs.VAR_LONG.encode(buf, recipe.releasedEnergyKeV());
            ByteBufCodecs.VAR_INT.encode(buf, recipe.priority());
        }
    };

    private DecayChamberRecipeSerializer() {}

    public static RecipeSerializer<DecayChamberRecipe> create() {
        return new RecipeSerializer<>(CODEC, STREAM_CODEC);
    }
}
