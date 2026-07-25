package igentuman.nc.multiblock.heat_exchanger;

import igentuman.nc.api.impl.MultiblockCacheImpl;

public class HeatExchangerCache extends MultiblockCacheImpl {

    public volatile int heatExchangers;
    public volatile int radiators;

    public void resetStats() {
        heatExchangers = 0;
        radiators = 0;
    }

    @Override
    public void clear() {
        super.clear();
        resetStats();
    }
}
