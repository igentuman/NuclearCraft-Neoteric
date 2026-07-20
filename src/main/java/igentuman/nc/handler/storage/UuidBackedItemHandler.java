package igentuman.nc.handler.storage;

import igentuman.nc.client.storage.ClientContainerInventory;
import igentuman.nc.item.ContainerBlockItem;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.util.thread.EffectiveSide;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.function.Supplier;

public class UuidBackedItemHandler implements IItemHandlerModifiable {

    private final int size;
    private final int slotLimit = 64;
    private final Supplier<UUID> uuidSupplier;

    public UuidBackedItemHandler(int size, Supplier<UUID> uuidSupplier) {
        this.size = size;
        this.uuidSupplier = uuidSupplier;
    }

    private static boolean isClient() {
        return EffectiveSide.get().isClient();
    }

    private StoredInventory resolve(boolean create) {
        UUID uuid = uuidSupplier.get();
        if (uuid == null) return null;
        if (isClient()) {
            return create ? ClientContainerInventory.getOrCreate(uuid, size) : ClientContainerInventory.get(uuid);
        }
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return null;
        return create ? ContainerInventoryStore.get(server).getOrCreate(uuid, size)
                : ContainerInventoryStore.get(server).get(uuid);
    }

    public void contentsChanged() {
        markChanged();
    }

    private void markChanged() {
        if (isClient()) return;
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;
        UUID uuid = uuidSupplier.get();
        if (uuid == null) return;
        ContainerInventoryStore.get(server).markChanged(uuid);
    }

    @Override
    public int getSlots() {
        return size;
    }

    @Override
    public @NotNull ItemStack getStackInSlot(int slot) {
        StoredInventory inv = resolve(false);
        return inv == null ? ItemStack.EMPTY : inv.get(slot);
    }

    @Override
    public void setStackInSlot(int slot, @NotNull ItemStack stack) {
        if (!stack.isEmpty() && !isItemValid(slot, stack)) return;
        StoredInventory inv = resolve(true);
        if (inv == null) return;
        inv.set(slot, stack);
        markChanged();
    }

    @Override
    public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        if (stack.isEmpty()) return ItemStack.EMPTY;
        if (!isItemValid(slot, stack)) return stack;
        StoredInventory inv = resolve(true);
        if (inv == null || slot < 0 || slot >= inv.size()) return stack;

        ItemStack existing = inv.get(slot);
        int limit = slotLimit;
        if (!existing.isEmpty()) {
            if (!ItemStack.isSameItemSameComponents(stack, existing)) return stack;
            limit -= existing.getCount();
        }
        if (limit <= 0) return stack;

        boolean reached = stack.getCount() > limit;
        if (!simulate) {
            if (existing.isEmpty()) {
                inv.set(slot, reached ? stack.copyWithCount(limit) : stack);
            } else {
                existing.grow(reached ? limit : stack.getCount());
            }
            markChanged();
        }
        return reached ? stack.copyWithCount(stack.getCount() - limit) : ItemStack.EMPTY;
    }

    @Override
    public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (amount == 0) return ItemStack.EMPTY;
        StoredInventory inv = resolve(false);
        if (inv == null || slot < 0 || slot >= inv.size()) return ItemStack.EMPTY;

        ItemStack existing = inv.get(slot);
        if (existing.isEmpty()) return ItemStack.EMPTY;

        int toExtract = Math.min(amount, existing.getCount());
        if (existing.getCount() <= toExtract) {
            if (!simulate) {
                inv.set(slot, ItemStack.EMPTY);
                markChanged();
                return existing;
            }
            return existing.copy();
        }
        if (!simulate) {
            inv.set(slot, existing.copyWithCount(existing.getCount() - toExtract));
            markChanged();
        }
        return existing.copyWithCount(toExtract);
    }

    @Override
    public int getSlotLimit(int slot) {
        return slotLimit;
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return !(stack.getItem() instanceof ContainerBlockItem);
    }
}
