package igentuman.nc.handler.storage;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.Objects;

/** Resolving view of shared inventory storage; aliases transfer through the backing store's journals. */
public abstract class LinkedInventoryHandler implements ResourceHandler<ItemResource> {
    private final int size;

    protected LinkedInventoryHandler(int size) {
        if (size < 0) throw new IllegalArgumentException("Negative linked inventory size");
        this.size = size;
    }

    /** Creation, when requested, must participate in the supplied transaction. */
    protected abstract StoredInventory resolve(boolean create, TransactionContext transaction);

    private StoredInventory inventory(boolean create, TransactionContext transaction) {
        StoredInventory inventory = resolve(create, transaction);
        if (inventory != null && inventory.size() != size) throw new IllegalStateException("Linked inventory size mismatch");
        return inventory;
    }

    @Override public int size() { return size; }
    public int getSlots() { return size; }
    public int getSlotLimit(int index) { Objects.checkIndex(index, size); return 64; }
    public ItemStack getStackInSlot(int index) {
        Objects.checkIndex(index, size);
        StoredInventory inventory = inventory(false, null);
        return inventory == null ? ItemStack.EMPTY : inventory.get(index);
    }
    public boolean isItemValid(int index, ItemStack stack) { Objects.checkIndex(index, size); return true; }
    public void setStackInSlot(int index, ItemStack stack) {
        Objects.checkIndex(index, size);
        if (!stack.isEmpty() && !isItemValid(index, stack)) return;
        // Avoid creating a saved entry merely to write an empty menu slot.
        StoredInventory inventory = inventory(!stack.isEmpty(), null);
        if (inventory != null) inventory.set(index, stack);
    }
    public void set(int index, ItemResource resource, int amount) {
        TransferPreconditions.checkNonNegative(amount);
        if (resource.isEmpty() && amount > 0) throw new IllegalArgumentException("Empty item with positive amount");
        setStackInSlot(index, resource.toStack(amount));
    }
    @Override public ItemResource getResource(int index) {
        Objects.checkIndex(index, size);
        StoredInventory inventory = inventory(false, null);
        return inventory == null ? ItemResource.EMPTY : inventory.getResource(index);
    }
    @Override public long getAmountAsLong(int index) {
        Objects.checkIndex(index, size);
        StoredInventory inventory = inventory(false, null);
        return inventory == null ? 0 : inventory.getAmountAsLong(index);
    }
    @Override public long getCapacityAsLong(int index, ItemResource resource) {
        Objects.checkIndex(index, size);
        return resource.isEmpty() ? getSlotLimit(index)
                : isValid(index, resource) ? Math.min(getSlotLimit(index), resource.getMaxStackSize()) : 0;
    }
    @Override public boolean isValid(int index, ItemResource resource) { return isItemValid(index, resource.toStack(1)); }
    @Override public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
        Objects.checkIndex(index, size);
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        if (amount == 0 || !isValid(index, resource)) return 0;
        try (var child = Transaction.open(transaction)) {
            StoredInventory inventory = inventory(true, child);
            int inserted = inventory == null ? 0 : inventory.insert(index, resource, amount, child);
            if (inserted > 0) child.commit();
            return inserted;
        }
    }
    @Override public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
        Objects.checkIndex(index, size);
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        StoredInventory inventory = inventory(false, transaction);
        return inventory == null ? 0 : inventory.extract(index, resource, amount, transaction);
    }
    /** Temporary local convenience for remaining callers; native transfers share a transaction instead. */
    public ItemStack insertItem(int index, ItemStack stack, boolean simulate) {
        if (stack.isEmpty()) return ItemStack.EMPTY;
        try (var tx = Transaction.openRoot()) {
            int inserted = insert(index, ItemResource.of(stack), stack.getCount(), tx);
            if (!simulate) tx.commit();
            return stack.copyWithCount(stack.getCount() - inserted);
        }
    }
    public ItemStack extractItem(int index, int amount, boolean simulate) {
        TransferPreconditions.checkNonNegative(amount);
        ItemResource resource = getResource(index);
        if (resource.isEmpty() || amount == 0) return ItemStack.EMPTY;
        try (var tx = Transaction.openRoot()) {
            int extracted = extract(index, resource, amount, tx);
            if (!simulate) tx.commit();
            return resource.toStack(extracted);
        }
    }
}
