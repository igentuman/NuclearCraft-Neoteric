package igentuman.nc.api.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import igentuman.nc.setup.Registers;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public record ParticleStack(
        @Nullable ResourceLocation particleId,
        long amount,
        long meanEnergyKeV,
        double focus
) {

    public static final ParticleStack EMPTY = new ParticleStack(null, 0, 0, 0);

    public ParticleStack {
        if (amount < 0) {
            throw new IllegalArgumentException("Particle stack amount must not be negative: " + amount);
        }
        if (amount == 0) {
            particleId = null;
            meanEnergyKeV = 0;
            focus = 0;
        } else {
            if (particleId == null) {
                throw new IllegalArgumentException("Non-empty particle stack requires a species");
            }
            if (meanEnergyKeV < 0) {
                throw new IllegalArgumentException("Particle stack energy must not be negative: " + meanEnergyKeV);
            }
            if (!Double.isFinite(focus) || focus < 0) {
                throw new IllegalArgumentException("Particle stack focus must be finite and non-negative: " + focus);
            }
        }
    }

    public boolean isEmpty() {
        return particleId == null;
    }

    public static ParticleStack merge(ParticleStack a, ParticleStack b) {
        if (a.isEmpty()) return b;
        if (b.isEmpty()) return a;
        if (!a.particleId().equals(b.particleId()) || a.meanEnergyKeV() != b.meanEnergyKeV() || a.focus() != b.focus()) {
            throw new IllegalArgumentException("Particle stacks can only merge with exact species, energy and focus equality");
        }
        return new ParticleStack(a.particleId(), Math.addExact(a.amount(), b.amount()), a.meanEnergyKeV(), a.focus());
    }

    public static final Codec<ResourceLocation> PARTICLE_ID_CODEC = ResourceLocation.CODEC.comapFlatMap(
            id -> Registers.PARTICLE_DEFINITION_REGISTRY.containsKey(id)
                    ? DataResult.success(id)
                    : DataResult.error(() -> "Unknown particle species: " + id),
            id -> id
    );

    public static final Codec<ParticleStack> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            PARTICLE_ID_CODEC.optionalFieldOf("particle").forGetter(s -> Optional.ofNullable(s.particleId)),
            Codec.LONG.fieldOf("amount").forGetter(ParticleStack::amount),
            Codec.LONG.optionalFieldOf("energy", 0L).forGetter(ParticleStack::meanEnergyKeV),
            Codec.DOUBLE.optionalFieldOf("focus", 0.0).forGetter(ParticleStack::focus)
    ).apply(inst, (particleId, amount, energy, focus) -> new ParticleStack(particleId.orElse(null), amount, energy, focus)));

    public static final StreamCodec<RegistryFriendlyByteBuf, ParticleStack> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC), s -> Optional.ofNullable(s.particleId),
            ByteBufCodecs.VAR_LONG, ParticleStack::amount,
            ByteBufCodecs.VAR_LONG, ParticleStack::meanEnergyKeV,
            ByteBufCodecs.DOUBLE, ParticleStack::focus,
            (particleId, amount, energy, focus) -> new ParticleStack(particleId.orElse(null), amount, energy, focus)
    );
}
