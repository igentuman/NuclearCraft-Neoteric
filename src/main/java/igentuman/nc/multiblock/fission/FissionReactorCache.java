package igentuman.nc.multiblock.fission;

import igentuman.nc.api.impl.MultiblockCacheImpl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** Multiblock cache for the fission reactor; stores classified components and derived stats for the runtime reaction. */
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
    /** Passive (always-on) heat-sink cooling only; active sinks cool at runtime when fed coolant. */
    public volatile double totalCooling;
    /** Count of valid active heat sinks per {@link ActiveCoolant} (indexed by ordinal). Replaced
     *  atomically each validation pass; the reactor logic reads it on the main thread. */
    public volatile int[] activeCoolantCounts = new int[ActiveCoolant.COUNT];
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
        activeCoolantCounts = new int[ActiveCoolant.COUNT];
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
