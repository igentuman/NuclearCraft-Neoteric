package igentuman.nc.client.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleType;
import net.minecraft.util.Direction;
import net.minecraft.network.PacketBuffer;
import org.antlr.v4.runtime.misc.NotNull;;

import java.util.Locale;

import static igentuman.nc.util.NcUtils.DIRECTION_CODEC;

public class FusionBeamParticleData implements IParticleData {

    public static final IDeserializer<FusionBeamParticleData> DESERIALIZER = new IDeserializer<FusionBeamParticleData>() {
        @NotNull
        @Override
        public FusionBeamParticleData fromCommand(@NotNull ParticleType<FusionBeamParticleData> type, @NotNull StringReader reader) throws CommandSyntaxException {
            reader.expect(' ');
            Direction direction = Direction.from3DDataValue(reader.readInt());
            reader.expect(' ');
            double distance = reader.readDouble();
            reader.expect(' ');
            float energyScale = reader.readFloat();
            return new FusionBeamParticleData(direction, distance, energyScale);
        }

        @NotNull
        @Override
        public FusionBeamParticleData fromNetwork(@NotNull ParticleType<FusionBeamParticleData> type, PacketBuffer buf) {
            return new FusionBeamParticleData(buf.readEnum(Direction.class), buf.readDouble(), buf.readFloat());
        }
    };
    public static final Codec<FusionBeamParticleData> CODEC = RecordCodecBuilder.create(val -> val.group(
            DIRECTION_CODEC.fieldOf("direction").forGetter(data -> data.direction),
            Codec.DOUBLE.fieldOf("distance").forGetter(data -> data.distance),
            Codec.FLOAT.fieldOf("energyScale").forGetter(data -> data.energyScale)
    ).apply(val, FusionBeamParticleData::new));

    public final Direction direction;
    public final double distance;
    public final float energyScale;

    public FusionBeamParticleData(Direction direction, double distance, float energyScale) {
        this.direction = direction;
        this.distance = distance;
        this.energyScale = energyScale;
    }

    @NotNull
    @Override
    public ParticleType<?> getType() {
        return null;
//return NcParticleTypes.FUSION_BEAM.get();
    }

    @Override
    public void writeToNetwork(@NotNull PacketBuffer buffer) {
        buffer.writeEnum(direction);
        buffer.writeDouble(distance);
        buffer.writeFloat(energyScale);
    }

    @NotNull
    @Override
    public String writeToString() {
        return String.format(Locale.ROOT, "%s %d %.2f %.2f", getType().toString(), direction.ordinal(), distance, energyScale);
    }
}