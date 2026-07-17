package igentuman.nc.block.heat_exchanger.entity;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidTank;

import java.util.function.Supplier;

/**
 * Scoped fluid view for a heat exchanger coolant port. Exposes exactly one input tank
 * (insert only) and one output tank (extract only) of the controller, so a hot port can
 * never touch cold tanks and vice versa.
 */
public class HeatExchangerPortFluidHandler implements IFluidHandler {

    private final Supplier<HeatExchangerControllerBE> controller;
    private final int inIdx;
    private final int outIdx;
    private final boolean hot;

    public HeatExchangerPortFluidHandler(Supplier<HeatExchangerControllerBE> controller, int inIdx, int outIdx, boolean hot) {
        this.controller = controller;
        this.inIdx = inIdx;
        this.outIdx = outIdx;
        this.hot = hot;
    }

    private FluidTank tank(int idx) {
        HeatExchangerControllerBE be = controller.get();
        if (be == null) return null;
        return be.getFluidTank(idx);
    }

    @Override
    public int getTanks() {
        return 2;
    }

    @Override
    public FluidStack getFluidInTank(int tank) {
        FluidTank t = tank(tank == 0 ? inIdx : outIdx);
        return t == null ? FluidStack.EMPTY : t.getFluid();
    }

    @Override
    public int getTankCapacity(int tank) {
        FluidTank t = tank(tank == 0 ? inIdx : outIdx);
        return t == null ? 0 : t.getCapacity();
    }

    @Override
    public boolean isFluidValid(int tank, FluidStack stack) {
        if (tank != 0) return false; // output tank rejects insertion
        HeatExchangerControllerBE be = controller.get();
        return be != null && be.isAllowedInput(hot, stack);
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        if (resource.isEmpty()) return 0;
        HeatExchangerControllerBE be = controller.get();
        if (be == null || !be.isAllowedInput(hot, resource)) return 0;
        FluidTank in = be.getFluidTank(inIdx);
        return in == null ? 0 : in.fill(resource, action);
    }

    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        FluidTank out = tank(outIdx);
        if (out == null) return FluidStack.EMPTY;
        return out.drain(resource, action);
    }

    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        FluidTank out = tank(outIdx);
        if (out == null) return FluidStack.EMPTY;
        return out.drain(maxDrain, action);
    }
}
