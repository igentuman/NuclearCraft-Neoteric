package igentuman.nc.util.sided;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;


public class SlotModePair {
    private final SlotMode slotMode;
    private final int slot;

    public SlotModePair(SlotMode slotMode, int slot) {
        this.slotMode = slotMode;
        this.slot = slot;
    }

    public SlotModePair(int slotMode, int slot) {
        this.slotMode = SlotMode.values()[slotMode];
        this.slot = slot;
    }

    public static Tag serializeArray(SlotModePair[] slotModePairs) {
        CompoundTag tag = new CompoundTag();
        int[] vals = new int[slotModePairs.length];
        for (int i = 0; i < slotModePairs.length; i++) {
            vals[i] = slotModePairs[i].pack();
        }
        tag.putIntArray("pairs", vals);
        return tag;
    }

    public static SlotModePair[] deserializeArray(CompoundTag compound) {
        int[] vals = compound.getIntArray("pairs");
        SlotModePair[] slotModePairs = new SlotModePair[vals.length];
        for (int i = 0; i < vals.length; i++) {
            slotModePairs[i] = unpack(vals[i]);
        }
        return slotModePairs;
    }

    public SlotMode getMode() {
        return slotMode;
    }

    public int getSlot() {
        return slot;
    }

    // pack slotMode and slot into one int
    public int pack() {
        return Integer.parseInt(String.format("%d%d",slotMode.ordinal(), slot));
    }

    public static SlotModePair unpack(int packedValue) {
        String val = String.valueOf(packedValue);//first digit is always slot mode, the rest is slot
        return new SlotModePair(Integer.parseInt(val.substring(0, 1)), Integer.parseInt(val.substring(1, val.length())));
    }

    public enum SlotMode {
        DEFAULT("default", 0x000000),
        INPUT("input", 0x0000FF),
        OUTPUT("output", 0xFF0000),
        DISABLED("disabled", 0x000000);

        private String name;
        private int color;

        SlotMode(String name, int color) {
            this.name = name;
            this.color = color;
        }
    }
}
