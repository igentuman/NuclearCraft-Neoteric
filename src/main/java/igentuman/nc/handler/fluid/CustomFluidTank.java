package igentuman.nc.handler.fluid;

import net.neoforged.neoforge.fluids.FluidStack;

import java.util.function.Predicate;

/** Fluid tank handler with factories that wire capacity, optional validators, and change callbacks. */
public class CustomFluidTank extends FluidStackHandler {

    public CustomFluidTank(int tanks, int defaultCapacity) {
        super(tanks, defaultCapacity);
    }

    /**
     * Creates a single-tank handler with the given capacity and a change callback.
     */
    public static CustomFluidTank create(int capacity, Runnable onChanged) {
        return new CustomFluidTank(1, capacity) {
            @Override
            protected void onContentsChanged(int tank) {
                onChanged.run();
            }
        };
    }

    /**
     * Creates a single-tank handler with the given capacity, validator, and a change callback.
     */
    public static CustomFluidTank create(int capacity, Predicate<FluidStack> validator, Runnable onChanged) {
        CustomFluidTank tank = new CustomFluidTank(1, capacity) {
            @Override
            protected void onContentsChanged(int t) {
                onChanged.run();
            }
        };
        tank.setTankValidator(0, validator);
        return tank;
    }

    /**
     * Creates a multi-tank handler with the given size and default capacity, plus a change callback.
     */
    public static CustomFluidTank create(int tanks, int defaultCapacity, Runnable onChanged) {
        return new CustomFluidTank(tanks, defaultCapacity) {
            @Override
            protected void onContentsChanged(int tank) {
                onChanged.run();
            }
        };
    }
}
