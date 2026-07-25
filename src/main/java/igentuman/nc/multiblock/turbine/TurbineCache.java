package igentuman.nc.multiblock.turbine;

import igentuman.nc.api.impl.MultiblockCacheImpl;
import net.minecraft.core.Direction;

import java.util.HashSet;
import java.util.Set;

public class TurbineCache extends MultiblockCacheImpl {

    public final Set<Long> rotorPositions = new HashSet<>();
    public final Set<Long> coilPositions = new HashSet<>();
    public final Set<Long> validCoils = new HashSet<>();
    public final Set<Long> bladePositions = new HashSet<>();

    public volatile Direction.Axis axis;
    public volatile long bearingPos1 = Long.MIN_VALUE;
    public volatile long bearingPos2 = Long.MIN_VALUE;
    public volatile double flow;
    public volatile float rotationSpeed;
    public volatile int bladeCount;
    public volatile int activeCoils;
    public volatile double coilsEfficiency;
    public volatile int width;
    public volatile int height;
    public volatile int depth;

    public void resetStats() {
        rotorPositions.clear();
        coilPositions.clear();
        validCoils.clear();
        bladePositions.clear();
        axis = null;
        bearingPos1 = Long.MIN_VALUE;
        bearingPos2 = Long.MIN_VALUE;
        flow = 0;
        rotationSpeed = 0;
        bladeCount = 0;
        activeCoils = 0;
        coilsEfficiency = 0;
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
