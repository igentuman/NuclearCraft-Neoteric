package igentuman.nc.multiblock.heat_exchanger;

import igentuman.nc.api.multiblock.AbstractMultiblockCache;
import net.minecraft.nbt.CompoundTag;

public class HeatExchangerCache extends AbstractMultiblockCache {

    private int workingHeatExchangers;
    private int workingRadiators;

    public volatile int heatExchangers;
    public volatile int radiators;

    public void countHeatExchanger() {
        workingHeatExchangers++;
    }

    public void countRadiator() {
        workingRadiators++;
    }

    @Override
    protected void resetWorkingData() {
        workingHeatExchangers = 0;
        workingRadiators = 0;
    }

    @Override
    protected void publishData() {
        heatExchangers = workingHeatExchangers;
        radiators = workingRadiators;
    }

    @Override
    protected void saveData(CompoundTag tag) {
        tag.putInt("heatExchangers", heatExchangers);
        tag.putInt("radiators", radiators);
    }

    @Override
    protected void loadData(CompoundTag tag) {
        heatExchangers = tag.getInt("heatExchangers");
        radiators = tag.getInt("radiators");
    }
}
