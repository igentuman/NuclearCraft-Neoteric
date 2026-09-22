package igentuman.nc.network.particle;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

public record CreativeParticleSourceSnapshot(
        BlockPos sourcePos,
        ResourceLocation particleId,
        double focus,
        long energyKeV,
        int energyScale,
        String errorKey
) {
}
