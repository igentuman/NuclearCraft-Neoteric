package igentuman.nc.handler.storage;

import com.mojang.serialization.Codec;
import igentuman.nc.util.persistence.CurrentSavedData;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/** Owns linked Container inventories in overworld storage for every dimension. */
public class ContainerInventoryStore extends SavedData {

    public static final String NAME = "nuclearcraft_container_inventories";
    public static final SavedDataType<ContainerInventoryStore> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath("nuclearcraft", NAME), level -> new ContainerInventoryStore(),
            level -> codec(Objects.requireNonNull(level, "Container inventories require a server level").registryAccess()));

    public static Codec<ContainerInventoryStore> codec(HolderLookup.Provider provider) {
        return CompoundTag.CODEC.xmap(tag -> load(tag, provider), store -> store.save(new CompoundTag(), provider));
    }

    private final Map<UUID, StoredInventory> entries = new HashMap<>();

    public static ContainerInventoryStore get(MinecraftServer server) {
        ServerLevel overworld = server.overworld();
        return CurrentSavedData.get(overworld, TYPE);
    }

    public boolean has(UUID uuid) {
        return entries.containsKey(uuid);
    }

    public StoredInventory get(UUID uuid) {
        return entries.get(uuid);
    }

    private final SnapshotJournal<Map<UUID, StoredInventory>> creationJournal = new SnapshotJournal<>() {
        @Override protected Map<UUID, StoredInventory> createSnapshot() { return new HashMap<>(entries); }
        @Override protected void revertToSnapshot(Map<UUID, StoredInventory> snapshot) {
            entries.clear();
            entries.putAll(snapshot);
        }
        @Override protected void onRootCommit(Map<UUID, StoredInventory> original) {
            if (!entries.equals(original)) setDirty();
        }
    };

    public StoredInventory getOrCreate(UUID uuid, int size, TransactionContext transaction) {
        StoredInventory existing = entries.get(uuid);
        if (existing != null) {
            if (existing.size() != size) throw new IllegalArgumentException("Linked inventory size changed for " + uuid);
            return existing;
        }
        StoredInventory created = new StoredInventory(size, () -> markChanged(uuid));
        if (transaction != null) creationJournal.updateSnapshots(transaction);
        else if (creationJournal.isInTransaction()) throw new IllegalStateException("Cannot create inventory outside active transfer");
        entries.put(uuid, created);
        if (transaction == null) setDirty();
        return created;
    }

    public void markChanged(UUID uuid) {
        setDirty();
        ContainerSyncDispatcher.notifyChange(uuid);
    }

    public static ContainerInventoryStore load(CompoundTag tag, HolderLookup.Provider provider) {
        ContainerInventoryStore store = new ContainerInventoryStore();
        ListTag list = tag.getList("Entries").orElseThrow(() -> new IllegalArgumentException("Missing Container Entries"));
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i).orElseThrow(() -> new IllegalArgumentException("Invalid Container entry"));
            UUID uuid = entry.read("uuid", UUIDUtil.CODEC).orElseThrow(() -> new IllegalArgumentException("Invalid Container UUID"));
            CompoundTag inventory = entry.getCompound("inv").orElseThrow(() -> new IllegalArgumentException("Missing Container inventory"));
            if (store.entries.containsKey(uuid)) throw new IllegalArgumentException("Duplicate Container UUID " + uuid);
            store.entries.put(uuid, StoredInventory.load(provider, inventory, () -> store.markChanged(uuid)));
        }
        return store;
    }

    public @NotNull CompoundTag save(@NotNull CompoundTag tag, HolderLookup.Provider provider) {
        ListTag list = new ListTag();
        for (Map.Entry<UUID, StoredInventory> e : entries.entrySet()) {
            CompoundTag entry = new CompoundTag();
            entry.store("uuid", UUIDUtil.CODEC, e.getKey());
            entry.put("inv", e.getValue().save(provider));
            list.add(entry);
        }
        tag.put("Entries", list);
        return tag;
    }
}
