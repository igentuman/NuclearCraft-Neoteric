package igentuman.nc.handler.sided;

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
        return slotMode.ordinal()*100+slot;
    }

    public static SlotModePair unpack(int packedValue) {
        int slotMode = packedValue/100;
        int slot = packedValue%100;
        return new SlotModePair(slotMode, slot);
    }

    public enum SlotMode {
        DEFAULT(0x80808080),
        INPUT(0x80007BFF),
        PULL(0x802A28C9),
        OUTPUT(0x80FF7B2C),
        PUSH(0x80C9BF38),
        PUSH_EXCESS(0x80C9221C),
        DISABLED(0x80000000),
        UNKNOWN(0x80FF0000);

        private int color;

        SlotMode(int color) {
            this.color = color;
        }

        public int getColor() {
            return color;
        }
    }
}
