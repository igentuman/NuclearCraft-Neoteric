package igentuman.nc.util.caps;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;

/**
 * Combines multiple IFluidHandler instances into a single view.
 * Tanks are indexed sequentially across all delegates.
 * Supports separating fillable (input/global) tanks from drain-only (output) tanks.
 */
public class CombinedFluidHandler implements IFluidHandler {

    private final IFluidHandler[] delegates;
    /** Handlers that accept external fill (input + global tanks). */
    private final IFluidHandler[] fillable;
    /** Handlers that accept external drain (output + global tanks). */
    private final IFluidHandler[] drainable;
    private final int totalTanks;

    public CombinedFluidHandler(IFluidHandler... delegates) {
        this(delegates, delegates, delegates);
    }

    /**
     * @param allTanks    all tanks for indexing/query purposes
     * @param fillable    tanks that accept external fill (input + global)
     * @param drainable   tanks that accept external drain (output + global)
     */
    public CombinedFluidHandler(IFluidHandler[] allTanks, IFluidHandler[] fillable, IFluidHandler[] drainable) {
        this.delegates = allTanks;
        this.fillable = fillable;
        this.drainable = drainable;
        int total = 0;
        for (IFluidHandler handler : allTanks) {
            total += handler.getTanks();
        }
        this.totalTanks = total;
    }

    private HandlerTank resolve(int tank) {
        int offset = 0;
        for (IFluidHandler handler : delegates) {
            int size = handler.getTanks();
            if (tank < offset + size) {
                return new HandlerTank(handler, tank - offset);
            }
            offset += size;
        }
        return null;
    }

    @Override
    public int getTanks() {
        return totalTanks;
    }

    @Override
    public @NotNull FluidStack getFluidInTank(int tank) {
        HandlerTank ht = resolve(tank);
        return ht != null ? ht.handler.getFluidInTank(ht.tank) : FluidStack.EMPTY;
    }

    @Override
    public int getTankCapacity(int tank) {
        HandlerTank ht = resolve(tank);
        return ht != null ? ht.handler.getTankCapacity(ht.tank) : 0;
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        HandlerTank ht = resolve(tank);
        return ht != null && ht.handler.isFluidValid(ht.tank, stack);
    }

    @Override
    public int fill(@NotNull FluidStack resource, FluidAction action) {
        if (resource.isEmpty()) return 0;
        for (IFluidHandler handler : fillable) {
            int filled = handler.fill(resource, action);
            if (filled > 0) return filled;
        }
        return 0;
    }

    @Override
    public @NotNull FluidStack drain(@NotNull FluidStack resource, FluidAction action) {
        if (resource.isEmpty()) return FluidStack.EMPTY;
        for (IFluidHandler handler : drainable) {
            FluidStack drained = handler.drain(resource, action);
            if (!drained.isEmpty()) return drained;
        }
        return FluidStack.EMPTY;
    }

    @Override
    public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
        if (maxDrain <= 0) return FluidStack.EMPTY;
        for (IFluidHandler handler : drainable) {
            FluidStack drained = handler.drain(maxDrain, action);
            if (!drained.isEmpty()) return drained;
        }
        return FluidStack.EMPTY;
    }

    private record HandlerTank(IFluidHandler handler, int tank) {}
}
