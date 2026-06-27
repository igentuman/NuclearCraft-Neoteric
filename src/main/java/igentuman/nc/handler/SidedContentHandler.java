package igentuman.nc.handler;

import igentuman.nc.handler.sided.FluidCapabilityHandler;
import igentuman.nc.handler.sided.ItemCapabilityHandler;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

public class SidedContentHandler {

    @Nullable
    private ItemCapabilityHandler itemHandler;

    @Nullable
    private FluidCapabilityHandler fluidHandler;

    private BlockEntity tile;
    private int lastPushSide = 0;
    private int lastPullSide = 0;

    public SidedContentHandler() {}

    public void setItemHandler(ItemCapabilityHandler itemHandler) {
        this.itemHandler = itemHandler;
        if (tile != null) itemHandler.setBlockEntity(tile);
    }

    public void setFluidHandler(FluidCapabilityHandler fluidHandler) {
        this.fluidHandler = fluidHandler;
        if (tile != null) fluidHandler.setBlockEntity(tile);
    }

    public void setBlockEntity(BlockEntity tile) {
        this.tile = tile;
        if (itemHandler != null) itemHandler.setBlockEntity(tile);
        if (fluidHandler != null) fluidHandler.setBlockEntity(tile);
    }

    @Nullable
    public IItemHandler getItemCapability(@Nullable Direction side) {
        if (itemHandler == null) return null;
        return itemHandler.getCapability(side);
    }

    @Nullable
    public IFluidHandler getFluidCapability(@Nullable Direction side) {
        if (fluidHandler == null) return null;
        return fluidHandler.getCapability(side);
    }

    public boolean hasItemCapability() {
        return itemHandler != null;
    }

    public boolean hasFluidCapability() {
        return fluidHandler != null;
    }

    @Nullable
    public ItemCapabilityHandler getItemHandler() {
        return itemHandler;
    }

    @Nullable
    public FluidCapabilityHandler getFluidHandler() {
        return fluidHandler;
    }

    public void tick() {
        if (tile == null || tile.getLevel() == null || tile.getLevel().isClientSide()) return;

        Direction[] directions = Direction.values();

        if (itemHandler != null) {
            if (itemHandler.hasPush()) {
                itemHandler.pushItems(directions[lastPushSide % 6]);
            }
            if (itemHandler.hasPull()) {
                itemHandler.pullItems(directions[lastPullSide % 6]);
            }
        }

        if (fluidHandler != null) {
            if (fluidHandler.hasPush()) {
                fluidHandler.pushFluids(directions[lastPushSide % 6]);
            }
            if (fluidHandler.hasPull()) {
                fluidHandler.pullFluids(directions[lastPullSide % 6]);
            }
        }

        lastPushSide = (lastPushSide + 1) % 6;
        lastPullSide = (lastPullSide + 1) % 6;
    }

    public void toggleSideConfig(int slot, Direction absoluteDir) {
        Direction facing = itemHandler != null ? itemHandler.getFacing() :
                fluidHandler != null ? fluidHandler.getFacing() : Direction.NORTH;
        RelativeDirection relDir = RelativeDirection.toRelative(absoluteDir, facing);
        if (relDir == null) return;
        int side = relDir.ordinal();

        if (itemHandler != null && slot < itemHandler.getSlots()) {
            itemHandler.toggleMode(slot, side);
            itemHandler.invalidateWrappers();
        } else if (fluidHandler != null) {
            int fluidSlot = slot - (itemHandler != null ? itemHandler.getSlots() : 0);
            if (fluidSlot >= 0 && fluidSlot < fluidHandler.getTanks()) {
                fluidHandler.toggleMode(fluidSlot, side);
                fluidHandler.invalidateWrappers();
            }
        }
    }

    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        if (itemHandler != null) {
            tag.put("ItemHandler", itemHandler.serializeNBT(provider));
        }
        if (fluidHandler != null) {
            tag.put("FluidHandler", fluidHandler.serializeNBT(provider));
        }
        return tag;
    }

    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        if (itemHandler != null && tag.contains("ItemHandler")) {
            itemHandler.deserializeNBT(provider, tag.getCompound("ItemHandler"));
        }
        if (fluidHandler != null && tag.contains("FluidHandler")) {
            fluidHandler.deserializeNBT(provider, tag.getCompound("FluidHandler"));
        }
    }

    public enum RelativeDirection {
        FRONT,
        BACK,
        LEFT,
        RIGHT,
        UP,
        DOWN;

        public static Direction toAbsolute(RelativeDirection relativeDirection, Direction facing) {
            return switch (relativeDirection) {
                case FRONT -> facing;
                case BACK -> facing.getOpposite();
                case LEFT -> facing.getClockWise();
                case RIGHT -> facing.getCounterClockWise();
                case UP -> Direction.UP;
                case DOWN -> Direction.DOWN;
            };
        }

        public static RelativeDirection toRelative(Direction absoluteDirection, Direction facing) {
            if (absoluteDirection == facing) return FRONT;
            if (absoluteDirection == facing.getOpposite()) return BACK;
            if (absoluteDirection == facing.getClockWise()) return LEFT;
            if (absoluteDirection == facing.getCounterClockWise()) return RIGHT;
            if (absoluteDirection == Direction.UP) return UP;
            if (absoluteDirection == Direction.DOWN) return DOWN;
            return null;
        }

        public static String getDirectionName(int direction) {
            return values()[direction].name().toUpperCase();
        }
    }
}
