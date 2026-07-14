package igentuman.nc.setup;

import com.mojang.serialization.MapCodec;
import igentuman.nc.client.particle.FusionBeamParticleData;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.registries.DeferredHolder;

/** Registers the mod's particle types, such as the fusion beam particle. */
public final class NcParticles {

    private NcParticles() {
    }

    public static final DeferredHolder<ParticleType<?>, ParticleType<FusionBeamParticleData>> FUSION_BEAM =
            Registers.PARTICLE_TYPES.<ParticleType<FusionBeamParticleData>>register("fusion_beam",
                    () -> new ParticleType<FusionBeamParticleData>(false) {
                        @Override
                        public MapCodec<FusionBeamParticleData> codec() {
                            return FusionBeamParticleData.CODEC;
                        }

                        @Override
                        public StreamCodec<? super RegistryFriendlyByteBuf, FusionBeamParticleData> streamCodec() {
                            return FusionBeamParticleData.STREAM_CODEC;
                        }
                    });

    public static void init() {
    }
}
