package igentuman.nc.multiblock.fission;

import net.minecraft.nbt.CompoundTag;

/**
 * Unified heat state for the fission reactor: capacity (meltdown threshold), accumulated heat, and
 * this tick's gross generation / total cooling. Replaces the legacy mod's scattered heat fields.
 */
public class HeatBuffer {

    public double capacity;
    public double currentHeat;
    public double heatPerTick;
    public double cooldownPerTick;

    public void setCapacity(double capacity) {
        this.capacity = capacity;
    }

    public void addHeat(double amount) {
        currentHeat = Math.max(0, currentHeat + amount);
    }

    public void cool() {
        currentHeat = Math.max(0, currentHeat - cooldownPerTick);
    }

    public double netRate() {
        return heatPerTick - cooldownPerTick;
    }

    public double fraction() {
        return capacity <= 0 ? 0 : currentHeat / capacity;
    }

    public boolean isOverMax() {
        return capacity > 0 && currentHeat > capacity;
    }

    public void reset() {
        currentHeat = 0;
        heatPerTick = 0;
        cooldownPerTick = 0;
    }

    public void save(CompoundTag tag) {
        tag.putDouble("currentHeat", currentHeat);
    }

    public void load(CompoundTag tag) {
        currentHeat = tag.getDouble("currentHeat");
    }
}
