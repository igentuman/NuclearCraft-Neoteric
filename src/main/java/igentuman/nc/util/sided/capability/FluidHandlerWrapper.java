package igentuman.nc.util.sided.capability;

import igentuman.nc.util.sided.SidedContentHandler;
import igentuman.nc.util.sided.SlotModePair;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

public class FluidHandlerWrapper implements IFluidHandler {
    private final FluidCapabilityHandler handler;
    private final Predicate<Integer> insert;
    private final Predicate<Integer> extract;
    private final SidedContentHandler.RelativeDirection direction;

    public FluidHandlerWrapper(FluidCapabilityHandler handler,
                               SidedContentHandler.RelativeDirection direction,
                               Predicate<Integer> insert,
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
            if(insert.test(pair.getSlot())) {
                FluidTank tank = handler.tanks.get(pair.getSlot());
                if(tank.isFluidValid(0, resource)) {
                    return tank.fill(resource.copy(), action);
                }
            }
        }
        return 0;
    }

    @Override
    public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
        for(SlotModePair pair: handler.sideMap.get(direction.ordinal())) {
            if(extract.test(pair.getSlot())) {
                FluidTank tank = handler.tanks.get(pair.getSlot());
                return tank.drain(resource, action);
            }
        }
        return FluidStack.EMPTY;
    }

    @Override
    public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
        for(SlotModePair pair: handler.sideMap.get(direction.ordinal())) {
            if (extract.test(pair.getSlot())) {
                FluidTank tank = handler.tanks.get(pair.getSlot());
                return tank.drain(maxDrain, action);
            }
        }
        return FluidStack.EMPTY;
    }
}