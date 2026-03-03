package igentuman.nc.util;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import igentuman.api.platform.NCSerialization;
import net.neoforged.neoforge.common.util.INBTSerializable;

public class ReactorPebble  implements INBTSerializable<Tag> {

    public int ticks = 1;
    public double ticksProcessed = 0;
    public double temperature = 0;
    public double criticality = 0;
    public double power = 0;
    public double heat = 0;
    public ItemStack outputStack = ItemStack.EMPTY;

    public static ReactorPebble make(int ticks, ItemStack outputStack, double heat, double criticality, double power, double heatGen) {
        ReactorPebble pebble = new ReactorPebble();
        pebble.ticks = ticks;
        pebble.outputStack = outputStack;
        pebble.temperature = heat;
        pebble.criticality = criticality;
        pebble.power = power;
        pebble.heat = heatGen;
        return pebble;
    }

    public double getPower() {
        return power;
    }

    public double getHeat() {
        return heat;
    }

    public void tick(double efficiency) {
        ticksProcessed += efficiency;
    }

    public boolean isDepleted() {
        return ticksProcessed >= ticks;
    }

    @Override
    public Tag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("ticks", ticks);
        tag.putDouble("ticksProcessed", ticksProcessed);
        tag.putDouble("temperature", temperature);
        tag.putDouble("criticality", criticality);
        tag.putDouble("power", power);
        tag.putDouble("heat", heat);
        tag.put("outputStack", NCSerialization.saveItemStack(outputStack, provider));
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, Tag nbt) {
        if (nbt instanceof CompoundTag tag) {
            ticks = tag.getInt("ticks");
            ticksProcessed = tag.getDouble("ticksProcessed");
            temperature = tag.getDouble("temperature");
            criticality = tag.getDouble("criticality");
            power = tag.getDouble("power");
            heat = tag.getDouble("heat");
            outputStack = NCSerialization.loadItemStack(provider, tag.getCompound("outputStack"));
        }
    }
}
