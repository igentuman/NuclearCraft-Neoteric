package igentuman.nc.multiblock.turbine;

import igentuman.nc.api.impl.MultiblockCacheImpl;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

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

    @Override
    public void saveNbt(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveNbt(tag, registries);
        tag.putInt("axis", axis == null ? -1 : axis.ordinal());
        tag.putLong("bearingPos1", bearingPos1);
        tag.putLong("bearingPos2", bearingPos2);
        tag.putDouble("flow", flow);
        tag.putInt("bladeCount", bladeCount);
        tag.putInt("activeCoils", activeCoils);
        tag.putDouble("coilsEfficiency", coilsEfficiency);
        tag.putInt("width", width);
        tag.putInt("height", height);
        tag.putInt("depth", depth);
    }

    @Override
    public void loadNbt(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadNbt(tag, registries);
        int a = tag.getInt("axis");
        axis = (a >= 0 && a < Direction.Axis.values().length) ? Direction.Axis.values()[a] : null;
        bearingPos1 = tag.getLong("bearingPos1");
        bearingPos2 = tag.getLong("bearingPos2");
        flow = tag.getDouble("flow");
        bladeCount = tag.getInt("bladeCount");
        activeCoils = tag.getInt("activeCoils");
        coilsEfficiency = tag.getDouble("coilsEfficiency");
        width = tag.getInt("width");
        height = tag.getInt("height");
        depth = tag.getInt("depth");
    }
}
