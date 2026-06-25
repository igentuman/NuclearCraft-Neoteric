package igentuman.nc.handler.sided.capability;

/**
 * Fluid handler whose tanks are {@link MSRVolumeTank}s so fluid inputs respect the reactor's
 * shared internal volume budget.
 */
public class MSRFluidCapabilityHandler extends FluidCapabilityHandler {

    public MSRFluidCapabilityHandler(int inputSlots, int outputSlots, int inputCapacity, int outputCapacity) {
        super(inputSlots, outputSlots, inputCapacity, outputCapacity);
    }

    @Override
    protected NcFluidTank createTank(int capacity) {
        return new MSRVolumeTank(capacity);
    }
}
