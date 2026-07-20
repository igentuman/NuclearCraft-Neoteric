package igentuman.nc.container;

import igentuman.nc.block_entity.storage.ContainerBE;
import igentuman.nc.handler.storage.UuidBackedItemHandler;
import igentuman.nc.item.ContainerBlockItem;
import igentuman.nc.setup.entries.Storage;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public class StorageContainerMenu extends AbstractContainerMenu {

    private final ContainerBE blockEntity;
    private final ContainerLevelAccess access;
    private final int containerSlots;

    public StorageContainerMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        this(containerId, playerInventory, resolve(playerInventory, buf));
    }

    private static ContainerBE resolve(Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        UUID uuid = buf.readUUID();
        ContainerBE be = (ContainerBE) playerInventory.player.level().getBlockEntity(pos);
        if (be != null && be.getUuid() == null) {
            be.setUuid(uuid);
        }
        return be;
    }

    public StorageContainerMenu(int containerId, Inventory playerInventory, ContainerBE blockEntity) {
        super(Storage.STORAGE_MENU.get(), containerId);
        this.blockEntity = blockEntity;
        this.access = ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos());

        int rows = blockEntity.getRows();
        int cols = blockEntity.getColumns();
        UuidBackedItemHandler inv = blockEntity.getInventory();
        this.containerSlots = StorageMenuSlots.layout(this::addSlot, inv, playerInventory, rows, cols);
    }

    public int getRows() {
        return blockEntity.getRows();
    }

    public int getColumns() {
        return blockEntity.getColumns();
    }

    public String getTier() {
        return blockEntity.name;
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
        return stillValid(access, player, blockEntity.getBlockState().getBlock());
    }
}
