package igentuman.nc.multiblock.particle_chamber;

import net.minecraft.resources.ResourceLocation;

public record DetectorDef(
        ResourceLocation id,
        double efficiency,
        long energyPerTick,
        int maximumDistance
) {
}
