package igentuman.nc.util.sided.capability;

import igentuman.nc.util.sided.SidedContentHandler.RelativeDirection;
import igentuman.nc.util.sided.SidedContentHandler.SlotType;
import igentuman.nc.util.sided.SlotModePair;
import igentuman.nc.util.sided.SlotModePair.SlotMode;

import java.util.HashMap;

public abstract class AbscractCapabilityHandler {
    protected int inputSlots;
    protected int outputSlots;
    
    protected boolean sideMapUpdated = true;
    public HashMap<Integer, SlotModePair[]> sideMap;

    protected void initDefault() {
        sideMap = new HashMap<>();
        for (RelativeDirection side : RelativeDirection.values()) {
            SlotModePair[] defaultSide = new SlotModePair[inputSlots+outputSlots];
            for (int i = 0; i < inputSlots; i++) {
                defaultSide[i] = new SlotModePair(SlotMode.INPUT, i);
            }
            for (int i = inputSlots; i < inputSlots+outputSlots; i++) {
                defaultSide[i] = new SlotModePair(SlotMode.OUTPUT, i);
            }
            sideMap.put(side.ordinal(), defaultSide);
        }
    }

    public SlotType getType(int slot) {
        return slot < inputSlots ? SlotType.INPUT : SlotType.OUTPUT;
    }

    public SlotMode getMode(int slot, int side) {
        return sideMap.get(side)[slot].getMode();
    }

    public void toggleMode(int slot, int side) {
        SlotModePair[] sideSlots = sideMap.get(side);
        SlotModePair slotModePair = sideSlots[slot];
        SlotMode mode = slotModePair.getMode();
        sideMapUpdated = true;
        if(getType(slot) == SlotType.INPUT) {
            if (mode == SlotMode.DISABLED) {
                sideSlots[slot] = new SlotModePair(SlotMode.INPUT, slot);
            } else if (mode == SlotMode.INPUT) {
                sideSlots[slot] = new SlotModePair(SlotMode.PULL, slot);
            } else if (mode == SlotMode.PULL) {
                sideSlots[slot] = new SlotModePair(SlotMode.DISABLED, slot);
            }
        } else {
            if (mode == SlotMode.DISABLED) {
                sideSlots[slot] = new SlotModePair(SlotMode.OUTPUT, slot);
            } else if (mode == SlotMode.OUTPUT) {
                sideSlots[slot] = new SlotModePair(SlotMode.PUSH, slot);
            } else if (mode == SlotMode.PUSH) {
                sideSlots[slot] = new SlotModePair(SlotMode.PUSH_EXCESS, slot);
            } else if (mode == SlotMode.PUSH_EXCESS) {
                sideSlots[slot] = new SlotModePair(SlotMode.DISABLED, slot);
            }
        }
    }
}
