package igentuman.nc.multiblock.fusion;

import igentuman.nc.api.impl.MultiblockCacheImpl;

/** Multiblock cache for the fusion reactor; stores derived magnet/amplifier stats and structure size for the runtime. */
public class FusionReactorCache extends MultiblockCacheImpl {

    public volatile double magneticFieldStrength;
    public volatile int magnetsEfficiency;
    public volatile int magnetsPower;
    public volatile int maxMagnetsTemp;

    public volatile int rfAmplification;
    public volatile int rfAmplifiersPower;
    public volatile int rfEfficiency;
    public volatile int minRFAmplifiersTemp;

    public volatile int magnetCount;
    public volatile int amplifierCount;
    public volatile int connectorCount;
    public volatile int casingCount;
    public volatile int size;

    public void resetStats() {
        magneticFieldStrength = 0;
        magnetsEfficiency = 0;
        magnetsPower = 0;
        maxMagnetsTemp = 0;
        rfAmplification = 0;
        rfAmplifiersPower = 0;
        rfEfficiency = 0;
        minRFAmplifiersTemp = 0;
        magnetCount = 0;
        amplifierCount = 0;
        connectorCount = 0;
        casingCount = 0;
        size = 0;
    }

    @Override
    public void clear() {
        super.clear();
        resetStats();
    }
}
