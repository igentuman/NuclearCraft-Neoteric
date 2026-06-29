package igentuman.nc.multiblock.fission;

import igentuman.nc.api.impl.MultiblockCacheImpl;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Multiblock cache for the fission reactor. Extends the base cache (which owns the structure
 * position set) with derived structure stats produced by {@link FissionReactorValidator}:
 * classified components and the scalars consumed by the reactor runtime logic.
 *
 * <p>All stats are (re)computed during validation, which runs on the multiblock executor thread.
 * Scalars are persisted so a reloaded-but-formed reactor keeps its numbers until the next
 * validation pass; the component position sets are rebuilt lazily on the next structure change.
 */
public class FissionReactorCache extends MultiblockCacheImpl {

    public final Set<Long> fuelCells = new HashSet<>();
    public final Set<Long> allModerators = new HashSet<>();
    public final Set<Long> activeModerators = new HashSet<>();
    /** Heat-sink position -> sink name (all placed sinks, valid or not). */
    public final Map<Long, String> heatSinks = new HashMap<>();
    public final Set<Long> validHeatSinks = new HashSet<>();
    public final Set<Long> activeHeatSinks = new HashSet<>();
    public final Set<Long> irradiators = new HashSet<>();
    public final Set<Long> validIrradiators = new HashSet<>();

    // Scalars are written off-thread during validation and read on the main thread by the
    // reactor logic; volatile gives visibility. The logic reads only these scalars (never the
    // mutable position sets), so it cannot observe a half-cleared collection.
    public volatile int irradiationLines;
    public volatile int fuelCellCount;
    public volatile double cellsHeatMult;
    public volatile double cellsEnergyMult;
    public volatile double moderatorsHeatMult;
    public volatile double moderatorsEnergyMult;
    public volatile double totalCooling;
    public volatile int width;
    public volatile int height;
    public volatile int depth;

    public void resetStats() {
        fuelCells.clear();
        allModerators.clear();
        activeModerators.clear();
        heatSinks.clear();
        validHeatSinks.clear();
        activeHeatSinks.clear();
        irradiators.clear();
        validIrradiators.clear();
        irradiationLines = 0;
        fuelCellCount = 0;
        cellsHeatMult = 0;
        cellsEnergyMult = 0;
        moderatorsHeatMult = 0;
        moderatorsEnergyMult = 0;
        totalCooling = 0;
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
        tag.putInt("irradiationLines", irradiationLines);
        tag.putInt("fuelCellCount", fuelCellCount);
        tag.putDouble("cellsHeatMult", cellsHeatMult);
        tag.putDouble("cellsEnergyMult", cellsEnergyMult);
        tag.putDouble("moderatorsHeatMult", moderatorsHeatMult);
        tag.putDouble("moderatorsEnergyMult", moderatorsEnergyMult);
        tag.putDouble("totalCooling", totalCooling);
        tag.putInt("width", width);
        tag.putInt("height", height);
        tag.putInt("depth", depth);
    }

    @Override
    public void loadNbt(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadNbt(tag, registries);
        irradiationLines = tag.getInt("irradiationLines");
        fuelCellCount = tag.getInt("fuelCellCount");
        cellsHeatMult = tag.getDouble("cellsHeatMult");
        cellsEnergyMult = tag.getDouble("cellsEnergyMult");
        moderatorsHeatMult = tag.getDouble("moderatorsHeatMult");
        moderatorsEnergyMult = tag.getDouble("moderatorsEnergyMult");
        totalCooling = tag.getDouble("totalCooling");
        width = tag.getInt("width");
        height = tag.getInt("height");
        depth = tag.getInt("depth");
    }
}
