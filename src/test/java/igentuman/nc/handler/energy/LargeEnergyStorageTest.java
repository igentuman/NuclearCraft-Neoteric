package igentuman.nc.handler.energy;

import net.minecraft.core.HolderLookup;
import net.neoforged.neoforge.transfer.energy.EnergyHandlerUtil;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Verifies long energy persistence and atomic transfer into a nearly full receiver. */
class LargeEnergyStorageTest {
    @Test
    void roundTripPreservesAmountsAndCapacitiesAboveIntegerRange() {
        long capacity = 8_000_000_000L;
        long charge = 5_000_000_123L;
        var registries = HolderLookup.Provider.create(Stream.empty());
        var original = new LargeEnergyStorage(capacity);
        original.setEnergyStored(charge);

        var restored = new LargeEnergyStorage(1);
        restored.deserializeNBT(registries, original.serializeNBT(registries));

        assertEquals(charge, restored.getAmountAsLong());
        assertEquals(capacity, restored.getCapacityAsLong());
    }

    @Test
    void transferConservesEnergyAndNotifiesOnlyOnRootCommit() {
        long charge = 5_000_000_123L;
        var sourceChanges = new AtomicInteger();
        var receiverChanges = new AtomicInteger();
        var source = LargeEnergyStorage.create(8_000_000_000L, sourceChanges::incrementAndGet);
        var receiver = LargeEnergyStorage.create(100, receiverChanges::incrementAndGet);
        source.setEnergyStored(charge);
        receiver.setEnergyStored(90);

        try (var transaction = Transaction.openRoot()) {
            assertEquals(10, EnergyHandlerUtil.move(source, receiver, 25, transaction));
            assertEquals(charge - 10, source.getAmountAsLong());
            assertEquals(100, receiver.getAmountAsLong());
        }
        assertEquals(charge, source.getAmountAsLong());
        assertEquals(90, receiver.getAmountAsLong());
        assertEquals(0, sourceChanges.get());
        assertEquals(0, receiverChanges.get());

        try (var transaction = Transaction.openRoot()) {
            assertEquals(10, EnergyHandlerUtil.move(source, receiver, 25, transaction));
            assertEquals(0, sourceChanges.get());
            assertEquals(0, receiverChanges.get());
            transaction.commit();
        }
        assertEquals(charge - 10, source.getAmountAsLong());
        assertEquals(100, receiver.getAmountAsLong());
        assertEquals(1, sourceChanges.get());
        assertEquals(1, receiverChanges.get());
    }
}
