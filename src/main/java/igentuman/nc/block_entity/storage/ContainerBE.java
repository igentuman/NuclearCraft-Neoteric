package igentuman.nc.block_entity.storage;

import igentuman.nc.container.StorageContainerMenu;
import igentuman.nc.content.storage.StorageDefs;
import igentuman.nc.handler.storage.UuidBackedItemHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.RangedResourceHandler;
import net.neoforged.neoforge.transfer.DelegatingResourceHandler;
import net.neoforged.neoforge.transfer.EmptyResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
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
    public ResourceHandler<ItemResource> getItemHandler(@Nullable Direction side) {
        if (side != null && getSideMode(side) == SideMode.DISABLED) return null;
        return new DelegatingResourceHandler<>(() -> isRemoved() || side != null && getSideMode(side) == SideMode.DISABLED
                ? EmptyResourceHandler.instance() : inventory);
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
            ResourceHandler<ItemResource> neighbour = level.getCapability(
                    Capabilities.Item.BLOCK, worldPosition.relative(direction), direction.getOpposite());
            if (neighbour == null) continue;

            if (mode == SideMode.OUT) {
                changed |= moveItems(inventory, neighbour);
            } else if (mode == SideMode.IN) {
                changed |= moveItems(neighbour, inventory);
            }
        }
        return changed;
    }

    private static boolean moveItems(ResourceHandler<ItemResource> from, ResourceHandler<ItemResource> to) {
        for (int i = 0; i < from.size(); i++) {
            var source = RangedResourceHandler.ofSingleIndex(from, i);
            for (int j = 0; j < to.size(); j++) {
                var target = RangedResourceHandler.ofSingleIndex(to, j);
                try (var tx = Transaction.openRoot()) {
                    if (ResourceHandlerUtil.move(source, target, resource -> true, 64, tx) > 0) {
                        tx.commit();
                        return true;
                    }
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
    protected void saveTagData(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveTagData(tag, registries);
        if (uuid != null) tag.store("uuid", UUIDUtil.CODEC, uuid);
    }

    @Override
    protected void loadTagData(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadTagData(tag, registries);
        uuid = tag.read("uuid", UUIDUtil.CODEC).orElse(null);
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
