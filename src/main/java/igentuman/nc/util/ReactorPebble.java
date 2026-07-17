package igentuman.nc.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.INBTSerializable;

/**
 * One TRISO pebble inside an MSR. Emitted irradiation (neutron flux) decays non-linearly with
 * burnup; the exponent (`gamma`) is set by enrichment class (HE drops fast, LE holds longer).
 * Criticality is a static ignition threshold. All pebbles in a reactor tick together each reactor tick.
 */
public class ReactorPebble implements INBTSerializable<Tag> {

    public int ticks = 1;             // ticks to full depletion
    public double ticksProcessed = 0; // accumulated burn (advances with reactivity)
    public double criticality = 0;    // ignition threshold (lower = easier; < SELF_PRIME_CRITICALITY self-primes)
    public double heat = 0;           // heat/tick at nominal reactivity
    public double irradiation = 0;    // neutron flux emitted at nominal reactivity (decays with burnup)
    public double gamma = 2.0;        // irradiation-decay exponent (HE high, LE low)
    public ItemStack outputStack = ItemStack.EMPTY; // depleted item produced on depletion

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

    @Override
    public Tag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("ticks", ticks);
        tag.putDouble("ticksProcessed", ticksProcessed);
        tag.putDouble("criticality", criticality);
        tag.putDouble("heat", heat);
        tag.putDouble("irradiation", irradiation);
        tag.putDouble("gamma", gamma);
        tag.put("outputStack", outputStack.serializeNBT());
        return tag;
    }

    @Override
    public void deserializeNBT(Tag nbt) {
        if (nbt instanceof CompoundTag tag) {
            ticks = tag.getInt("ticks");
            ticksProcessed = tag.getDouble("ticksProcessed");
            criticality = tag.getDouble("criticality");
            heat = tag.getDouble("heat");
            irradiation = tag.getDouble("irradiation");
            gamma = tag.contains("gamma") ? tag.getDouble("gamma") : 2.0;
            outputStack = ItemStack.of(tag.getCompound("outputStack"));
        }
    }
}
