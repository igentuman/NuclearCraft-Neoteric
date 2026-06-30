package igentuman.nc.handler.fluid;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

/**
 * A multi-tank fluid handler that mirrors the ItemStackHandler pattern.
 * Each tank has its own capacity and optional fluid validator.
 * Implements IFluidHandler and INBTSerializable for seamless capability and NBT support.
 */
public class FluidStackHandler implements IFluidHandler, INBTSerializable<CompoundTag> {

    protected int tanks;
    protected FluidStack[] fluids;
    protected int[] capacities;
    protected Predicate<FluidStack>[] validators;

    // External access restriction fields
    private FluidStackHandler inner;
    private int[] fillableTanks;
    private int[] drainableTanks;

    public FluidStackHandler(int tanks, int[] capacities) {
        this.tanks = tanks;
        this.fluids = new FluidStack[tanks];
        this.capacities = capacities;
        this.validators = new Predicate[tanks];
        for (int i = 0; i < tanks; i++) {
            this.fluids[i] = FluidStack.EMPTY;
            this.validators[i] = fs -> true;
        }
    }

    @SuppressWarnings("unchecked")
    public FluidStackHandler(int tanks, int defaultCapacity) {
        this.tanks = tanks;
        this.fluids = new FluidStack[tanks];
        this.capacities = new int[tanks];
        this.validators = new Predicate[tanks];
        for (int i = 0; i < tanks; i++) {
            this.fluids[i] = FluidStack.EMPTY;
            this.capacities[i] = defaultCapacity;
            this.validators[i] = fs -> true;
        }
    }

    /**
     * Private constructor for creating an external-access-restricted view.
     * The returned handler delegates all reads to the inner handler but restricts
     * fill operations to fillableTanks and drain operations to drainableTanks.
     */
    private FluidStackHandler(FluidStackHandler inner, int[] fillableTanks, int[] drainableTanks) {
        this.inner = inner;
        this.fillableTanks = fillableTanks;
        this.drainableTanks = drainableTanks;
        this.tanks = inner.tanks;
        this.fluids = inner.fluids;
        this.capacities = inner.capacities;
        this.validators = inner.validators;
    }

    /**
     * Creates an external-access-restricted view of this handler.
     * Fill operations only target fillable tanks (input + global).
     * Drain operations only target drainable tanks (output + global).
     * All tanks remain visible for reading (getTanks, getFluidInTank, etc.).
     *
     * @param fillableTanks  tank indices that accept external fill (input + global)
     * @param drainableTanks tank indices that allow external drain (output + global)
     * @return a restricted IFluidHandler view backed by this handler
     */
    public FluidStackHandler createExternalView(int[] fillableTanks, int[] drainableTanks) {
        return new FluidStackHandler(this, fillableTanks, drainableTanks);
    }

    private boolean isExternalView() {
        return inner != null;
    }

    @SuppressWarnings("unchecked")
    public void setSize(int size) {
        this.tanks = size;
        this.fluids = new FluidStack[size];
        this.capacities = new int[size];
        this.validators = new Predicate[size];
        for (int i = 0; i < size; i++) {
            this.fluids[i] = FluidStack.EMPTY;
            this.capacities[i] = 1000;
            this.validators[i] = fs -> true;
        }
    }

    /**
     * Sets the capacity for a specific tank.
     */
    public void setTankCapacity(int tank, int capacity) {
        validateTankIndex(tank);
        this.capacities[tank] = capacity;
    }

    /**
     * Sets the fluid validator for a specific tank.
     */
    public void setTankValidator(int tank, Predicate<FluidStack> validator) {
        validateTankIndex(tank);
        this.validators[tank] = validator;
    }

    /**
     * Directly sets the fluid in a specific tank.
     */
    public void setFluidInTank(int tank, FluidStack stack) {
        validateTankIndex(tank);
        this.fluids[tank] = stack;
        onContentsChanged(tank);
    }

    @Override
    public int getTanks() {
        return tanks;
    }

    @Override
    public @NotNull FluidStack getFluidInTank(int tank) {
        validateTankIndex(tank);
        return this.fluids[tank];
    }

    @Override
    public int getTankCapacity(int tank) {
        validateTankIndex(tank);
        return this.capacities[tank];
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        validateTankIndex(tank);
        return validators[tank].test(stack);
    }

    /**
     * Fills the first compatible tank with the given fluid resource.
     * If this is an external view, only fillable tanks are tried.
     * Otherwise, tries each tank in order.
     */
    @Override
    public int fill(@NotNull FluidStack resource, FluidAction action) {
        if (resource.isEmpty()) return 0;

        if (isExternalView()) {
            for (int tank : fillableTanks) {
                int filled = inner.fillTank(tank, resource, action);
                if (filled > 0) return filled;
            }
            return 0;
        }

        for (int i = 0; i < tanks; i++) {
            int filled = fillTank(i, resource, action);
            if (filled > 0) return filled;
        }
        return 0;
    }

