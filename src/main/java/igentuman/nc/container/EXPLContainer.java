package igentuman.nc.container;

import igentuman.nc.block_entity.kugelblitz.EXPLBE;
import igentuman.nc.setup.ModEntries;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;

/** Menu for the EXPL laser emitter: exposes charge, burst target, and readiness, no item slots. */
public class EXPLContainer extends AbstractContainerMenu {

    private static final int DATA_SLOT_COUNT = 5;

    private final EXPLBE blockEntity;
    private final ContainerLevelAccess access;
    private final ContainerData data;

    public EXPLContainer(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf extraData) {
        this(containerId, playerInventory,
                (EXPLBE) playerInventory.player.level().getBlockEntity(extraData.readBlockPos()),
                new SimpleContainerData(DATA_SLOT_COUNT));
    }

    public EXPLContainer(int containerId, Inventory playerInventory, EXPLBE blockEntity, ContainerData data) {
        super(ModEntries.get("expl").menu().get(), containerId);
        this.blockEntity = blockEntity;
        this.access = ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos());
        this.data = data;
        addDataSlots(data);
    }

    public long getCharge() {
        return (data.get(0) & 0xFFFFFFFFL) | ((long) data.get(1) << 32);
    }

    public boolean isReady() {
        return data.get(2) != 0;
    }

    public long getTargetCharge() {
        return (data.get(3) & 0xFFFFFFFFL) | ((long) data.get(4) << 32);
    }

    public BlockPos getPosition() {
        return blockEntity.getBlockPos();
    }

    public EXPLBE getBlockEntity() {
        return blockEntity;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, ModEntries.get("expl").block().get());
    }
}
