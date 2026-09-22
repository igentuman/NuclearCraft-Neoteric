package igentuman.nc.network.particle;

import net.minecraft.core.BlockPos;

public record ControllerControlPayload(
        BlockPos controllerPos,
        long structureGeneration,
        boolean enabled,
        int controlSignal
) {
}
