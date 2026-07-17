package igentuman.nc.handler.storage;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Global, server-authoritative store of container contents keyed by UUID, anchored in the overworld's
 * data storage. Item/block NBT holds only the UUID; the real stacks live here. UUIDs are globally unique,
 * so one store backs every container regardless of the dimension it currently sits in. Whole-store
 * (de)serialization is used for <em>disk</em> only — never the network. Per-entry sync goes through
 * {@link ContainerSyncDispatcher}.
 */
public class ContainerInventoryStore extends SavedData {

    public static final String NAME = "nc_container_inventories";

    private final Map<UUID, StoredInventory> entries = new HashMap<>();

    /** Resolves the single global store, anchored in the overworld's data storage. */
    public static ContainerInventoryStore get(MinecraftServer server) {
        ServerLevel overworld = server.overworld();
        return overworld.getDataStorage().computeIfAbsent(
                ContainerInventoryStore::load,
                ContainerInventoryStore::new,
                NAME);
    }

    public boolean has(UUID uuid) {
        return entries.containsKey(uuid);
    }

    public StoredInventory get(UUID uuid) {
        return entries.get(uuid);
    }

    public StoredInventory getOrCreate(UUID uuid, int size) {
        return entries.computeIfAbsent(uuid, k -> {
            setDirty();
            return new StoredInventory(size);
        });
    }

    public void markChanged(UUID uuid) {
        setDirty();
        ContainerSyncDispatcher.notifyChange(uuid);
    }

    /** Persist to disk only (used by migration, which never has viewers yet). */
    public void markDirtyOnly() {
        setDirty();
    }

    public static ContainerInventoryStore load(CompoundTag tag) {
        ContainerInventoryStore store = new ContainerInventoryStore();
        ListTag list = tag.getList("Entries", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            if (!entry.hasUUID("uuid")) continue;
            UUID uuid = entry.getUUID("uuid");
            store.entries.put(uuid, StoredInventory.load(entry.getCompound("inv"), 0));
        }
        return store;
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag) {
        ListTag list = new ListTag();
        for (Map.Entry<UUID, StoredInventory> e : entries.entrySet()) {
            CompoundTag entry = new CompoundTag();
            entry.putUUID("uuid", e.getKey());
            entry.put("inv", e.getValue().save());
            list.add(entry);
        }
        tag.put("Entries", list);
        return tag;
    }
}
