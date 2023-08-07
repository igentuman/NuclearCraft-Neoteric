package igentuman.nc.handler.sided.capability;

import igentuman.nc.handler.sided.SidedContentHandler;
import igentuman.nc.handler.sided.SlotModePair;

import java.util.HashMap;

public abstract class AbstractCapabilityHandler {
    public int inputSlots;
    public int outputSlots;
    
    protected boolean sideMapUpdated = true;
    public HashMap<Integer, SlotModePair[]> sideMap;

    protected void initDefault() {
        sideMap = new HashMap<>();
        for (SidedContentHandler.RelativeDirection side : SidedContentHandler.RelativeDirection.values()) {
            SlotModePair[] defaultSide = new SlotModePair[inputSlots+outputSlots];
            for (int i = 0; i < inputSlots; i++) {
                defaultSide[i] = new SlotModePair(SlotModePair.SlotMode.INPUT, i);
            }
            for (int i = inputSlots; i < inputSlots+outputSlots; i++) {
                defaultSide[i] = new SlotModePair(SlotModePair.SlotMode.OUTPUT, i);
            }
            sideMap.put(side.ordinal(), defaultSide);
        }
    }

    public SidedContentHandler.SlotType getType(int slot) {
        return slot < inputSlots ? SidedContentHandler.SlotType.INPUT : SidedContentHandler.SlotType.OUTPUT;
    }

    public SlotModePair.SlotMode getMode(int slot, int side) {
        return sideMap.get(side)[slot].getMode();
    }

    public void toggleMode(int slot, int side) {
        SlotModePair[] sideSlots = sideMap.get(side);
        SlotModePair slotModePair = sideSlots[slot];
        SlotModePair.SlotMode mode = slotModePair.getMode();
        sideMapUpdated = true;
        if(getType(slot) == SidedContentHandler.SlotType.INPUT) {
            if (mode == SlotModePair.SlotMode.DISABLED) {
                sideSlots[slot] = new SlotModePair(SlotModePair.SlotMode.INPUT, slot);
            } else if (mode == SlotModePair.SlotMode.INPUT) {
                sideSlots[slot] = new SlotModePair(SlotModePair.SlotMode.PULL, slot);
            } else if (mode == SlotModePair.SlotMode.PULL) {
                sideSlots[slot] = new SlotModePair(SlotModePair.SlotMode.DISABLED, slot);
            }
        } else {
            if (mode == SlotModePair.SlotMode.DISABLED) {
                sideSlots[slot] = new SlotModePair(SlotModePair.SlotMode.OUTPUT, slot);
            } else if (mode == SlotModePair.SlotMode.OUTPUT) {
                sideSlots[slot] = new SlotModePair(SlotModePair.SlotMode.PUSH, slot);
            } else if (mode == SlotModePair.SlotMode.PUSH) {
                sideSlots[slot] = new SlotModePair(SlotModePair.SlotMode.PUSH_EXCESS, slot);
            } else if (mode == SlotModePair.SlotMode.PUSH_EXCESS) {
                sideSlots[slot] = new SlotModePair(SlotModePair.SlotMode.DISABLED, slot);
            }
        }
    }
}
