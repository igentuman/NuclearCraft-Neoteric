package igentuman.nc.multiblock.heat_exchanger;

import igentuman.nc.api.impl.MultiblockCacheImpl;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

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

    @Override
    public void saveNbt(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveNbt(tag, registries);
        tag.putInt("heatExchangers", heatExchangers);
        tag.putInt("radiators", radiators);
    }

    @Override
    public void loadNbt(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadNbt(tag, registries);
        heatExchangers = tag.getInt("heatExchangers");
        radiators = tag.getInt("radiators");
    }
}
