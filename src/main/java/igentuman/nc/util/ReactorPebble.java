package igentuman.nc.util;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public class ReactorPebble {

    public int ticks = 1;
    public double ticksProcessed = 0;
    public double criticality = 0;
    public double heat = 0;
    public double irradiation = 0;
    public double gamma = 2.0;
    public ItemStack outputStack = ItemStack.EMPTY;

    public static ReactorPebble make(int ticks, ItemStack outputStack, double criticality, double heat, double gamma, double irradiation) {
        ReactorPebble pebble = new ReactorPebble();
        pebble.ticks = Math.max(1, ticks);
        pebble.outputStack = outputStack;
        pebble.criticality = criticality;
        pebble.heat = heat;
        pebble.gamma = gamma;
        pebble.irradiation = irradiation;
        return pebble;
    }

    public double burnup() {
        return Math.min(1.0, ticksProcessed / Math.max(1.0, ticks));
    }

    public double effectiveIrradiation() {
        return irradiation * Math.pow(Math.max(0.0, 1.0 - burnup()), gamma);
    }

    public double getHeat() {
        return heat;
    }

    public void tick(double reactivity) {
        ticksProcessed += Math.max(0.0, reactivity);
    }

    public boolean isDepleted() {
        return ticksProcessed >= ticks;
    }

    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("ticks", ticks);
        tag.putDouble("ticksProcessed", ticksProcessed);
        tag.putDouble("criticality", criticality);
        tag.putDouble("heat", heat);
        tag.putDouble("irradiation", irradiation);
        tag.putDouble("gamma", gamma);
        tag.put("outputStack", outputStack.saveOptional(provider));
        return tag;
    }

    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        ticks = tag.getInt("ticks");
        ticksProcessed = tag.getDouble("ticksProcessed");
        criticality = tag.getDouble("criticality");
        heat = tag.getDouble("heat");
        irradiation = tag.getDouble("irradiation");
        gamma = tag.contains("gamma") ? tag.getDouble("gamma") : 2.0;
        outputStack = ItemStack.parseOptional(provider, tag.getCompound("outputStack"));
    }
}