    /**
     * Fills a specific tank with the given fluid resource.
     *
     * @param tank     the tank index
     * @param resource the fluid to insert
     * @param action   SIMULATE or EXECUTE
     * @return the amount of fluid that was (or would be) filled
     */
    public int fillTank(int tank, @NotNull FluidStack resource, FluidAction action) {
        if (resource.isEmpty()) return 0;
        if (!isFluidValid(tank, resource)) return 0;

        validateTankIndex(tank);

        FluidStack existing = this.fluids[tank];
        int capacity = getTankCapacity(tank);

        if (!existing.isEmpty()) {
            if (!FluidStack.isSameFluidSameComponents(resource, existing))
                return 0;

            int space = capacity - existing.getAmount();
            if (space <= 0) return 0;

            int toFill = Math.min(resource.getAmount(), space);
            if (action.execute()) {
                existing.grow(toFill);
                onContentsChanged(tank);
            }
            return toFill;
        } else {
            int toFill = Math.min(resource.getAmount(), capacity);
            if (action.execute()) {
                this.fluids[tank] = resource.copyWithAmount(toFill);
                onContentsChanged(tank);
            }
            return toFill;
        }
    }

    /**
     * Drains a specific fluid from the first matching tank.
     * If this is an external view, only drainable tanks are considered.
     */
    @Override
    public @NotNull FluidStack drain(@NotNull FluidStack resource, FluidAction action) {
        if (resource.isEmpty()) return FluidStack.EMPTY;

        if (isExternalView()) {
            for (int tank : drainableTanks) {
                FluidStack existing = inner.getFluidInTank(tank);
                if (!existing.isEmpty() && FluidStack.isSameFluidSameComponents(resource, existing)) {
                    return inner.drainTank(tank, resource.getAmount(), action);
                }
            }
            return FluidStack.EMPTY;
        }

        for (int i = 0; i < tanks; i++) {
            FluidStack existing = this.fluids[i];
            if (!existing.isEmpty() && FluidStack.isSameFluidSameComponents(resource, existing)) {
                return drainTank(i, resource.getAmount(), action);
            }
        }
        return FluidStack.EMPTY;
    }

    /**
     * Drains up to maxDrain from the first non-empty tank.
     * If this is an external view, only drainable tanks are considered.
     */
    @Override
    public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
        if (maxDrain <= 0) return FluidStack.EMPTY;

        if (isExternalView()) {
            for (int tank : drainableTanks) {
                FluidStack existing = inner.getFluidInTank(tank);
                if (!existing.isEmpty()) {
                    return inner.drainTank(tank, maxDrain, action);
                }
            }
            return FluidStack.EMPTY;
        }

        for (int i = 0; i < tanks; i++) {
            if (!this.fluids[i].isEmpty()) {
                return drainTank(i, maxDrain, action);
            }
        }
        return FluidStack.EMPTY;
    }

    public void voidTank(int tank) {
        validateTankIndex(tank);
        this.fluids[tank] = FluidStack.EMPTY;
        onContentsChanged(tank);
    }

    public @NotNull FluidStack drainTank(int tank, int maxDrain, FluidAction action) {
        if (maxDrain <= 0) return FluidStack.EMPTY;

        validateTankIndex(tank);

        FluidStack existing = this.fluids[tank];
        if (existing.isEmpty()) return FluidStack.EMPTY;

        int toDrain = Math.min(maxDrain, existing.getAmount());

        FluidStack drained = existing.copyWithAmount(toDrain);
        if (action.execute()) {
            existing.shrink(toDrain);
            if (existing.isEmpty()) {
                this.fluids[tank] = FluidStack.EMPTY;
            }
            onContentsChanged(tank);
        }
        return drained;
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        ListTag tankList = new ListTag();
        for (int i = 0; i < tanks; i++) {
            if (!fluids[i].isEmpty()) {
                CompoundTag tankTag = new CompoundTag();
                tankTag.putInt("Tank", i);
                tankTag.putInt("Capacity", capacities[i]);
                Tag fluidTag = fluids[i].save(provider);
                tankTag.put("Fluid", fluidTag);
                tankList.add(tankTag);
            }
        }
        CompoundTag nbt = new CompoundTag();
        nbt.put("Tanks", tankList);
        nbt.putInt("Size", tanks);
        return nbt;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        int size = nbt.contains("Size", Tag.TAG_INT) ? nbt.getInt("Size") : tanks;
        setSize(size);
        ListTag tankList = nbt.getList("Tanks", Tag.TAG_COMPOUND);
        for (int i = 0; i < tankList.size(); i++) {
            CompoundTag tankTag = tankList.getCompound(i);
            int tank = tankTag.getInt("Tank");
            if (tank >= 0 && tank < tanks) {
                if (tankTag.contains("Capacity", Tag.TAG_INT)) {
                    capacities[tank] = tankTag.getInt("Capacity");
                }
                // Support both new format (nested "Fluid" tag) and legacy format (inline)
                if (tankTag.contains("Fluid")) {
                    FluidStack.parse(provider, tankTag.getCompound("Fluid")).ifPresent(stack -> fluids[tank] = stack);
                } else {
                    FluidStack.parse(provider, tankTag).ifPresent(stack -> fluids[tank] = stack);
                }
            }
        }
        onLoad();
    }

    protected void validateTankIndex(int tank) {
        if (tank < 0 || tank >= tanks)
            throw new RuntimeException("Tank " + tank + " not in valid range - [0," + tanks + ")");
    }

    protected void onLoad() {}

    protected void onContentsChanged(int tank) {}
}
