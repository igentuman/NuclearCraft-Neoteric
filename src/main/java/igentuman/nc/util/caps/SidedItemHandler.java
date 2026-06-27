package igentuman.nc.util.caps;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

/**
 * A wrapper around an ItemStackHandler that exposes only a contiguous range of slots
 * and can restrict insertion and/or extraction.
 */
public class SidedItemHandler implements IItemHandler {

    private final ItemStackHandler inner;
    private final int startSlot;
    private final int slotCount;
    private final boolean canInsert;
    private final boolean canExtract;

    public SidedItemHandler(ItemStackHandler inner, int startSlot, int slotCount, boolean canInsert, boolean canExtract) {
        this.inner = inner;
        this.startSlot = startSlot;
        this.slotCount = slotCount;
        this.canInsert = canInsert;
        this.canExtract = canExtract;
    }

    @Override
    public int getSlots() {
        return slotCount;
    }

    @Override
    public @NotNull ItemStack getStackInSlot(int slot) {
        return inner.getStackInSlot(startSlot + slot);
    }

    @Override
    public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        if (!canInsert) return stack;
        return inner.insertItem(startSlot + slot, stack, simulate);
    }

    @Override
    public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (!canExtract) return ItemStack.EMPTY;
        return inner.extractItem(startSlot + slot, amount, simulate);
    }

    @Override
    public int getSlotLimit(int slot) {
        return inner.getSlotLimit(startSlot + slot);
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        if (!canInsert) return false;
        return inner.isItemValid(startSlot + slot, stack);
    }
}
