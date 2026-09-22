package igentuman.nc.multiblock.discovery;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.UUID;

@FunctionalInterface
public interface GeometryDiscoveryJobFactory {

    GeometryDiscoveryJob create(UUID structureId, BlockPos controllerPos, Direction facing);
}
