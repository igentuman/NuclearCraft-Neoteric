package igentuman.nc.handler.sided;

import igentuman.nc.handler.SlotModePair;
import igentuman.nc.handler.SidedContentHandler.RelativeDirection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.util.EnumMap;
import java.util.Map;

/** Sided item handler backing an internal inventory, enforcing per-face access and auto push/pull. */
public class ItemCapabilityHandler extends AbstractCapabilityHandler implements IItemHandlerModifiable {

    private final ItemStackHandler internal;
    private final Map<Direction, ItemHandlerWrapper> wrappers = new EnumMap<>(Direction.class);

    public ItemCapabilityHandler(int inputSlots, int outputSlots, int extraSlots) {
        super(inputSlots, outputSlots, extraSlots);
        this.internal = new ItemStackHandler(getTotalSlots()) {
            @Override
            protected void onContentsChanged(int slot) {
                if (tile != null) tile.setChanged();
            }
        };
    }

    public ItemStackHandler getInternalHandler() {
        return internal;
    }

    public IItemHandler getCapability(Direction side) {
        if (side == null) return this;
        return wrappers.computeIfAbsent(side, d -> new ItemHandlerWrapper(this, d));
    }

    public void invalidateWrappers() {
        wrappers.clear();
    }

    public boolean canInsertFromSide(int slot, ItemStack stack, Direction side) {
        SlotModePair.SlotMode mode = getModeForAbsoluteSide(side, slot);
        return mode == SlotModePair.SlotMode.INPUT
                || mode == SlotModePair.SlotMode.PULL
                || mode == SlotModePair.SlotMode.DEFAULT;
    }

    public boolean canExtractFromSide(int slot, Direction side) {
        SlotModePair.SlotMode mode = getModeForAbsoluteSide(side, slot);
        return mode == SlotModePair.SlotMode.OUTPUT
                || mode == SlotModePair.SlotMode.PUSH
                || mode == SlotModePair.SlotMode.DEFAULT;
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack) {
        internal.setStackInSlot(slot, stack);
    }

    @Override
    public int getSlots() {
        return internal.getSlots();
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return internal.getStackInSlot(slot);
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        return internal.insertItem(slot, stack, simulate);
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return internal.extractItem(slot, amount, simulate);
    }

    @Override
    public int getSlotLimit(int slot) {
        return internal.getSlotLimit(slot);
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return internal.isItemValid(slot, stack);
    }

    public void pushItems(Direction absoluteDir) {
        if (tile == null || tile.getLevel() == null || tile.getLevel().isClientSide()) return;
        BlockPos neighborPos = tile.getBlockPos().relative(absoluteDir);
        IItemHandler neighbor = tile.getLevel().getCapability(
                Capabilities.ItemHandler.BLOCK, neighborPos, absoluteDir.getOpposite());
        if (neighbor == null) return;

        Direction facing = getFacing();
        RelativeDirection relDir = RelativeDirection.toRelative(absoluteDir, facing);
        if (relDir == null) return;
        SlotModePair[] pairs = sideMap.get(relDir.ordinal());
        if (pairs == null) return;

        for (SlotModePair pair : pairs) {
            if (pair.getMode() != SlotModePair.SlotMode.PUSH) continue;
            int slot = pair.getSlot();
            if (slot < inputSlots) continue;
            ItemStack inSlot = internal.getStackInSlot(slot);
            if (inSlot.isEmpty()) continue;

            for (int neighborSlot = 0; neighborSlot < neighbor.getSlots(); neighborSlot++) {
                ItemStack remaining = neighbor.insertItem(neighborSlot, inSlot.copy(), true);
                if (remaining.getCount() < inSlot.getCount()) {
                    int toMove = inSlot.getCount() - remaining.getCount();
                    neighbor.insertItem(neighborSlot, inSlot.copyWithCount(toMove), false);
                    internal.extractItem(slot, toMove, false);
                    break;
                }
            }
        }
    }

    public void pullItems(Direction absoluteDir) {
        if (tile == null || tile.getLevel() == null || tile.getLevel().isClientSide()) return;
        BlockPos neighborPos = tile.getBlockPos().relative(absoluteDir);
        IItemHandler neighbor = tile.getLevel().getCapability(
                Capabilities.ItemHandler.BLOCK, neighborPos, absoluteDir.getOpposite());
        if (neighbor == null) return;

        Direction facing = getFacing();
        RelativeDirection relDir = RelativeDirection.toRelative(absoluteDir, facing);
        if (relDir == null) return;
        SlotModePair[] pairs = sideMap.get(relDir.ordinal());
        if (pairs == null) return;

        for (SlotModePair pair : pairs) {
            if (pair.getMode() != SlotModePair.SlotMode.PULL) continue;
            int slot = pair.getSlot();
            if (slot >= inputSlots) continue;

            for (int neighborSlot = 0; neighborSlot < neighbor.getSlots(); neighborSlot++) {
                ItemStack extracted = neighbor.extractItem(neighborSlot, 64, true);
                if (extracted.isEmpty()) continue;
                ItemStack remaining = internal.insertItem(slot, extracted.copy(), true);
                if (remaining.getCount() < extracted.getCount()) {
                    int toMove = extracted.getCount() - remaining.getCount();
                    ItemStack toInsert = extracted.copyWithCount(toMove);
                    ItemStack actualRemaining = internal.insertItem(slot, toInsert, false);
                    int moved = toMove - actualRemaining.getCount();
                    if (moved > 0) {
                        neighbor.extractItem(neighborSlot, moved, false);
                    }
                    break;
                }
            }
        }
    }

    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.put("Items", internal.serializeNBT(provider));
        tag.put("SideMap", serializeSideMap());
        return tag;
    }

    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        if (tag.contains("Items")) {
            internal.deserializeNBT(provider, tag.getCompound("Items"));
        }
        if (tag.contains("SideMap")) {
            deserializeSideMap(tag.getCompound("SideMap"));
            invalidateWrappers();
        }
    }
}
