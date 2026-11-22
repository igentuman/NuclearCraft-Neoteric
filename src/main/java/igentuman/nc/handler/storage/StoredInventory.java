package igentuman.nc.handler.storage;

import igentuman.nc.handler.item.CustomInventoryHandler;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

/** Shared linked-Container storage with one transactional journal for every alias of its UUID. */
public class StoredInventory extends CustomInventoryHandler {
    private final Runnable onChanged;

    public StoredInventory(int size) { this(size, () -> {}); }
    public StoredInventory(int size, Runnable onChanged) {
        super(size);
        this.onChanged = onChanged;
    }

    public ItemStack get(int slot) { return getStackInSlot(slot); }
    public void set(int slot, ItemStack stack) { setStackInSlot(slot, stack); }
    public void read(HolderLookup.Provider provider, CompoundTag tag) { deserializeNBT(provider, tag); }
    public CompoundTag save(HolderLookup.Provider provider) { return serializeNBT(provider); }

    @Override protected void onContentsChanged(int slot) { onChanged.run(); }

    public static StoredInventory load(HolderLookup.Provider provider, CompoundTag tag, Runnable onChanged) {
        int size = tag.getInt("Size").filter(value -> value >= 0)
                .orElseThrow(() -> new IllegalArgumentException("Invalid Container inventory Size"));
        StoredInventory inventory = new StoredInventory(size, onChanged);
        inventory.read(provider, tag);
        return inventory;
    }
}
