package igentuman.nc.multiblock.fission;

import igentuman.nc.api.multiblock.AbstractMultiblockCache;
import net.minecraft.core.BlockPos;

import java.util.HashSet;
import java.util.Set;

public class MsrCache extends AbstractMultiblockCache {

    private final Set<Long> workingFuelCells = new HashSet<>();

    public volatile Set<Long> fuelCells = Set.of();
    public volatile int fuelCellCount;
    public volatile int width;
    public volatile int height;
    public volatile int depth;

    void addFuelCell(BlockPos pos) {
        workingFuelCells.add(pos.asLong());
    }

    @Override
    protected void resetWorkingData() {
        workingFuelCells.clear();
    }

    @Override
    protected void publishData() {
        fuelCells = Set.copyOf(workingFuelCells);
        fuelCellCount = workingFuelCells.size();
        BlockPos min = workingMin();
        BlockPos max = workingMax();
        width = max.getX() - min.getX() + 1;
        height = max.getY() - min.getY() + 1;
        depth = max.getZ() - min.getZ() + 1;
    }
}
