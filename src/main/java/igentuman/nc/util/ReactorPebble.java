package igentuman.nc.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.INBTSerializable;

public class ReactorPebble  implements INBTSerializable<Tag> {

    public int ticks = 1;
    public double ticksProcessed = 0;
    public double temperature = 0;
    public double criticality = 0;
    public ItemStack outputStack = ItemStack.EMPTY;

    public static ReactorPebble make(int ticks, ItemStack outputStack, double heat, double criticality) {
        ReactorPebble pebble = new ReactorPebble();
        pebble.ticks = ticks;
        pebble.outputStack = outputStack;
        pebble.temperature = heat;
        pebble.criticality = criticality;
        return pebble;
    }

    public void tick(double efficiency) {
        ticksProcessed += efficiency;
    }

    public boolean isDepleted() {
        return ticksProcessed >= ticks;
    }

    @Override
    public Tag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("ticks", ticks);
        tag.putDouble("ticksProcessed", ticksProcessed);
        tag.putDouble("temperature", temperature);
        tag.putDouble("criticality", criticality);
        tag.put("outputStack", outputStack.serializeNBT());
        return tag;
    }

    @Override
    public void deserializeNBT(Tag nbt) {
        if (nbt instanceof CompoundTag tag) {
            ticks = tag.getInt("ticks");
            ticksProcessed = tag.getDouble("ticksProcessed");
            temperature = tag.getDouble("temperature");
            criticality = tag.getDouble("criticality");
            outputStack = ItemStack.of(tag.getCompound("outputStack"));
        }
    }
}
