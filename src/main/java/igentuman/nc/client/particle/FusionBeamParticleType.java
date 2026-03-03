package igentuman.nc.client.particle;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

public class FusionBeamParticleType extends ParticleType<FusionBeamParticleData> {

    public FusionBeamParticleType() {
        super(false);
    }

    @NotNull
    @Override
    public MapCodec<FusionBeamParticleData> codec() {
        return FusionBeamParticleData.CODEC;
    }

    @NotNull
    @Override
    public StreamCodec<RegistryFriendlyByteBuf, FusionBeamParticleData> streamCodec() {
        return FusionBeamParticleData.STREAM_CODEC;
    }
}
