package igentuman.nc.handler.sided;

import net.minecraft.core.Direction;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;

public class FluidHandlerWrapper implements IFluidHandler {

    private final FluidCapabilityHandler handler;
    private final Direction side;

    public FluidHandlerWrapper(FluidCapabilityHandler handler, Direction side) {
        this.handler = handler;
        this.side = side;
    }

    @Override
    public int getTanks() {
        return handler.getTanks();
    }

    @Override
    public @NotNull FluidStack getFluidInTank(int tank) {
        return handler.getFluidInTank(tank);
    }

    @Override
    public int getTankCapacity(int tank) {
        return handler.getTankCapacity(tank);
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        return handler.isFluidValid(tank, stack);
    }

    @Override
    public int fill(@NotNull FluidStack resource, FluidAction action) {
        if (resource.isEmpty()) return 0;
        int remaining = resource.getAmount();
        int totalFilled = 0;
        for (int tank = 0; tank < handler.getTanks(); tank++) {
            if (!handler.canInsertFromSide(tank, resource, side)) continue;
            if (!handler.isFluidValid(tank, resource)) continue;
            int filled = handler.fillTank(tank, resource.copyWithAmount(remaining), action);
            totalFilled += filled;
            remaining -= filled;
            if (remaining <= 0) break;
        }
        return totalFilled;
    }

    @Override
    public @NotNull FluidStack drain(@NotNull FluidStack resource, FluidAction action) {
        if (resource.isEmpty()) return FluidStack.EMPTY;
        for (int tank = 0; tank < handler.getTanks(); tank++) {
            if (!handler.canExtractFromSide(tank, side)) continue;
            FluidStack inTank = handler.getFluidInTank(tank);
            if (!inTank.isEmpty() && FluidStack.isSameFluidSameComponents(resource, inTank)) {
                return handler.drainTank(tank, resource.getAmount(), action);
            }
        }
        return FluidStack.EMPTY;
    }

    @Override
    public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
        for (int tank = 0; tank < handler.getTanks(); tank++) {
            if (!handler.canExtractFromSide(tank, side)) continue;
            FluidStack inTank = handler.getFluidInTank(tank);
            if (!inTank.isEmpty()) {
                return handler.drainTank(tank, maxDrain, action);
            }
        }
        return FluidStack.EMPTY;
    }
}
