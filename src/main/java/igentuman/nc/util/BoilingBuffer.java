package igentuman.nc.util;

import net.minecraft.nbt.CompoundTag;

/** Tracks coolant boiling: capacity, current/max boiling rate, and coolant/hot-coolant amounts. */
public class BoilingBuffer implements NBTSerializable {

    public double capacity;
    public double boilingRate;
    public double maxBoilingRate;
    public double coolantAmount;
    public double hotCoolantAmount;

    public void setCapacity(double capacity) {
        this.capacity = capacity;
    }

    public void addHeat(double amount) {
        boilingRate = Math.max(0, boilingRate + amount);
    }

    public void cool() {
        boilingRate = Math.max(0, boilingRate - 10); // Default cooldown if not specified
    }

    public double netRate() {
        return boilingRate;
    }

    public double fraction() {
        return capacity <= 0 ? 0 : (coolantAmount + hotCoolantAmount) / capacity;
    }

    public boolean isOverMax() {
        return capacity > 0 && (coolantAmount + hotCoolantAmount) > capacity;
    }

    public void reset() {
        coolantAmount = 0;
        hotCoolantAmount = 0;
        boilingRate = 0;
        maxBoilingRate = 0;
    }

    public void save(CompoundTag tag) {
        tag.putDouble("capacity", capacity);
        tag.putDouble("boilingRate", boilingRate);
        tag.putDouble("maxBoilingRate", maxBoilingRate);
        tag.putDouble("coolantAmount", coolantAmount);
        tag.putDouble("hotCoolantAmount", hotCoolantAmount);
    }

    public void load(CompoundTag tag) {
        capacity = tag.getDouble("capacity");
        boilingRate = tag.getDouble("boilingRate");
        maxBoilingRate = tag.getDouble("maxBoilingRate");
        coolantAmount = tag.getDouble("coolantAmount");
        hotCoolantAmount = tag.getDouble("hotCoolantAmount");
    }
}
