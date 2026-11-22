package igentuman.nc.client.storage;

import igentuman.nc.handler.storage.StoredInventory;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ClientContainerInventory {

    private static final Map<UUID, StoredInventory> CACHE = new HashMap<>();

    public static StoredInventory get(UUID uuid) {
        return CACHE.get(uuid);
    }

    private static final SnapshotJournal<Map<UUID, StoredInventory>> CREATION_JOURNAL = new SnapshotJournal<>() {
        @Override protected Map<UUID, StoredInventory> createSnapshot() { return new HashMap<>(CACHE); }
        @Override protected void revertToSnapshot(Map<UUID, StoredInventory> snapshot) {
            CACHE.clear();
            CACHE.putAll(snapshot);
        }
    };

    public static StoredInventory getOrCreate(UUID uuid, int size, TransactionContext transaction) {
        StoredInventory existing = CACHE.get(uuid);
        if (existing != null) {
            if (existing.size() != size) throw new IllegalArgumentException("Linked client inventory size changed");
            return existing;
        }
        StoredInventory created = new StoredInventory(size);
        if (transaction != null) CREATION_JOURNAL.updateSnapshots(transaction);
        else checkDirectMutation();
        CACHE.put(uuid, created);
        return created;
    }

    private static void checkDirectMutation() {
        if (CREATION_JOURNAL.isInTransaction()) throw new IllegalStateException("Cannot replace client inventory cache during transfer");
        CACHE.values().forEach(StoredInventory::checkDirectMutation);
    }

    public static void put(UUID uuid, StoredInventory inv) {
        checkDirectMutation();
        if (inv == null) {
            CACHE.remove(uuid);
        } else {
            CACHE.put(uuid, inv);
        }
    }

    public static void clear() {
        checkDirectMutation();
        CACHE.clear();
    }
}
