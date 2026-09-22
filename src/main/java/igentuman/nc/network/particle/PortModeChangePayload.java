package igentuman.nc.network.particle;

import igentuman.nc.block.accelerator.BeamPortMode;
import net.minecraft.core.BlockPos;

public record PortModeChangePayload(
        BlockPos controllerPos,
        long structureGeneration,
        int portId,
        BeamPortMode mode
) {
}
