package igentuman.nc.util;

import net.minecraft.nbt.CompoundTag;

public class HeatBuffer implements NBTSerializable {

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
        tag.putDouble("capacity", capacity);
        tag.putDouble("currentHeat", currentHeat);
        tag.putDouble("heatPerTick", heatPerTick);
        tag.putDouble("cooldownPerTick", cooldownPerTick);
    }

    public void load(CompoundTag tag) {
        capacity = tag.getDouble("capacity");
        currentHeat = tag.getDouble("currentHeat");
        heatPerTick = tag.getDouble("heatPerTick");
        cooldownPerTick = tag.getDouble("cooldownPerTick");
    }
}
