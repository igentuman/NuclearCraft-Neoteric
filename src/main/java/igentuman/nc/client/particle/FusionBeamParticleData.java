package igentuman.nc.client.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import igentuman.nc.setup.NcParticles;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

/** Particle options carrying beam direction, length and energy scale for the fusion reactor beam particle. */
public record FusionBeamParticleData(Direction direction, double distance, float energyScale) implements ParticleOptions {

    public static final MapCodec<FusionBeamParticleData> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            Direction.CODEC.fieldOf("direction").forGetter(FusionBeamParticleData::direction),
            Codec.DOUBLE.fieldOf("distance").forGetter(FusionBeamParticleData::distance),
            Codec.FLOAT.fieldOf("energyScale").forGetter(FusionBeamParticleData::energyScale)
    ).apply(inst, FusionBeamParticleData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, FusionBeamParticleData> STREAM_CODEC = StreamCodec.composite(
            Direction.STREAM_CODEC, FusionBeamParticleData::direction,
            ByteBufCodecs.DOUBLE, FusionBeamParticleData::distance,
            ByteBufCodecs.FLOAT, FusionBeamParticleData::energyScale,
            FusionBeamParticleData::new
    );

    @NotNull
    @Override
    public ParticleType<?> getType() {
        return NcParticles.FUSION_BEAM.get();
    }
}
