package igentuman.nc.util.caps;

import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/** Routes item operations through current endpoints in the caller's transaction. */
public class RoutingItemHandler implements ResourceHandler<ItemResource> {
    private final Supplier<List<Endpoint>> endpoints;

    public RoutingItemHandler(Supplier<List<Endpoint>> endpoints) {
        this.endpoints = Objects.requireNonNull(endpoints);
    }

    /** Permissions apply to both indexed and whole-handler operations. */
    public record Endpoint(ResourceHandler<ItemResource> handler, boolean insert, boolean extract) {
        public Endpoint {
            Objects.requireNonNull(handler);
        }
    }

    @Override
    public int size() {
        int size = 0;
        for (var endpoint : endpoints.get()) size = Math.addExact(size, endpoint.handler().size());
        return size;
    }

    // Topology can shrink after a caller reads size(); stale indices become empty.
    private Slot resolve(int index) {
        if (index < 0) throw new IndexOutOfBoundsException(index);
        for (var endpoint : endpoints.get()) {
            int size = endpoint.handler().size();
            if (index < size) return new Slot(endpoint, index);
            index -= size;
        }
        return null;
    }

    @Override public ItemResource getResource(int index) {
        var slot = resolve(index);
        return slot == null ? ItemResource.EMPTY : slot.endpoint().handler().getResource(slot.index());
    }

    @Override public long getAmountAsLong(int index) {
        var slot = resolve(index);
        return slot == null ? 0 : slot.endpoint().handler().getAmountAsLong(slot.index());
    }

    @Override public long getCapacityAsLong(int index, ItemResource resource) {
        var slot = resolve(index);
        return slot == null || !slot.endpoint().insert() ? 0
                : slot.endpoint().handler().getCapacityAsLong(slot.index(), resource);
    }

    @Override public boolean isValid(int index, ItemResource resource) {
        TransferPreconditions.checkNonEmpty(resource);
        var slot = resolve(index);
        return slot != null && slot.endpoint().insert() && slot.endpoint().handler().isValid(slot.index(), resource);
    }

    @Override public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        var slot = resolve(index);
        return slot == null || !slot.endpoint().insert() ? 0
                : slot.endpoint().handler().insert(slot.index(), resource, amount, transaction);
    }

    @Override public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        var slot = resolve(index);
        return slot == null || !slot.endpoint().extract() ? 0
                : slot.endpoint().handler().extract(slot.index(), resource, amount, transaction);
    }

    @Override public int insert(ItemResource resource, int amount, TransactionContext transaction) {
        return route(resource, amount, transaction, true);
    }

    @Override public int extract(ItemResource resource, int amount, TransactionContext transaction) {
        return route(resource, amount, transaction, false);
    }

    private int route(ItemResource resource, int amount, TransactionContext transaction, boolean insert) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        if (amount == 0) return 0;
        int moved = 0;
        for (var endpoint : endpoints.get()) {
            if (insert ? !endpoint.insert() : !endpoint.extract()) continue;
            moved += insert ? endpoint.handler().insert(resource, amount - moved, transaction)
                    : endpoint.handler().extract(resource, amount - moved, transaction);
            if (moved == amount) break;
        }
        return moved;
    }

    private record Slot(Endpoint endpoint, int index) {}
}
