package igentuman.nc.multiblock.fission;

import igentuman.nc.api.impl.MultiblockCacheImpl;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

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

    @Override
    public void saveNbt(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveNbt(tag, registries);
        tag.putInt("fuelCellCount", fuelCellCount);
        tag.putInt("width", width);
        tag.putInt("height", height);
        tag.putInt("depth", depth);
        long[] cells = new long[fuelCells.size()];
        int i = 0;
        for (long p : fuelCells) cells[i++] = p;
        tag.putLongArray("fuelCells", cells);
    }

    @Override
    public void loadNbt(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadNbt(tag, registries);
        fuelCellCount = tag.getInt("fuelCellCount");
        width = tag.getInt("width");
        height = tag.getInt("height");
        depth = tag.getInt("depth");
        fuelCells.clear();
        if (tag.contains("fuelCells")) {
            for (long p : tag.getLongArray("fuelCells")) fuelCells.add(p);
        }
    }
}
