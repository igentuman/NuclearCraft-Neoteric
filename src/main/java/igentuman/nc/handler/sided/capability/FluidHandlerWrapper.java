package igentuman.nc.handler.sided.capability;

import igentuman.nc.handler.sided.SidedContentHandler;
import igentuman.nc.handler.sided.SlotModePair;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiPredicate;
import java.util.function.Predicate;

public class FluidHandlerWrapper implements IFluidHandler {
    private final FluidCapabilityHandler handler;
    private final BiPredicate<Integer, FluidStack> insert;
    private final Predicate<Integer> extract;
    private final SidedContentHandler.RelativeDirection direction;

    public FluidHandlerWrapper(FluidCapabilityHandler handler,
                               SidedContentHandler.RelativeDirection direction,
                               BiPredicate<Integer, FluidStack> insert,
                               Predicate<Integer> extract) {
        this.handler = handler;
        this.direction = direction;
        this.insert = insert;
        this.extract = extract;
    }
    @Override
    public int getTanks() {
        return handler.tanks.size();
    }

    @Override
    public @NotNull FluidStack getFluidInTank(int tank) {
        return handler.tanks.get(tank).getFluidInTank(0);
    }

    @Override
    public int getTankCapacity(int tank) {
        return handler.tanks.get(tank).getTankCapacity(0);
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        return handler.tanks.get(tank).isFluidValid(tank, stack);
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        for(SlotModePair pair: handler.sideMap.get(direction.ordinal())) {
            if(insert.test(pair.getSlot(), resource)) {
                NcFluidTank tank = handler.tanks.get(pair.getSlot());
                if(tank.isFluidValid(pair.getSlot(), resource)) {
                    return tank.fill(resource.copy(), action);
                }
            }
        }
        return 0;
    }

    @Override
    public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
        for(SlotModePair pair: handler.sideMap.get(direction.ordinal())) {
            if(extract.test(pair.getSlot()) && (resource.isEmpty() || resource.isFluidEqual(handler.tanks.get(pair.getSlot()).getFluid()))) {
                NcFluidTank tank = handler.tanks.get(pair.getSlot());
                return tank.drain(resource, action);
            }
        }
        return FluidStack.EMPTY;
    }

    @Override
    public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
        for(SlotModePair pair: handler.sideMap.get(direction.ordinal())) {
            if (extract.test(pair.getSlot())) {
                NcFluidTank tank = handler.tanks.get(pair.getSlot());
                return tank.drain(maxDrain, action);
            }
        }
        return FluidStack.EMPTY;
    }
}