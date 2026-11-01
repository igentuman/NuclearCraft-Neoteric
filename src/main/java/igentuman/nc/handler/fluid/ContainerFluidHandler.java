package igentuman.nc.handler.fluid;

import igentuman.nc.handler.sided.FluidCapabilityHandler;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

/** Manual container access to one tank, allowing input recovery but never filling outputs. */
public record ContainerFluidHandler(FluidCapabilityHandler handler, int tank) implements IFluidHandler {

    public ContainerFluidHandler {
        if (tank < 0 || tank >= handler.getTanks()) throw new IllegalArgumentException("Invalid tank: " + tank);
    }

    @Override
    public int getTanks() {
        return 1;
    }

    @Override
    public FluidStack getFluidInTank(int tank) {
        return tank == 0 ? handler.getFluidInTank(this.tank) : FluidStack.EMPTY;
    }

    @Override
    public int getTankCapacity(int tank) {
        return tank == 0 ? handler.getTankCapacity(this.tank) : 0;
    }

    @Override
    public boolean isFluidValid(int tank, FluidStack stack) {
        return tank == 0 && (this.tank < handler.inputSlots
                || this.tank >= handler.inputSlots + handler.outputSlots)
                && handler.isFluidValid(this.tank, stack);
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        return isFluidValid(0, resource) ? handler.fillTank(tank, resource, action) : 0;
    }

    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        if (resource.isEmpty() || !FluidStack.isSameFluidSameComponents(resource, handler.getFluidInTank(tank))) {
            return FluidStack.EMPTY;
        }
        return drain(resource.getAmount(), action);
    }

    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        return handler.drainTank(tank, maxDrain, action);
    }
}
