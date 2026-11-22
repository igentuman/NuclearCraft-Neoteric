package igentuman.nc.util.caps;

import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/** Routes fluid operations through current endpoints in the caller's transaction. */
public class RoutingFluidHandler implements ResourceHandler<FluidResource> {
    private final Supplier<List<Endpoint>> endpoints;

    public RoutingFluidHandler(Supplier<List<Endpoint>> endpoints) {
        this.endpoints = Objects.requireNonNull(endpoints);
    }

    /** Permissions apply to both indexed and whole-handler operations. */
    public record Endpoint(ResourceHandler<FluidResource> handler, boolean insert, boolean extract) {
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
    private Tank resolve(int index) {
        if (index < 0) throw new IndexOutOfBoundsException(index);
        for (var endpoint : endpoints.get()) {
            int size = endpoint.handler().size();
            if (index < size) return new Tank(endpoint, index);
            index -= size;
        }
        return null;
    }

    @Override public FluidResource getResource(int index) {
        var tank = resolve(index);
        return tank == null ? FluidResource.EMPTY : tank.endpoint().handler().getResource(tank.index());
    }

    @Override public long getAmountAsLong(int index) {
        var tank = resolve(index);
        return tank == null ? 0 : tank.endpoint().handler().getAmountAsLong(tank.index());
    }

    @Override public long getCapacityAsLong(int index, FluidResource resource) {
        var tank = resolve(index);
        return tank == null || !tank.endpoint().insert() ? 0
                : tank.endpoint().handler().getCapacityAsLong(tank.index(), resource);
    }

    @Override public boolean isValid(int index, FluidResource resource) {
        TransferPreconditions.checkNonEmpty(resource);
        var tank = resolve(index);
        return tank != null && tank.endpoint().insert() && tank.endpoint().handler().isValid(tank.index(), resource);
    }

    @Override public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        var tank = resolve(index);
        return tank == null || !tank.endpoint().insert() ? 0
                : tank.endpoint().handler().insert(tank.index(), resource, amount, transaction);
    }

    @Override public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        var tank = resolve(index);
        return tank == null || !tank.endpoint().extract() ? 0
                : tank.endpoint().handler().extract(tank.index(), resource, amount, transaction);
    }

    @Override public int insert(FluidResource resource, int amount, TransactionContext transaction) {
        return route(resource, amount, transaction, true);
    }

    @Override public int extract(FluidResource resource, int amount, TransactionContext transaction) {
        return route(resource, amount, transaction, false);
    }

    private int route(FluidResource resource, int amount, TransactionContext transaction, boolean insert) {
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

    private record Tank(Endpoint endpoint, int index) {}
}
