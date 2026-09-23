package igentuman.nc.multiblock.fission;

import igentuman.nc.api.multiblock.AbstractMultiblockCache;
import net.minecraft.core.BlockPos;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class FissionReactorCache extends AbstractMultiblockCache {

    final Set<Long> workingFuelCells = new HashSet<>();
    final Set<Long> workingModerators = new HashSet<>();
    final Set<Long> workingActiveModerators = new HashSet<>();
    final Map<Long, String> workingHeatSinks = new HashMap<>();
    final Set<Long> workingValidHeatSinks = new HashSet<>();
    final Set<Long> workingIrradiators = new HashSet<>();
    int workingIrradiationLines;
    double workingCellsHeatMult;
    double workingCellsEnergyMult;
    double workingModeratorsHeatMult;
    double workingModeratorsEnergyMult;
    double workingTotalCooling;
    int[] workingActiveCoolantCounts = new int[ActiveCoolant.COUNT];

    public volatile Set<Long> fuelCells = Set.of();
    public volatile int fuelCellCount;
    public volatile int heatSinkCount;
    public volatile int moderatorCount;
    public volatile int irradiationLines;
    public volatile double cellsHeatMult;
    public volatile double cellsEnergyMult;
    public volatile double moderatorsHeatMult;
    public volatile double moderatorsEnergyMult;
    public volatile double totalCooling;
    public volatile int[] activeCoolantCounts = new int[ActiveCoolant.COUNT];
    public volatile int width;
    public volatile int height;
    public volatile int depth;

    @Override
    protected void resetWorkingData() {
        workingFuelCells.clear();
        workingModerators.clear();
        workingActiveModerators.clear();
        workingHeatSinks.clear();
        workingValidHeatSinks.clear();
        workingIrradiators.clear();
        workingIrradiationLines = 0;
        workingCellsHeatMult = 0;
        workingCellsEnergyMult = 0;
        workingModeratorsHeatMult = 0;
        workingModeratorsEnergyMult = 0;
        workingTotalCooling = 0;
        workingActiveCoolantCounts = new int[ActiveCoolant.COUNT];
    }

    @Override
    protected void publishData() {
        fuelCells = Set.copyOf(workingFuelCells);
        fuelCellCount = workingFuelCells.size();
        heatSinkCount = workingValidHeatSinks.size();
        moderatorCount = workingModerators.size();
        irradiationLines = workingIrradiationLines;
        cellsHeatMult = workingCellsHeatMult;
        cellsEnergyMult = workingCellsEnergyMult;
        moderatorsHeatMult = workingModeratorsHeatMult;
        moderatorsEnergyMult = workingModeratorsEnergyMult;
        totalCooling = workingTotalCooling;
        activeCoolantCounts = workingActiveCoolantCounts.clone();
        BlockPos min = workingMin();
        BlockPos max = workingMax();
        width = max.getX() - min.getX() + 1;
        height = max.getY() - min.getY() + 1;
        depth = max.getZ() - min.getZ() + 1;
    }
}
