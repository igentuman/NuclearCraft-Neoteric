package igentuman.nc.handler.fluid;

import igentuman.nc.handler.sided.FluidCapabilityHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.Objects;

/** Native manual access to one tank, allowing input recovery but never filling outputs. */
public record ContainerFluidHandler(FluidCapabilityHandler handler, int tank) implements ResourceHandler<FluidResource> {
    public ContainerFluidHandler {
        Objects.checkIndex(tank, handler.getTanks());
    }
    private void checkIndex(int index) { Objects.checkIndex(index, 1); }
    @Override public int size() { return 1; }
    @Override public FluidResource getResource(int index) {
        checkIndex(index);
        return handler.getInternalHandler().getResource(tank);
    }
    @Override public long getAmountAsLong(int index) {
        checkIndex(index);
        return handler.getInternalHandler().getAmountAsLong(tank);
    }
    @Override public long getCapacityAsLong(int index, FluidResource resource) {
        checkIndex(index);
        return resource.isEmpty() || isValid(index, resource) ? handler.getTankCapacity(tank) : 0;
    }
    @Override public boolean isValid(int index, FluidResource resource) {
        checkIndex(index);
        return (tank < handler.inputSlots || tank >= handler.inputSlots + handler.outputSlots)
                && handler.getInternalHandler().isValid(tank, resource);
    }
    @Override public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
        checkIndex(index);
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        return isValid(index, resource) ? handler.insert(tank, resource, amount, transaction) : 0;
    }
    @Override public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) {
        checkIndex(index);
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        return handler.extract(tank, resource, amount, transaction);
    }
}
