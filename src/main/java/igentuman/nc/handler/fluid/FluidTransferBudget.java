package igentuman.nc.handler.fluid;

import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.function.IntUnaryOperator;

/** Journals a fluid transfer's per-tick allowance alongside the storage participating in its transaction. */
public final class FluidTransferBudget {
    private long tick = Long.MIN_VALUE;
    private int used;
    private record State(long tick, int used) {}
    private final SnapshotJournal<State> journal = new SnapshotJournal<>() {
        @Override protected State createSnapshot() { return new State(tick, used); }
        @Override protected void revertToSnapshot(State state) {
            tick = state.tick();
            used = state.used();
        }
    };

    public boolean isInTransaction() { return journal.isInTransaction(); }

    public int transfer(long currentTick, int limit, int amount, TransactionContext transaction, IntUnaryOperator operation) {
        TransferPreconditions.checkNonNegative(amount);
        journal.updateSnapshots(transaction);
        if (tick != currentTick) {
            tick = currentTick;
            used = 0;
        }
        int allowed = Math.min(amount, Math.max(0, Math.max(0, limit) - used));
        if (allowed == 0) return 0;
        int transferred = operation.applyAsInt(allowed);
        if (transferred < 0 || transferred > allowed) throw new IllegalStateException("Invalid fluid transfer amount");
        used += transferred;
        return transferred;
    }
}
