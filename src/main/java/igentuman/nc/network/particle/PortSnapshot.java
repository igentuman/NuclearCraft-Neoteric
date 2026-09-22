package igentuman.nc.network.particle;

import igentuman.nc.api.particle.ParticleStack;
import igentuman.nc.block.accelerator.BeamPortMode;
import igentuman.nc.multiblock.geometry.StructureRole;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public record PortSnapshot(
        int stableId,
        BlockPos position,
        StructureRole role,
        BeamPortMode mode,
        Direction outwardFace,
        boolean connected,
        ParticleStack lastTransfer
) {
}
