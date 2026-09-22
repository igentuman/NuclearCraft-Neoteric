package igentuman.nc.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import igentuman.nc.api.particle.ParticleStack;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

public record ParticleSourceData(
        ResourceLocation particleId,
        long remainingAmount,
        long capacity,
        long initialEnergyKeV,
        double focus
) {

    public ParticleSourceData {
        Objects.requireNonNull(particleId, "particleId");
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity must be positive: " + capacity);
        }
        if (remainingAmount < 0 || remainingAmount > capacity) {
            throw new IllegalArgumentException("remainingAmount must be within [0, " + capacity + "]: " + remainingAmount);
        }
        if (initialEnergyKeV < 0) {
            throw new IllegalArgumentException("initialEnergyKeV must not be negative: " + initialEnergyKeV);
        }
        if (!Double.isFinite(focus) || focus < 0) {
            throw new IllegalArgumentException("focus must be finite and non-negative: " + focus);
        }
    }

    public static ParticleSourceData fullCharge(ResourceLocation particleId, long capacity, long initialEnergyKeV, double focus) {
        return new ParticleSourceData(particleId, capacity, capacity, initialEnergyKeV, focus);
    }

    public boolean isDepleted() {
        return remainingAmount <= 0;
    }

    public record EmitResult(ParticleStack stack, ParticleSourceData remaining) {
    }

    public EmitResult emit(long batchSize) {
        if (batchSize <= 0) {
            throw new IllegalArgumentException("batchSize must be positive: " + batchSize);
        }
        long toEmit = Math.min(batchSize, remainingAmount);
        if (toEmit <= 0) {
            return new EmitResult(ParticleStack.EMPTY, this);
        }
        ParticleStack stack = new ParticleStack(particleId, toEmit, initialEnergyKeV, focus);
        ParticleSourceData remaining = new ParticleSourceData(particleId, remainingAmount - toEmit, capacity, initialEnergyKeV, focus);
        return new EmitResult(stack, remaining);
    }

    public static final Codec<ParticleSourceData> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            ParticleStack.PARTICLE_ID_CODEC.fieldOf("particle").forGetter(ParticleSourceData::particleId),
            Codec.LONG.fieldOf("remaining").forGetter(ParticleSourceData::remainingAmount),
            Codec.LONG.fieldOf("capacity").forGetter(ParticleSourceData::capacity),
            Codec.LONG.optionalFieldOf("initial_energy", 0L).forGetter(ParticleSourceData::initialEnergyKeV),
            Codec.DOUBLE.optionalFieldOf("focus", 0.0).forGetter(ParticleSourceData::focus)
    ).apply(inst, ParticleSourceData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ParticleSourceData> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, ParticleSourceData::particleId,
            ByteBufCodecs.VAR_LONG, ParticleSourceData::remainingAmount,
            ByteBufCodecs.VAR_LONG, ParticleSourceData::capacity,
            ByteBufCodecs.VAR_LONG, ParticleSourceData::initialEnergyKeV,
            ByteBufCodecs.DOUBLE, ParticleSourceData::focus,
            ParticleSourceData::new
    );
}
