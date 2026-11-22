package igentuman.nc.handler.sided;

import net.minecraft.core.Direction;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.Objects;

/** Native per-face item view that rechecks slot modes, GUI restrictions and insertion locks on every operation. */
public class ItemHandlerWrapper implements ResourceHandler<ItemResource> {
    private final ItemCapabilityHandler handler;
    private final Direction side;

    public ItemHandlerWrapper(ItemCapabilityHandler handler, Direction side) {
        this.handler = handler;
        this.side = side;
    }
    @Override public int size() { return handler.size(); }
    @Override public ItemResource getResource(int index) { return handler.getResource(index); }
    @Override public long getAmountAsLong(int index) { return handler.getAmountAsLong(index); }
    @Override public long getCapacityAsLong(int index, ItemResource resource) {
        Objects.checkIndex(index, size());
        return resource.isEmpty() || isValid(index, resource) ? handler.getCapacityAsLong(index, resource) : 0;
    }
    @Override public boolean isValid(int index, ItemResource resource) {
        Objects.checkIndex(index, size());
        return !handler.isSideInsertLocked() && handler.canInsertFromSide(index, resource.toStack(1), side)
                && handler.isValid(index, resource);
    }
    @Override public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
        Objects.checkIndex(index, size());
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        return isValid(index, resource) ? handler.insert(index, resource, amount, transaction) : 0;
    }
    @Override public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
        Objects.checkIndex(index, size());
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        return handler.canExtractFromSide(index, side) ? handler.extract(index, resource, amount, transaction) : 0;
    }
}
