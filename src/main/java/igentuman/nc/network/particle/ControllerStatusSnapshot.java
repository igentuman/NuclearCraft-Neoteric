package igentuman.nc.network.particle;

import igentuman.nc.api.particle.ParticleStack;
import igentuman.nc.multiblock.StructureLifecycleState;
import net.minecraft.core.BlockPos;

public record ControllerStatusSnapshot(
        BlockPos controllerPos,
        long structureGeneration,
        StructureLifecycleState state,
        String statusKey,
        long energyStored,
        long energyCapacity,
        long storedHeat,
        long temperatureK,
        long maximumTemperatureK,
        ParticleStack beam
) {
}
