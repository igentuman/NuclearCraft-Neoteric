package igentuman.nc.block_entity.storage;

import igentuman.nc.container.StorageContainerMenu;
import igentuman.nc.content.storage.StorageDefs;
import igentuman.nc.handler.storage.UuidBackedItemHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class ContainerBE extends AbstractStorageBE implements MenuProvider {

    private UUID uuid;
    private final UuidBackedItemHandler inventory;

    public ContainerBE(BlockEntityType<?> type, BlockPos pos, BlockState state, String name) {
        super(type, pos, state, name);
        this.inventory = new UuidBackedItemHandler(StorageDefs.containerSize(name), () -> uuid);
    }

    public UuidBackedItemHandler getInventory() {
        return inventory;
    }

    public int getRows() {
        return StorageDefs.containerRows(name);
    }

    public int getColumns() {
        return StorageDefs.containerColumns(name);
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID assignUuidIfAbsent() {
        if (uuid == null) {
            uuid = UUID.randomUUID();
            setChanged();
        }
        return uuid;
    }

    @Nullable
    @Override
    public IItemHandler getItemHandler(@Nullable Direction side) {
        if (side != null && getSideMode(side) == SideMode.DISABLED) return null;
        return inventory;
    }

    @Override
    public void serverTick() {
        assignUuidIfAbsent();
        super.serverTick();
    }

    @Override
    protected boolean transfer() {
        if (level == null) return false;
        boolean changed = false;
        for (Direction direction : Direction.values()) {
            SideMode mode = getSideMode(direction);
            if (mode == SideMode.DEFAULT || mode == SideMode.DISABLED) continue;
            IItemHandler neighbour = level.getCapability(
                    Capabilities.ItemHandler.BLOCK, worldPosition.relative(direction), direction.getOpposite());
            if (neighbour == null) continue;

            if (mode == SideMode.OUT) {
                changed |= moveItems(inventory, neighbour);
            } else if (mode == SideMode.IN) {
                changed |= moveItems(neighbour, inventory);
            }
        }
        return changed;
    }

    private static boolean moveItems(IItemHandler from, IItemHandler to) {
        for (int i = 0; i < from.getSlots(); i++) {
            ItemStack stack = from.extractItem(i, 64, true);
            if (stack.isEmpty()) continue;
            for (int j = 0; j < to.getSlots(); j++) {
                ItemStack leftover = to.insertItem(j, stack, true);
                int moved = stack.getCount() - leftover.getCount();
                if (moved > 0) {
                    ItemStack extracted = from.extractItem(i, moved, false);
                    to.insertItem(j, extracted, false);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public int getComparatorSignal() {
        int slots = inventory.getSlots();
        if (slots == 0) return 0;
        double fill = 0;
        boolean any = false;
        for (int i = 0; i < slots; i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (stack.isEmpty()) continue;
            any = true;
            fill += (stack.getCount() / (double) stack.getMaxStackSize()) / slots;
        }
        return any ? Math.max(1, (int) (fill * 15)) : 0;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (uuid != null) tag.putUUID("uuid", uuid);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.hasUUID("uuid")) uuid = tag.getUUID("uuid");
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.nuclearcraft." + name);
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new StorageContainerMenu(containerId, playerInventory, this);
    }
}
