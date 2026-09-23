package igentuman.nc.multiblock.turbine;

import igentuman.nc.api.multiblock.AbstractMultiblockCache;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TurbineCache extends AbstractMultiblockCache {

    final Set<Long> workingRotors = new HashSet<>();
    final List<BlockPos> workingBearings = new ArrayList<>();
    final Map<Long, String> workingCoils = new HashMap<>();
    final Set<Long> workingValidCoils = new HashSet<>();
    final Set<Long> workingBlades = new HashSet<>();
    Direction.Axis workingAxis;
    double workingFlow;
    int workingActiveCoils;
    double workingCoilsEfficiency;

    @Nullable
    public volatile Direction.Axis axis;
    @Nullable
    public volatile BlockPos bearingPos1;
    @Nullable
    public volatile BlockPos bearingPos2;
    public volatile double flow;
    public volatile int bladeCount;
    public volatile int activeCoils;
    public volatile double coilsEfficiency;
    public volatile int width;
    public volatile int height;
    public volatile int depth;

    @Override
    protected void resetWorkingData() {
        workingRotors.clear();
        workingBearings.clear();
        workingCoils.clear();
        workingValidCoils.clear();
        workingBlades.clear();
        workingAxis = null;
        workingFlow = 0;
        workingActiveCoils = 0;
        workingCoilsEfficiency = 0;
    }

    @Override
    protected void publishData() {
        axis = workingAxis;
        bearingPos1 = workingBearings.size() > 0 ? workingBearings.get(0) : null;
        bearingPos2 = workingBearings.size() > 1 ? workingBearings.get(1) : null;
        flow = workingFlow;
        bladeCount = workingBlades.size();
        activeCoils = workingActiveCoils;
        coilsEfficiency = workingCoilsEfficiency;
        BlockPos min = workingMin();
        BlockPos max = workingMax();
        if (min == null || max == null) {
            width = 0;
            height = 0;
            depth = 0;
            return;
        }
        width = max.getX() - min.getX() + 1;
        height = max.getY() - min.getY() + 1;
        depth = max.getZ() - min.getZ() + 1;
    }
}
