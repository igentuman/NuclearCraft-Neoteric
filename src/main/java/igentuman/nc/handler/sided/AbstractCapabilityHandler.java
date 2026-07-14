package igentuman.nc.handler.sided;

import igentuman.nc.handler.SlotModePair;
import igentuman.nc.handler.SidedContentHandler.RelativeDirection;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.HashMap;

/** Base for sided capability handlers: tracks per-face slot modes, toggling, and access resolution. */
public abstract class AbstractCapabilityHandler {
    public final int inputSlots;
    public final int outputSlots;
    public final int extraSlots;
    protected BlockEntity tile;
    protected boolean sideMapUpdated = false;
    protected final HashMap<Integer, SlotModePair[]> sideMap = new HashMap<>();

    public AbstractCapabilityHandler(int inputSlots, int outputSlots, int extraSlots) {
        this.inputSlots = inputSlots;
        this.outputSlots = outputSlots;
        this.extraSlots = extraSlots;
        initDefault();
    }

    public void setBlockEntity(BlockEntity tile) {
        this.tile = tile;
    }

    public void initDefault() {
        int totalSlots = inputSlots + outputSlots + extraSlots;
        for (int side = 0; side < 6; side++) {
            SlotModePair[] pairs = new SlotModePair[totalSlots];
            for (int slot = 0; slot < totalSlots; slot++) {
                SlotModePair.SlotMode mode;
                if (slot < inputSlots) {
                    mode = SlotModePair.SlotMode.INPUT;
                } else if (slot < inputSlots + outputSlots) {
                    mode = SlotModePair.SlotMode.OUTPUT;
                } else {
                    mode = SlotModePair.SlotMode.DEFAULT;
                }
                pairs[slot] = new SlotModePair(mode, slot);
            }
            sideMap.put(side, pairs);
        }
    }

    public SlotModePair.SlotMode getType(int slot) {
        if (slot < inputSlots) return SlotModePair.SlotMode.INPUT;
        if (slot < inputSlots + outputSlots) return SlotModePair.SlotMode.OUTPUT;
        return SlotModePair.SlotMode.DEFAULT;
    }

    public SlotModePair.SlotMode getMode(int slot, int side) {
        SlotModePair[] pairs = sideMap.get(side);
        if (pairs == null || slot < 0 || slot >= pairs.length) return SlotModePair.SlotMode.DISABLED;
        return pairs[slot].getMode();
    }

    public void toggleMode(int slot, int side) {
        SlotModePair[] pairs = sideMap.get(side);
        if (pairs == null || slot < 0 || slot >= pairs.length) return;
        SlotModePair pair = pairs[slot];
        SlotModePair.SlotMode current = pair.getMode();
        SlotModePair.SlotMode next;
        SlotModePair.SlotMode type = getType(slot);
        if (type == SlotModePair.SlotMode.INPUT) {
            next = switch (current) {
                case INPUT -> SlotModePair.SlotMode.PULL;
                case PULL -> SlotModePair.SlotMode.DISABLED;
                case DISABLED -> SlotModePair.SlotMode.INPUT;
                default -> SlotModePair.SlotMode.INPUT;
            };
        } else if (type == SlotModePair.SlotMode.OUTPUT) {
            next = switch (current) {
                case OUTPUT -> SlotModePair.SlotMode.PUSH;
                case PUSH -> SlotModePair.SlotMode.DISABLED;
                case DISABLED -> SlotModePair.SlotMode.OUTPUT;
                default -> SlotModePair.SlotMode.OUTPUT;
            };
        } else {
            next = switch (current) {
                case DEFAULT -> SlotModePair.SlotMode.DISABLED;
                case DISABLED -> SlotModePair.SlotMode.DEFAULT;
                default -> SlotModePair.SlotMode.DEFAULT;
            };
        }
        pair.setMode(next);
        sideMapUpdated = true;
    }

    public boolean hasPull() {
        for (SlotModePair[] pairs : sideMap.values()) {
            for (SlotModePair pair : pairs) {
                if (pair.getMode() == SlotModePair.SlotMode.PULL) return true;
            }
        }
        return false;
    }

    public boolean hasPush() {
        for (SlotModePair[] pairs : sideMap.values()) {
            for (SlotModePair pair : pairs) {
                if (pair.getMode() == SlotModePair.SlotMode.PUSH) return true;
            }
        }
        return false;
    }

    public Direction getFacing() {
        if (tile == null) return Direction.NORTH;
        try {
            return tile.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        } catch (Exception e) {
            return Direction.NORTH;
        }
    }

    public boolean haveAccessFromSide(Direction side, int slot) {
        return getModeForAbsoluteSide(side, slot) != SlotModePair.SlotMode.DISABLED;
    }

    public SlotModePair.SlotMode getModeForAbsoluteSide(Direction side, int slot) {
        Direction facing = getFacing();
        RelativeDirection relDir = RelativeDirection.toRelative(side, facing);
        if (relDir == null) return SlotModePair.SlotMode.DISABLED;
        SlotModePair[] pairs = sideMap.get(relDir.ordinal());
        if (pairs == null || slot < 0 || slot >= pairs.length) return SlotModePair.SlotMode.DISABLED;
        return pairs[slot].getMode();
    }

    public void setGlobalMode(int slot, SlotModePair.SlotMode mode) {
        for (int side = 0; side < 6; side++) {
            SlotModePair[] pairs = sideMap.get(side);
            if (pairs != null && slot >= 0 && slot < pairs.length) {
                pairs[slot].setMode(mode);
            }
        }
        sideMapUpdated = true;
    }

    public CompoundTag serializeSideMap() {
        CompoundTag tag = new CompoundTag();
        for (int side = 0; side < 6; side++) {
            SlotModePair[] pairs = sideMap.get(side);
            if (pairs != null) {
                tag.put("Side" + side, SlotModePair.serializeArray(pairs));
            }
        }
        return tag;
    }

    public void deserializeSideMap(CompoundTag tag) {
        for (int side = 0; side < 6; side++) {
            String key = "Side" + side;
            if (tag.contains(key)) {
                sideMap.put(side, SlotModePair.deserializeArray(tag.getCompound(key)));
            }
        }
        sideMapUpdated = false;
    }

    public int getTotalSlots() {
        return inputSlots + outputSlots + extraSlots;
    }
}
