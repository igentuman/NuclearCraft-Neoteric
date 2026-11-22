package igentuman.nc.handler.sided;

import net.minecraft.core.Direction;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/** Native per-face fluid view that rechecks configured tank permissions on every operation. */
public class FluidHandlerWrapper implements ResourceHandler<FluidResource> {
    private final FluidCapabilityHandler handler;
    private final @Nullable Direction side;

    public FluidHandlerWrapper(FluidCapabilityHandler handler, @Nullable Direction side) {
        this.handler = handler;
        this.side = side;
    }

    @Override public int size() { return handler.getTanks(); }
    @Override public FluidResource getResource(int index) { return handler.getInternalHandler().getResource(index); }
    @Override public long getAmountAsLong(int index) { return handler.getInternalHandler().getAmountAsLong(index); }
    @Override public long getCapacityAsLong(int index, FluidResource resource) {
        Objects.checkIndex(index, size());
        return resource.isEmpty() || isValid(index, resource) ? handler.getTankCapacity(index) : 0;
    }
    @Override public boolean isValid(int index, FluidResource resource) {
        Objects.checkIndex(index, size());
        return (side == null || handler.canInsertFromSide(index, resource.toStack(1), side))
                && handler.getInternalHandler().isValid(index, resource);
    }
    @Override public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
        Objects.checkIndex(index, size());
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        return isValid(index, resource) ? handler.insert(index, resource, amount, transaction) : 0;
    }
    @Override public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) {
        Objects.checkIndex(index, size());
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        return side == null || handler.canExtractFromSide(index, side)
                ? handler.extract(index, resource, amount, transaction) : 0;
    }
}
