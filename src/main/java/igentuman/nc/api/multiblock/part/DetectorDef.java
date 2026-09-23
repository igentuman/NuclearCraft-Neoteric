package igentuman.nc.api.multiblock.part;

import net.minecraft.resources.ResourceLocation;

public record DetectorDef(
        ResourceLocation id,
        double efficiency,
        long energyPerTick,
        int maximumDistance
) {
}
