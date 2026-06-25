package igentuman.nc.handler.sided.capability;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;

/**
 * Fluid tank for the MSR. Beyond its own capacity it enforces two reactor constraints:
 * <ul>
 *   <li>the shared internal volume ({@code freeVolume}) — fill is clamped so salt + pebbles never
 *       exceed the reactor's global volume;</li>
 *   <li>an optional per-tick throughput cap ({@code maxFillPerTick} / {@code maxDrainPerTick}, mB)
 *       set by the player as the molten-salt input / output rate.</li>
 * </ul>
 * Counters reset each reactor tick via {@link #resetTickCounters()}.
 */
public class MSRVolumeTank extends NcFluidTank {

    private DoubleSupplier freeVolume;
    private IntSupplier maxFillPerTick;
    private IntSupplier maxDrainPerTick;
    private int filledThisTick = 0;
    private int drainedThisTick = 0;

    public MSRVolumeTank(int capacity) {
        super(capacity);
    }

    public void setFreeVolume(DoubleSupplier freeVolume) {
        this.freeVolume = freeVolume;
    }

    public void setMaxFillPerTick(IntSupplier maxFillPerTick) {
        this.maxFillPerTick = maxFillPerTick;
    }

    public void setMaxDrainPerTick(IntSupplier maxDrainPerTick) {
        this.maxDrainPerTick = maxDrainPerTick;
    }

    public void resetTickCounters() {
        filledThisTick = 0;
        drainedThisTick = 0;
    }

    @Override
    public int fill(FluidStack resource, IFluidHandler.FluidAction action) {
        if (resource == null || resource.isEmpty()) {
            return 0;
        }
        if (freeVolume != null) {
            int free = (int) Math.max(0, Math.floor(freeVolume.getAsDouble()));
            if (free <= 0) return 0;
            if (resource.getAmount() > free) {
                resource = resource.copy();
                resource.setAmount(free);
            }
        }
        if (maxFillPerTick != null) {
            int remain = maxFillPerTick.getAsInt() - filledThisTick;
            if (remain <= 0) return 0;
            if (resource.getAmount() > remain) {
                resource = resource.copy();
                resource.setAmount(remain);
            }
        }
        int filled = super.fill(resource, action);
        if (action.execute()) {
            filledThisTick += filled;
        }
        return filled;
    }

    @Override
    public FluidStack drain(int maxDrain, IFluidHandler.FluidAction action) {
        if (maxDrainPerTick != null) {
            int remain = maxDrainPerTick.getAsInt() - drainedThisTick;
            if (remain <= 0) return FluidStack.EMPTY;
            maxDrain = Math.min(maxDrain, remain);
        }
        FluidStack drained = super.drain(maxDrain, action);
        if (action.execute()) {
            drainedThisTick += drained.getAmount();
        }
        return drained;
    }

    @Override
    public FluidStack drain(FluidStack resource, IFluidHandler.FluidAction action) {
        if (resource == null || resource.isEmpty()) {
            return FluidStack.EMPTY;
        }
        if (maxDrainPerTick != null) {
            int remain = maxDrainPerTick.getAsInt() - drainedThisTick;
            if (remain <= 0) return FluidStack.EMPTY;
            if (resource.getAmount() > remain) {
                resource = resource.copy();
                resource.setAmount(remain);
            }
        }
        FluidStack drained = super.drain(resource, action);
        if (action.execute()) {
            drainedThisTick += drained.getAmount();
        }
        return drained;
    }
}
