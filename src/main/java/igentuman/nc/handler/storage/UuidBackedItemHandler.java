package igentuman.nc.handler.storage;

import igentuman.nc.client.storage.ClientContainerInventory;
import igentuman.nc.item.ContainerBlockItem;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.util.thread.EffectiveSide;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.UUID;
import java.util.function.Supplier;

/** Native linked inventory view resolving the current UUID and shared server/client store on every operation. */
public class UuidBackedItemHandler extends LinkedInventoryHandler {
    private final Supplier<UUID> uuidSupplier;

    public UuidBackedItemHandler(int size, Supplier<UUID> uuidSupplier) {
        super(size);
        this.uuidSupplier = uuidSupplier;
    }

    @Override protected StoredInventory resolve(boolean create, TransactionContext transaction) {
        UUID uuid = uuidSupplier.get();
        if (uuid == null) return null;
        if (EffectiveSide.get().isClient()) {
            // Client menus only write synchronization copies; automation runs against the authoritative server store.
            return create ? ClientContainerInventory.getOrCreate(uuid, size(), transaction) : ClientContainerInventory.get(uuid);
        }
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return null;
        ContainerInventoryStore store = ContainerInventoryStore.get(server);
        return create ? store.getOrCreate(uuid, size(), transaction) : store.get(uuid);
    }

    @Override public boolean isItemValid(int index, ItemStack stack) {
        return super.isItemValid(index, stack) && !(stack.getItem() instanceof ContainerBlockItem);
    }
}
