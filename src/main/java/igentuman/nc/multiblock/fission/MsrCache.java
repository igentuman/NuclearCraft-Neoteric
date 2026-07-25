package igentuman.nc.multiblock.fission;

import igentuman.nc.api.impl.MultiblockCacheImpl;

import java.util.HashSet;
import java.util.Set;

public class MsrCache extends MultiblockCacheImpl {

    public final Set<Long> fuelCells = new HashSet<>();

    public volatile int fuelCellCount;
    public volatile int width;
    public volatile int height;
    public volatile int depth;

    public void resetStats() {
        fuelCells.clear();
        fuelCellCount = 0;
        width = 0;
        height = 0;
        depth = 0;
    }

    @Override
    public void clear() {
        super.clear();
        resetStats();
    }
}
