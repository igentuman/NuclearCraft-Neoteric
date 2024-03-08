package igentuman.nc.client.particle;

import com.mojang.serialization.Codec;
import net.minecraft.core.particles.ParticleType;
import org.antlr.v4.runtime.misc.NotNull;;

public class FusionBeamParticleType extends ParticleType<FusionBeamParticleData> {

    public FusionBeamParticleType() {
        super(false, FusionBeamParticleData.DESERIALIZER);
    }

    @NotNull
    @Override
    public Codec<FusionBeamParticleData> codec() {
        return FusionBeamParticleData.CODEC;
    }
}