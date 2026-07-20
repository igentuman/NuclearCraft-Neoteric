package igentuman.nc.setup;

import com.mojang.serialization.MapCodec;
import igentuman.nc.client.particle.FusionBeamParticleData;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.registries.DeferredHolder;

import static igentuman.nc.registration.ModEntryBuilder.addParticleType;

/** Registers the mod's particle types, such as the fusion beam particle and bomb FX particles. */
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
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> FIRE_VERTICAL1 = addParticleType("fire_vertical1", true);
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> SMOKE = addParticleType("smoke", true);
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> EXPLOSION = addParticleType("explosion", true);
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> EXPLOSION_SEED = addParticleType("explosion_seed", true);

    public static void init() {
    }
}
