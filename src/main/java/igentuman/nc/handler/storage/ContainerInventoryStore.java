package igentuman.nc.handler.storage;

import net.minecraft.core.HolderLookup;
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

public class ContainerInventoryStore extends SavedData {

    public static final String NAME = "nuclearcraft_container_inventories";

    private final Map<UUID, StoredInventory> entries = new HashMap<>();

    public static ContainerInventoryStore get(MinecraftServer server) {
        ServerLevel overworld = server.overworld();
        return overworld.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(ContainerInventoryStore::new, ContainerInventoryStore::load),
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

    public static ContainerInventoryStore load(CompoundTag tag, HolderLookup.Provider provider) {
        ContainerInventoryStore store = new ContainerInventoryStore();
        ListTag list = tag.getList("Entries", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            if (!entry.hasUUID("uuid")) continue;
            UUID uuid = entry.getUUID("uuid");
            store.entries.put(uuid, StoredInventory.load(provider, entry.getCompound("inv"), 0));
        }
        return store;
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag, HolderLookup.Provider provider) {
        ListTag list = new ListTag();
        for (Map.Entry<UUID, StoredInventory> e : entries.entrySet()) {
            CompoundTag entry = new CompoundTag();
            entry.putUUID("uuid", e.getKey());
            entry.put("inv", e.getValue().save(provider));
            list.add(entry);
        }
        tag.put("Entries", list);
        return tag;
    }
}
