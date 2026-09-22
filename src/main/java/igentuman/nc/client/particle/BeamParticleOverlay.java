package igentuman.nc.client.particle;

import igentuman.nc.api.particle.ParticleStack;
import net.minecraft.core.Direction;

public record BeamParticleOverlay(
        ParticleStack sample,
        Direction direction,
        long transferGameTime,
        boolean blocked
) {
}
