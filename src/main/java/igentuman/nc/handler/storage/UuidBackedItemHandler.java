package igentuman.nc.handler.storage;

import igentuman.nc.client.storage.ClientContainerInventory;
import igentuman.nc.item.ContainerBlockItem;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.util.thread.EffectiveSide;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * {@link IItemHandlerModifiable} view over a UUID-keyed {@link StoredInventory}. It owns no stacks:
 * every call resolves the backing inventory lazily. Which store it resolves is chosen per call by the
 * current thread's logical side — the server store on the server thread, the client cache on the client
 * thread — so a single handler instance works for an item or block that exists on both sides.
 */
public class UuidBackedItemHandler implements IItemHandlerModifiable {

    private final int size;
    private final int slotLimit;
    private final ItemStack itemStack; // non-null ⇒ item-backed (may self-assign UUID on the server)
    private final Supplier<UUID> uuidSupplier;

    /** Item-backed: UUID is read from (and assigned into) the stack's NBT. */
    public UuidBackedItemHandler(ItemStack stack, int size) {
        this.itemStack = stack;
        this.size = size;
        this.slotLimit = 64;
        this.uuidSupplier = () -> ContainerBlockItem.readUuid(stack);
    }

    /** Block-backed / general: UUID is supplied by the owner (e.g. the block entity). */
    public UuidBackedItemHandler(int size, Supplier<UUID> uuidSupplier) {
        this.itemStack = null;
        this.size = size;
        this.slotLimit = 64;
        this.uuidSupplier = uuidSupplier;
    }

    private static boolean isClient() {
        return EffectiveSide.get().isClient();
    }

    private StoredInventory resolve(boolean create) {
        UUID uuid = uuidSupplier.get();
        if (isClient()) {
            if (uuid == null) return null;
            return create ? ClientContainerInventory.getOrCreate(uuid, size) : ClientContainerInventory.get(uuid);
        }
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return null;
        if (uuid == null) {
            if (!create || itemStack == null) return null;
            uuid = ContainerBlockItem.assignUuid(itemStack);
        }
        return create ? ContainerInventoryStore.get(server).getOrCreate(uuid, size)
                : ContainerInventoryStore.get(server).get(uuid);
    }

    /** Marks the backing store dirty and pushes to viewers. Call after out-of-band mutations (e.g. vanilla slot grows). */
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
            if (!ItemHandlerHelper.canItemStacksStack(stack, existing)) return stack;
            limit -= existing.getCount();
        }
        if (limit <= 0) return stack;

        boolean reached = stack.getCount() > limit;
        if (!simulate) {
            if (existing.isEmpty()) {
                inv.set(slot, reached ? ItemHandlerHelper.copyStackWithSize(stack, limit) : stack);
            } else {
                existing.grow(reached ? limit : stack.getCount());
            }
            markChanged();
        }
        return reached ? ItemHandlerHelper.copyStackWithSize(stack, stack.getCount() - limit) : ItemStack.EMPTY;
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
            inv.set(slot, ItemHandlerHelper.copyStackWithSize(existing, existing.getCount() - toExtract));
            markChanged();
        }
        return ItemHandlerHelper.copyStackWithSize(existing, toExtract);
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
