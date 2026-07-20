package igentuman.nc.container;

import igentuman.nc.content.storage.StorageDefs;
import igentuman.nc.handler.storage.UuidBackedItemHandler;
import igentuman.nc.item.ContainerBlockItem;
import igentuman.nc.setup.entries.Storage;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public class StorageContainerItemMenu extends AbstractContainerMenu {

    public static final int MAGNET_BUTTON = 0;

    private final int heldSlot;
    private final UUID uuid;
    private final String tier;
    private final int rows;
    private final int cols;
    private final int containerSlots;

    public StorageContainerItemMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        this(containerId, playerInventory, buf.readVarInt(), buf.readUUID(), buf.readUtf());
    }

    public StorageContainerItemMenu(int containerId, Inventory playerInventory, int heldSlot, UUID uuid, String tier) {
        super(Storage.STORAGE_ITEM_MENU.get(), containerId);
        this.heldSlot = heldSlot;
        this.uuid = uuid;
        this.tier = tier;
        this.rows = StorageDefs.containerRows(tier);
        this.cols = StorageDefs.containerColumns(tier);

        UuidBackedItemHandler inv = new UuidBackedItemHandler(rows * cols, () -> uuid);
        this.containerSlots = StorageMenuSlots.layout(this::addSlot, inv, playerInventory, rows, cols);
    }

    public int getRows() {
        return rows;
    }

    public int getColumns() {
        return cols;
    }

    public String getTier() {
        return tier;
    }

    public boolean isMagnetEnabled(Player player) {
        ItemStack stack = held(player);
        return stack.getItem() instanceof ContainerBlockItem cbi && cbi.isMagnetEnabled(stack);
    }

    private ItemStack held(Player player) {
        return heldSlot == 40 ? player.getOffhandItem() : player.getInventory().getItem(heldSlot);
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id == MAGNET_BUTTON) {
            ItemStack stack = held(player);
            if (stack.getItem() instanceof ContainerBlockItem cbi) {
                cbi.toggleMagnet(stack);
                return true;
            }
        }
        return false;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;

        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();

        if (index < containerSlots) {
            if (!moveItemStackTo(stack, containerSlots, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else {
            if (stack.getItem() instanceof ContainerBlockItem) return ItemStack.EMPTY;
            if (!moveItemStackTo(stack, 0, containerSlots, false)) {
                return ItemStack.EMPTY;
            }
        }

        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return original;
    }

    @Override
    public boolean stillValid(Player player) {
        ItemStack stack = held(player);
        return stack.getItem() instanceof ContainerBlockItem
                && uuid != null && uuid.equals(ContainerBlockItem.readUuid(stack));
    }
}
