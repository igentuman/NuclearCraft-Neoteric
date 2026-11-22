package igentuman.nc.handler.energy;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/** Long-capacity native energy storage with rollback and notifications after root commit. */
public class LargeEnergyStorage implements ILongEnergyStorage, ValueIOSerializable {
    protected long energy;
    protected long capacity;
    protected long maxReceive;
    protected long maxExtract;

    private final SnapshotJournal<Long> journal = new SnapshotJournal<>() {
        @Override protected Long createSnapshot() { return energy; }
        @Override protected void revertToSnapshot(Long snapshot) { energy = snapshot; }
        @Override protected void onRootCommit(Long original) {
            if (energy != original) onEnergyChanged();
        }
    };

    public LargeEnergyStorage(long capacity) { this(capacity, capacity, capacity, 0); }
    public LargeEnergyStorage(long capacity, long maxTransfer) { this(capacity, maxTransfer, maxTransfer, 0); }
    public LargeEnergyStorage(long capacity, long maxReceive, long maxExtract) { this(capacity, maxReceive, maxExtract, 0); }

    public LargeEnergyStorage(long capacity, long maxReceive, long maxExtract, long energy) {
        checkNonNegative(capacity);
        checkNonNegative(maxReceive);
        checkNonNegative(maxExtract);
        this.capacity = capacity;
        this.maxReceive = maxReceive;
        this.maxExtract = maxExtract;
        this.energy = Math.clamp(energy, 0, capacity);
    }

    public void setEnergyStored(long amount) {
        checkDirectMutation();
        energy = Math.clamp(amount, 0, capacity);
    }

    public void setCapacity(long capacity) {
        checkNonNegative(capacity);
        checkDirectMutation();
        this.capacity = capacity;
        energy = Math.min(energy, capacity);
    }

    private static void checkNonNegative(long amount) {
        if (amount < 0) throw new IllegalArgumentException("Negative energy amount: " + amount);
    }

    private void checkDirectMutation() {
        if (journal.isInTransaction()) throw new IllegalStateException("Cannot overwrite energy during a transfer");
    }

    public void drainEnergy(long amount) {
        checkNonNegative(amount);
        setEnergyStored(energy - Math.min(energy, amount));
    }

    public void addEnergy(long amount) {
        checkNonNegative(amount);
        setEnergyStored(energy + Math.min(capacity - energy, amount));
    }

    @Override public long getAmountAsLong() { return energy; }
    @Override public long getCapacityAsLong() { return capacity; }
    public boolean canExtract() { return maxExtract > 0; }
    public boolean canReceive() { return maxReceive > 0; }

    @Override
    public int insert(int amount, TransactionContext transaction) {
        return (int) insertLong(amount, transaction);
    }

    private long insertLong(long amount, TransactionContext transaction) {
        checkNonNegative(amount);
        long inserted = Math.min(capacity - energy, Math.min(maxReceive, amount));
        if (inserted > 0) {
            journal.updateSnapshots(transaction);
            energy += inserted;
        }
        return inserted;
    }

    @Override
    public int extract(int amount, TransactionContext transaction) {
        return (int) extractLong(amount, transaction);
    }

    private long extractLong(long amount, TransactionContext transaction) {
        checkNonNegative(amount);
        long extracted = Math.min(energy, Math.min(maxExtract, amount));
        if (extracted > 0) {
            journal.updateSnapshots(transaction);
            energy -= extracted;
        }
        return extracted;
    }

    /** Machine-internal operation; cross-handler transfers must pass a transaction to insert/extract. */
    @Override
    public long receiveEnergyL(long amount, boolean simulate) {
        try (var transaction = Transaction.openRoot()) {
            long inserted = insertLong(amount, transaction);
            if (!simulate) transaction.commit();
            return inserted;
        }
    }

    /** Machine-internal operation; cross-handler transfers must pass a transaction to insert/extract. */
    @Override
    public long extractEnergyL(long amount, boolean simulate) {
        try (var transaction = Transaction.openRoot()) {
            long extracted = extractLong(amount, transaction);
            if (!simulate) transaction.commit();
            return extracted;
        }
    }

    protected void onEnergyChanged() {}

    public static LargeEnergyStorage create(long capacity, long maxReceive, long maxExtract, Runnable onChanged) {
        return new LargeEnergyStorage(capacity, maxReceive, maxExtract) {
            @Override protected void onEnergyChanged() { onChanged.run(); }
        };
    }
    public static LargeEnergyStorage create(long capacity, long maxReceive, Runnable onChanged) {
        return create(capacity, maxReceive, maxReceive, onChanged);
    }
    public static LargeEnergyStorage create(long capacity, Runnable onChanged) {
        return create(capacity, capacity, capacity, onChanged);
    }

    @Override
    public void serialize(ValueOutput output) {
        output.putLong("energy", energy);
        output.putLong("capacity", capacity);
    }

    @Override
    public void deserialize(ValueInput input) {
        checkDirectMutation();
        long savedCapacity = input.getLongOr("capacity", capacity);
        checkNonNegative(savedCapacity);
        capacity = savedCapacity;
        energy = Math.clamp(input.getLongOr("energy", 0), 0, capacity);
    }

    /** Current-format tag bridge for owners whose other handlers still use raw tags. */
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        var output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, provider);
        serialize(output);
        return output.buildResult();
    }

    /** Current-format tag bridge; older scalar energy tags are unsupported. */
    public void deserializeNBT(HolderLookup.Provider provider, Tag tag) {
        if (!(tag instanceof CompoundTag compound)) throw new IllegalArgumentException("Expected energy compound");
        deserialize(TagValueInput.create(ProblemReporter.DISCARDING, provider, compound));
    }
}
