package igentuman.nc.handler.sided;

import igentuman.nc.handler.SlotModePair;
import igentuman.nc.handler.item.CustomInventoryHandler;
import igentuman.nc.handler.SidedContentHandler.RelativeDirection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.RangedResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.IntConsumer;
import java.util.function.Predicate;

/** Sided item handler backing an internal inventory, enforcing per-face access and auto push/pull. */
public class ItemCapabilityHandler extends AbstractCapabilityHandler implements ResourceHandler<ItemResource> {

    private final CustomInventoryHandler internal;
    private final Map<Direction, ItemHandlerWrapper> wrappers = new EnumMap<>(Direction.class);
    private final Predicate<ItemStack>[] validators;
    private IntConsumer slotChangeListener;
    private boolean sideInsertLocked = false;
    private final boolean[] guiOnlySlots;

    @SuppressWarnings("unchecked")
    public ItemCapabilityHandler(int inputSlots, int outputSlots, int extraSlots) {
        super(inputSlots, outputSlots, extraSlots);
        int total = getTotalSlots();
        this.validators = new Predicate[total];
        this.guiOnlySlots = new boolean[total];
        for (int i = 0; i < total; i++) {
            this.validators[i] = s -> true;
        }
        this.internal = new CustomInventoryHandler(total) {
            @Override
            protected void onContentsChanged(int slot) {
                if (tile != null) tile.setChanged();
                if (slotChangeListener != null) slotChangeListener.accept(slot);
            }

            @Override
            public boolean isItemValid(int slot, ItemStack stack) {
                if (slot >= 0 && slot < validators.length && !validators[slot].test(stack)) return false;
                return super.isItemValid(slot, stack);
            }
        };
    }

    /** Restricts which items a slot accepts. A null validator clears the restriction. */
    public void setSlotValidator(int slot, Predicate<ItemStack> validator) {
        internal.checkDirectMutation();
        if (slot >= 0 && slot < validators.length) {
            validators[slot] = validator == null ? s -> true : validator;
        }
    }

    public void setGuiOnlySlot(int slot, boolean guiOnly) {
        internal.checkDirectMutation();
        if (slot >= 0 && slot < guiOnlySlots.length) {
            guiOnlySlots[slot] = guiOnly;
        }
    }

    public boolean isGuiOnlySlot(int slot) {
        return slot >= 0 && slot < guiOnlySlots.length && guiOnlySlots[slot];
    }

    public void setSlotChangeListener(IntConsumer listener) {
        this.slotChangeListener = listener;
    }

    public void setSideInsertLocked(boolean locked) {
        internal.checkDirectMutation();
        this.sideInsertLocked = locked;
    }

    public boolean isSideInsertLocked() {
        return sideInsertLocked;
    }

    public CustomInventoryHandler getInternalHandler() {
        return internal;
    }

    public ResourceHandler<ItemResource> getCapability(Direction side) {
        if (side == null) return this;
        return wrappers.computeIfAbsent(side, d -> new ItemHandlerWrapper(this, d));
    }

    public void invalidateWrappers() {
        wrappers.clear();
    }

    public boolean canInsertFromSide(int slot, ItemStack stack, Direction side) {
        if (isGuiOnlySlot(slot)) return false;
        SlotModePair.SlotMode mode = getModeForAbsoluteSide(side, slot);
        return mode == SlotModePair.SlotMode.INPUT
                || mode == SlotModePair.SlotMode.PULL
                || mode == SlotModePair.SlotMode.DEFAULT;
    }

    public boolean canExtractFromSide(int slot, Direction side) {
        if (isGuiOnlySlot(slot)) return false;
        SlotModePair.SlotMode mode = getModeForAbsoluteSide(side, slot);
        return mode == SlotModePair.SlotMode.OUTPUT
                || mode == SlotModePair.SlotMode.PUSH
                || mode == SlotModePair.SlotMode.DEFAULT;
    }

    public void setStackInSlot(int slot, ItemStack stack) {
        internal.setStackInSlot(slot, stack);
    }

    public void set(int index, ItemResource resource, int amount) { internal.set(index, resource, amount); }

    public int getSlots() {
        return internal.getSlots();
    }

    public ItemStack getStackInSlot(int slot) {
        return internal.getStackInSlot(slot);
    }

    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        return internal.insertItem(slot, stack, simulate);
    }

    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return internal.extractItem(slot, amount, simulate);
    }

    public int getSlotLimit(int slot) {
        return internal.getSlotLimit(slot);
    }

    public boolean isItemValid(int slot, ItemStack stack) {
        return internal.isItemValid(slot, stack);
    }

    @Override public int size() { return internal.size(); }
    @Override public ItemResource getResource(int index) { return internal.getResource(index); }
    @Override public long getAmountAsLong(int index) { return internal.getAmountAsLong(index); }
    @Override public long getCapacityAsLong(int index, ItemResource resource) { return internal.getCapacityAsLong(index, resource); }
    @Override public boolean isValid(int index, ItemResource resource) { return internal.isValid(index, resource); }
    @Override public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
        return internal.insert(index, resource, amount, transaction);
    }
    @Override public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
        return internal.extract(index, resource, amount, transaction);
    }

    public void pushItems(Direction absoluteDir) { transferItems(absoluteDir, true); }
    public void pullItems(Direction absoluteDir) { transferItems(absoluteDir, false); }

    private void transferItems(Direction absoluteDir, boolean push) {
        if (tile == null || tile.isRemoved() || tile.getLevel() == null || tile.getLevel().isClientSide()) return;
        BlockPos neighborPos = tile.getBlockPos().relative(absoluteDir);
        ResourceHandler<ItemResource> neighbor = tile.getLevel().getCapability(
                Capabilities.Item.BLOCK, neighborPos, absoluteDir.getOpposite());
        if (neighbor == null) return;
        RelativeDirection relDir = RelativeDirection.toRelative(absoluteDir, getFacing());
        if (relDir == null) return;
        SlotModePair[] pairs = sideMap.get(relDir.ordinal());
        if (pairs == null) return;
        ResourceHandler<ItemResource> sided = getCapability(absoluteDir);
        for (SlotModePair pair : pairs) {
            if (pair.getMode() != (push ? SlotModePair.SlotMode.PUSH : SlotModePair.SlotMode.PULL)) continue;
            int slot = pair.getSlot();
            if (slot < 0 || slot >= size() || (push ? slot < inputSlots : slot >= inputSlots)) continue;
            var local = RangedResourceHandler.ofSingleIndex(sided, slot);
            // Retain first accepting neighbour slot and the original 64-item pull ceiling.
            for (int neighborSlot = 0; neighborSlot < neighbor.size(); neighborSlot++) {
                var remote = RangedResourceHandler.ofSingleIndex(neighbor, neighborSlot);
                try (var transaction = Transaction.openRoot()) {
                    int moved = ResourceHandlerUtil.move(push ? local : remote, push ? remote : local,
                            resource -> true, push ? (int) internal.getAmountAsLong(slot) : 64, transaction);
                    if (moved > 0) {
                        transaction.commit();
                        break;
                    }
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
            internal.deserializeNBT(provider, tag.getCompound("Items").orElseThrow());
        }
        if (tag.contains("SideMap")) {
            deserializeSideMap(tag.getCompound("SideMap").orElseThrow());
            invalidateWrappers();
        }
    }
}
