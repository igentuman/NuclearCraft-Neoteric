package igentuman.nc.network.particle;

import igentuman.nc.api.particle.ParticleStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public record BeamVisualPayload(
        BlockPos portPos,
        Direction direction,
        ParticleStack particle,
        long gameTime
) {
}
