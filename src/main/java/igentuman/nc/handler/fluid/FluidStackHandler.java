package igentuman.nc.handler.fluid;

import com.mojang.serialization.Codec;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/** Native multi-tank storage with transactional contents, restricted views and current-format Value I/O. */
public class FluidStackHandler implements ResourceHandler<FluidResource>, ValueIOSerializable {
    public record SavedTank(int tank, int capacity, FluidStack fluid) {
        public static final Codec<SavedTank> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.intRange(0, Integer.MAX_VALUE).fieldOf("Tank").forGetter(SavedTank::tank),
                Codec.intRange(0, Integer.MAX_VALUE).fieldOf("Capacity").forGetter(SavedTank::capacity),
                FluidStack.CODEC.optionalFieldOf("Fluid", FluidStack.EMPTY).forGetter(SavedTank::fluid)
        ).apply(instance, SavedTank::new));
    }

    // ValueInput may return a codec's partial result. Never turn malformed fluid into an empty tank.
    public static final Codec<List<SavedTank>> TANKS_CODEC = SavedTank.CODEC.listOf().mapResult(new Codec.ResultFunction<>() {
        private <T> DataResult<T> complete(DataResult<T> result) {
            return result.error().<DataResult<T>>map(error -> DataResult.error(error::message)).orElse(result);
        }
        @Override public <T> DataResult<Pair<List<SavedTank>, T>> apply(DynamicOps<T> ops, T input,
                                                                       DataResult<Pair<List<SavedTank>, T>> result) {
            return complete(result);
        }
        @Override public <T> DataResult<T> coApply(DynamicOps<T> ops, List<SavedTank> input, DataResult<T> result) {
            return complete(result);
        }
    });

    private FluidStack[] fluids;
    private int[] capacities;
    private Predicate<FluidStack>[] validators;
    private List<TankJournal> journals;
    private boolean fixedCapacities;

    public FluidStackHandler(int tanks, int[] capacities) {
        if (tanks < 0 || capacities.length != tanks) throw new IllegalArgumentException("Invalid tank capacities");
        for (int capacity : capacities) checkCapacity(capacity);
        this.capacities = capacities.clone();
        initialize(tanks);
    }

    public FluidStackHandler(int tanks, int defaultCapacity) {
        if (tanks < 0) throw new IllegalArgumentException("Negative tank count");
        checkCapacity(defaultCapacity);
        capacities = new int[tanks];
        Arrays.fill(capacities, defaultCapacity);
        initialize(tanks);
    }

    @SuppressWarnings("unchecked")
    private void initialize(int size) {
        fluids = new FluidStack[size];
        Arrays.fill(fluids, FluidStack.EMPTY);
        validators = new Predicate[size];
        Arrays.fill(validators, (Predicate<FluidStack>) stack -> true);
        journals = new ArrayList<>(size);
        for (int tank = 0; tank < size; tank++) journals.add(new TankJournal(tank));
    }

    /** Resizing clears contents, retaining configured capacities and validators for existing indices. */
    public void setSize(int size) {
        checkDirectMutation();
        if (size < 0) throw new IllegalArgumentException("Negative tank count");
        int[] previousCapacities = capacities;
        Predicate<FluidStack>[] previousValidators = validators;
        capacities = new int[size];
        initialize(size);
        for (int tank = 0; tank < size; tank++) {
            capacities[tank] = tank < previousCapacities.length ? previousCapacities[tank] : 1000;
            if (tank < previousValidators.length) validators[tank] = previousValidators[tank];
        }
    }

    public void setFixedCapacities() {
        checkDirectMutation();
        fixedCapacities = true;
    }

    /** Capacity changes retain existing fluid, including when the configured capacity becomes smaller. */
    public void setTankCapacity(int tank, int capacity) {
        checkDirectMutation();
        validateTankIndex(tank);
        checkCapacity(capacity);
        capacities[tank] = capacity;
    }

    public void setTankValidator(int tank, Predicate<FluidStack> validator) {
        checkDirectMutation();
        validateTankIndex(tank);
        validators[tank] = Objects.requireNonNull(validator);
    }

    /** Machine-internal overwrite; external transfers must use insert/extract with the caller's transaction. */
    public void setFluidInTank(int tank, FluidStack stack) {
        checkDirectMutation();
        validateTankIndex(tank);
        FluidStack previous = fluids[tank];
        fluids[tank] = stack.copy();
        if (!sameContents(previous, fluids[tank])) onContentsChanged(tank);
    }

    public void voidTank(int tank) { setFluidInTank(tank, FluidStack.EMPTY); }
    public int getTanks() { return size(); }
    public FluidStack getFluidInTank(int tank) {
        validateTankIndex(tank);
        return fluids[tank].copy();
    }
    public int getTankCapacity(int tank) {
        validateTankIndex(tank);
        return capacities[tank];
    }
    public boolean isFluidValid(int tank, FluidStack stack) {
        validateTankIndex(tank);
        return validators[tank].test(stack.copy());
    }

    @Override public int size() { return fluids.length; }
    @Override public FluidResource getResource(int index) {
        validateTankIndex(index);
        return FluidResource.of(fluids[index]);
    }
    @Override public long getAmountAsLong(int index) {
        validateTankIndex(index);
        return fluids[index].getAmount();
    }
    @Override public long getCapacityAsLong(int index, FluidResource resource) {
        return resource.isEmpty() || isValid(index, resource) ? getTankCapacity(index) : 0;
    }
    @Override public boolean isValid(int index, FluidResource resource) {
        return isFluidValid(index, resource.toStack(1));
    }

    @Override
    public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
        validateTankIndex(index);
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        FluidStack current = fluids[index];
        if (!isValid(index, resource) || (!current.isEmpty() && !resource.matches(current))) return 0;
        int inserted = Math.min(amount, Math.max(0, capacities[index] - current.getAmount()));
        if (inserted > 0) {
            journals.get(index).updateSnapshots(transaction);
            fluids[index] = resource.toStack(current.getAmount() + inserted);
        }
        return inserted;
    }

    @Override
    public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) {
        validateTankIndex(index);
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        if (!resource.matches(fluids[index])) return 0;
        int extracted = Math.min(amount, fluids[index].getAmount());
        if (extracted > 0) {
            journals.get(index).updateSnapshots(transaction);
            fluids[index] = resource.toStack(fluids[index].getAmount() - extracted);
        }
        return extracted;
    }

    /** All tanks stay visible; both indexed and whole-handler transfers obey these restrictions. */
    public ResourceHandler<FluidResource> createExternalView(int[] fillableTanks, int[] drainableTanks) {
        int[] fillable = fillableTanks.clone();
        int[] drainable = drainableTanks.clone();
        for (int tank : fillable) validateTankIndex(tank);
        for (int tank : drainable) validateTankIndex(tank);
        return new ResourceHandler<>() {
            private boolean allows(int[] indices, int index) {
                return Arrays.stream(indices).anyMatch(tank -> tank == index);
            }
            @Override public int size() { return FluidStackHandler.this.size(); }
            @Override public FluidResource getResource(int index) { return FluidStackHandler.this.getResource(index); }
            @Override public long getAmountAsLong(int index) { return FluidStackHandler.this.getAmountAsLong(index); }
            @Override public long getCapacityAsLong(int index, FluidResource resource) {
                validateTankIndex(index);
                return resource.isEmpty() || isValid(index, resource) ? getTankCapacity(index) : 0;
            }
            @Override public boolean isValid(int index, FluidResource resource) {
                validateTankIndex(index);
                return allows(fillable, index) && FluidStackHandler.this.isValid(index, resource);
            }
            @Override public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
                validateTankIndex(index);
                TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
                return allows(fillable, index) ? FluidStackHandler.this.insert(index, resource, amount, transaction) : 0;
            }
            @Override public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) {
                validateTankIndex(index);
                TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
                return allows(drainable, index) ? FluidStackHandler.this.extract(index, resource, amount, transaction) : 0;
            }
        };
    }

    @Override
    public void serialize(ValueOutput output) {
        List<SavedTank> saved = new ArrayList<>(size());
        for (int tank = 0; tank < size(); tank++) saved.add(new SavedTank(tank, capacities[tank], fluids[tank]));
        output.putInt("Size", size());
        output.store("Tanks", TANKS_CODEC, saved);
    }

    @Override
    public void deserialize(ValueInput input) {
        checkDirectMutation();
        int size = input.getIntOr("Size", -1);
        List<SavedTank> saved = input.read("Tanks", TANKS_CODEC)
                .orElseThrow(() -> new IllegalArgumentException("Invalid current-format fluid tanks"));
        validateSavedTanks(size, saved).getOrThrow();
        if (fixedCapacities && size != size()) throw new IllegalArgumentException("Fixed tank count changed");
        if (size != size()) setSize(size);
        // Validate the entire payload before changing any live content; validators describe insertion, not loading.
        for (SavedTank tank : saved) {
            if (!fixedCapacities) capacities[tank.tank()] = tank.capacity();
            fluids[tank.tank()] = tank.fluid().copy();
        }
        onLoad();
    }

    public static DataResult<List<SavedTank>> validateSavedTanks(int size, List<SavedTank> saved) {
        if (size < 0 || saved.size() != size) return DataResult.error(() -> "Fluid tank count mismatch");
        boolean[] seen = new boolean[size];
        for (SavedTank tank : saved) {
            if (tank.tank() < 0 || tank.tank() >= size || seen[tank.tank()] || tank.capacity() < 0) {
                return DataResult.error(() -> "Invalid or duplicate fluid tank index/capacity");
            }
            seen[tank.tank()] = true;
        }
        return DataResult.success(saved);
    }

    /** Current-format bridge for owners whose other persistence fields still use raw tags. */
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        var output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, provider);
        serialize(output);
        return output.buildResult();
    }
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        deserialize(TagValueInput.create(ProblemReporter.DISCARDING, provider, tag));
    }

    protected void validateTankIndex(int tank) { Objects.checkIndex(tank, size()); }
    protected void onLoad() {}
    protected void onContentsChanged(int tank) {}

    private static void checkCapacity(int capacity) {
        if (capacity < 0) throw new IllegalArgumentException("Negative tank capacity");
    }
    private void checkDirectMutation() {
        if (journals.stream().anyMatch(SnapshotJournal::isInTransaction)) {
            throw new IllegalStateException("Cannot overwrite fluid storage during a transfer");
        }
    }
    private static boolean sameContents(FluidStack first, FluidStack second) {
        return first.getAmount() == second.getAmount()
                && (first.isEmpty() && second.isEmpty() || FluidStack.isSameFluidSameComponents(first, second));
    }

    private class TankJournal extends SnapshotJournal<FluidStack> {
        private final int tank;
        private TankJournal(int tank) { this.tank = tank; }
        @Override protected FluidStack createSnapshot() { return fluids[tank].copy(); }
        @Override protected void revertToSnapshot(FluidStack snapshot) { fluids[tank] = snapshot; }
        @Override protected void onRootCommit(FluidStack original) {
            if (!sameContents(original, fluids[tank])) onContentsChanged(tank);
        }
    }
}
