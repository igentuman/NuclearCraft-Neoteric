package igentuman.nc.multiblock.fusion;

import igentuman.nc.api.impl.MultiblockCacheImpl;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

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

    @Override
    public void saveNbt(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveNbt(tag, registries);
        tag.putDouble("magneticFieldStrength", magneticFieldStrength);
        tag.putInt("magnetsEfficiency", magnetsEfficiency);
        tag.putInt("magnetsPower", magnetsPower);
        tag.putInt("maxMagnetsTemp", maxMagnetsTemp);
        tag.putInt("rfAmplification", rfAmplification);
        tag.putInt("rfAmplifiersPower", rfAmplifiersPower);
        tag.putInt("rfEfficiency", rfEfficiency);
        tag.putInt("minRFAmplifiersTemp", minRFAmplifiersTemp);
        tag.putInt("magnetCount", magnetCount);
        tag.putInt("amplifierCount", amplifierCount);
        tag.putInt("connectorCount", connectorCount);
        tag.putInt("casingCount", casingCount);
        tag.putInt("size", size);
    }

    @Override
    public void loadNbt(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadNbt(tag, registries);
        magneticFieldStrength = tag.getDouble("magneticFieldStrength");
        magnetsEfficiency = tag.getInt("magnetsEfficiency");
        magnetsPower = tag.getInt("magnetsPower");
        maxMagnetsTemp = tag.getInt("maxMagnetsTemp");
        rfAmplification = tag.getInt("rfAmplification");
        rfAmplifiersPower = tag.getInt("rfAmplifiersPower");
        rfEfficiency = tag.getInt("rfEfficiency");
        minRFAmplifiersTemp = tag.getInt("minRFAmplifiersTemp");
        magnetCount = tag.getInt("magnetCount");
        amplifierCount = tag.getInt("amplifierCount");
        connectorCount = tag.getInt("connectorCount");
        casingCount = tag.getInt("casingCount");
        size = tag.getInt("size");
    }
}
