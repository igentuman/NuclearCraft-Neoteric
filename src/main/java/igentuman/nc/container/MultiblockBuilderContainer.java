package igentuman.nc.container;

import igentuman.nc.block.entity.MultiblockBuilderBE;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.setup.registration.NCBlocks.*;
import static igentuman.nc.util.TextUtils.__;

public class MultiblockBuilderContainer extends AbstractContainerMenu {

    protected final MultiblockBuilderBE blockEntity;
    protected final Player playerEntity;
    protected final IItemHandler playerInventory;
    public int slotIndex = 0;

    public MultiblockBuilderContainer(int pContainerId, BlockPos pos, Inventory playerInventory) {
        super(MULTIBLOCK_BUILDER_CONTAINER.get(), pContainerId);
        this.playerEntity = playerInventory.player;
        this.playerInventory =  new InvWrapper(playerInventory);
        blockEntity = (MultiblockBuilderBE) playerEntity.getCommandSenderWorld().getBlockEntity(pos);
        layoutPlayerInventorySlots();
    }


    private void addSlotRange(IItemHandler handler, int x, int y, int amount, int dx) {
        for (int i = 0 ; i < amount ; i++) {
            addSlot(new SlotItemHandler(handler, slotIndex, x, y));
            x += dx;
            slotIndex++;
        }
    }

    protected void addSlotBox(IItemHandler handler, int x, int y, int horAmount, int dx, int verAmount, int dy) {
        for (int j = 0 ; j < verAmount ; j++) {
            addSlotRange(handler, x, y, horAmount, dx);
            y += dy;
        }
    }


    protected void layoutPlayerInventorySlots() {
        int leftCol = 9;
        int topRow = 217;
        addSlotRange(playerInventory, leftCol, topRow, 9, 18);
        topRow -= 58;
        addSlotBox(playerInventory, leftCol, topRow, 9, 18, 3, 18);
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player pPlayer, int pIndex) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(@NotNull Player playerIn) {
        return stillValid(
                ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos()),
                playerEntity,
                MULTIBLOCK_BUILDER_BLOCK.get()
        );
    }

    public Component getTitle() {
        return __("block."+MODID+".redstone_dimmer");
    }

    public BlockPos getBlockPos() {
        return blockEntity.getBlockPos();
    }

    public BlockPos getPosition() {
        return blockEntity.getBlockPos();
    }

    public void setBlocksMap(HashMap<BlockPos, Block> blockMap) {
        blockEntity.setBlockMap(blockMap);
    }

    public HashMap<BlockPos, Block> getBlocksMap() {
        if (blockEntity.blockMap == null) {
            blockEntity.blockMap = new HashMap<>();
        }
        return blockEntity.blockMap;
    }
}
