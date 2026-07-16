package igentuman.nc.client.storage;

import igentuman.nc.handler.storage.StoredInventory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ClientContainerInventory {

    private static final Map<UUID, StoredInventory> CACHE = new HashMap<>();

    public static StoredInventory get(UUID uuid) {
        return CACHE.get(uuid);
    }

    public static StoredInventory getOrCreate(UUID uuid, int size) {
        return CACHE.computeIfAbsent(uuid, k -> new StoredInventory(size));
    }

    public static void put(UUID uuid, StoredInventory inv) {
        if (inv == null) {
            CACHE.remove(uuid);
        } else {
            CACHE.put(uuid, inv);
        }
    }

    public static void clear() {
        CACHE.clear();
    }
}
