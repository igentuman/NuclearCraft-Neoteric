package igentuman.nc.api.multiblock.part;

import net.minecraft.resources.Identifier;

public record DetectorDef(
        Identifier id,
        double efficiency,
        long energyPerTick,
        int maximumDistance
) {
}
