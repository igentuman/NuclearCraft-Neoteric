package igentuman.nc.util.caps;

import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.Arrays;
import java.util.Objects;

/** Combines native fluid stores with separate insertion and extraction permissions. */
public class CombinedFluidHandler implements ResourceHandler<FluidResource> {
    private final ResourceHandler<FluidResource>[] delegates;
    private final ResourceHandler<FluidResource>[] fillable;
    private final ResourceHandler<FluidResource>[] drainable;

    @SafeVarargs
    public CombinedFluidHandler(ResourceHandler<FluidResource>... delegates) {
        this(delegates, delegates, delegates);
    }

    public CombinedFluidHandler(ResourceHandler<FluidResource>[] allTanks,
                                ResourceHandler<FluidResource>[] fillable,
                                ResourceHandler<FluidResource>[] drainable) {
        this.delegates = allTanks.clone();
        this.fillable = fillable.clone();
        this.drainable = drainable.clone();
        for (var handler : delegates) Objects.requireNonNull(handler);
        for (var group : java.util.List.of(this.fillable, this.drainable)) {
            for (var handler : group) {
                if (!allows(delegates, handler)) throw new IllegalArgumentException("Unlisted fluid delegate");
            }
        }
    }

    private boolean allows(ResourceHandler<FluidResource>[] group, ResourceHandler<FluidResource> handler) {
        return Arrays.stream(group).anyMatch(candidate -> candidate == handler);
    }

    @Override public int size() {
        int size = 0;
        for (var handler : delegates) size = Math.addExact(size, handler.size());
        return size;
    }
    private HandlerTank resolve(int index) {
        Objects.checkIndex(index, size());
        for (var handler : delegates) {
            if (index < handler.size()) return new HandlerTank(handler, index);
            index -= handler.size();
        }
        throw new IndexOutOfBoundsException(index);
    }
    @Override public FluidResource getResource(int index) {
        var tank = resolve(index);
        return tank.handler().getResource(tank.index());
    }
    @Override public long getAmountAsLong(int index) {
        var tank = resolve(index);
        return tank.handler().getAmountAsLong(tank.index());
    }
    @Override public long getCapacityAsLong(int index, FluidResource resource) {
        var tank = resolve(index);
        return resource.isEmpty() || isValid(index, resource) ? tank.handler().getCapacityAsLong(tank.index(), resource) : 0;
    }
    @Override public boolean isValid(int index, FluidResource resource) {
        var tank = resolve(index);
        return allows(fillable, tank.handler()) && tank.handler().isValid(tank.index(), resource);
    }
    @Override public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
        var tank = resolve(index);
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        return allows(fillable, tank.handler()) ? tank.handler().insert(tank.index(), resource, amount, transaction) : 0;
    }
    @Override public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) {
        var tank = resolve(index);
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        return allows(drainable, tank.handler()) ? tank.handler().extract(tank.index(), resource, amount, transaction) : 0;
    }
    private record HandlerTank(ResourceHandler<FluidResource> handler, int index) {}
}
