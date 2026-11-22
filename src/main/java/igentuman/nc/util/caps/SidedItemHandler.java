package igentuman.nc.util.caps;

import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.Objects;

/** Exposes a native item slot range with insert/extract restrictions on every operation. */
public class SidedItemHandler implements ResourceHandler<ItemResource> {
    private final ResourceHandler<ItemResource> inner;
    private final int startSlot;
    private final int slotCount;
    private final boolean canInsert;
    private final boolean canExtract;

    public SidedItemHandler(ResourceHandler<ItemResource> inner, int startSlot, int slotCount,
                            boolean canInsert, boolean canExtract) {
        Objects.checkFromIndexSize(startSlot, slotCount, inner.size());
        this.inner = inner;
        this.startSlot = startSlot;
        this.slotCount = slotCount;
        this.canInsert = canInsert;
        this.canExtract = canExtract;
    }

    private int slot(int index) { return startSlot + Objects.checkIndex(index, size()); }
    @Override public int size() { return slotCount; }
    @Override public ItemResource getResource(int index) { return inner.getResource(slot(index)); }
    @Override public long getAmountAsLong(int index) { return inner.getAmountAsLong(slot(index)); }
    @Override public long getCapacityAsLong(int index, ItemResource resource) {
        int slot = slot(index);
        return resource.isEmpty() || isValid(index, resource) ? inner.getCapacityAsLong(slot, resource) : 0;
    }
    @Override public boolean isValid(int index, ItemResource resource) {
        int slot = slot(index);
        return canInsert && inner.isValid(slot, resource);
    }
    @Override public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
        int slot = slot(index);
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        return canInsert ? inner.insert(slot, resource, amount, transaction) : 0;
    }
    @Override public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
        int slot = slot(index);
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        return canExtract ? inner.extract(slot, resource, amount, transaction) : 0;
    }
}
