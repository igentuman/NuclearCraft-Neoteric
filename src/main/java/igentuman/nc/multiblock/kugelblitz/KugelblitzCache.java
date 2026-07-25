package igentuman.nc.multiblock.kugelblitz;

import igentuman.nc.api.impl.MultiblockCacheImpl;
import net.minecraft.core.BlockPos;

public class KugelblitzCache extends MultiblockCacheImpl {

    public volatile int transformers;
    public volatile int fluxRegulators;
    public volatile int stabilizers;
    public volatile long center = Long.MIN_VALUE;

    public void resetStats() {
        transformers = 0;
        fluxRegulators = 0;
        stabilizers = 0;
        center = Long.MIN_VALUE;
    }

    public boolean hasCenter() {
        return center != Long.MIN_VALUE;
    }

    public BlockPos centerPos() {
        return BlockPos.of(center);
    }

    @Override
    public void clear() {
        super.clear();
        resetStats();
    }
}
