package igentuman.nc.handler.sided.capability;

import igentuman.nc.handler.sided.SidedContentHandler;
import igentuman.nc.handler.sided.SlotModePair;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;

import java.util.HashMap;

public abstract class AbstractCapabilityHandler {
    public int inputSlots;
    public int outputSlots;
    public TileEntity tile;
    public boolean sideMapUpdated = true;
    public HashMap<Integer, SlotModePair[]> sideMap;
    public SidedContentHandler sidedContentHandler;

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

    public int toggleMode(int slot, int side) {
        SlotModePair[] sideSlots = sideMap.get(side);
        SlotModePair slotModePair = sideSlots[slot];
        SlotModePair.SlotMode mode = slotModePair.getMode();
        sideMapUpdated = true;
        if(getType(slot) == SidedContentHandler.SlotType.INPUT) {
            switch (mode) {
                case DISABLED:
                    sideSlots[slot] = new SlotModePair(SlotModePair.SlotMode.INPUT, slot);
                    break;
                case INPUT:
                    sideSlots[slot] = new SlotModePair(SlotModePair.SlotMode.PULL, slot);
                    break;
                case PULL:
                    sideSlots[slot] = new SlotModePair(SlotModePair.SlotMode.DISABLED, slot);
                    break;
            }
        } else {
            switch (mode) {
                case DISABLED:
                    sideSlots[slot] = new SlotModePair(SlotModePair.SlotMode.OUTPUT, slot);
                    break;
                case OUTPUT:
                    sideSlots[slot] = new SlotModePair(SlotModePair.SlotMode.PUSH, slot);
                    break;
                case PUSH:
                    sideSlots[slot] = new SlotModePair(SlotModePair.SlotMode.PUSH_EXCESS, slot);
                    break;
                case PUSH_EXCESS:
                    sideSlots[slot] = new SlotModePair(SlotModePair.SlotMode.DISABLED, slot);
                    break;
            }
        }
        return sideSlots[slot].getMode().ordinal();
    }

    protected Direction getFacing() {
        Direction facing = Direction.NORTH;
        if(tile == null) return facing;
        if(tile.getBlockState().hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            facing = tile.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        }
        return facing;
    }

    public boolean haveAccessFromSide(Direction side, int slot) {
        SidedContentHandler.RelativeDirection relativeDirection =
                SidedContentHandler.RelativeDirection.toRelative(side, getFacing());
        return sideMap.get(relativeDirection.ordinal())[slot].getMode() != SlotModePair.SlotMode.DISABLED;
    }

    public void setGlobalMode(int i, SlotModePair.SlotMode slotMode) {
        for(SidedContentHandler.RelativeDirection dir: SidedContentHandler.RelativeDirection.values()) {
            sideMap.get(dir.ordinal())[i].setMode(slotMode);
        }
    }
}
