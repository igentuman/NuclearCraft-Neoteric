package igentuman.nc.client.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import igentuman.nc.setup.registration.NcParticleTypes;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

public record FusionBeamParticleData(Direction direction, double distance, float energyScale) implements ParticleOptions {

    public static final MapCodec<FusionBeamParticleData> CODEC = RecordCodecBuilder.mapCodec(val -> val.group(
          Direction.CODEC.fieldOf("direction").forGetter(data -> data.direction()),
          Codec.DOUBLE.fieldOf("distance").forGetter(data -> data.distance()),
          Codec.FLOAT.fieldOf("energyScale").forGetter(data -> data.energyScale())
    ).apply(val, FusionBeamParticleData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, FusionBeamParticleData> STREAM_CODEC =
        StreamCodec.of(
            (buf, data) -> { buf.writeEnum(data.direction()); buf.writeDouble(data.distance()); buf.writeFloat(data.energyScale()); },
            buf -> new FusionBeamParticleData(buf.readEnum(Direction.class), buf.readDouble(), buf.readFloat())
        );

    @NotNull
    @Override
    public ParticleType<?> getType() {
        return NcParticleTypes.FUSION_BEAM.get();
    }
}
