package igentuman.nc.container;

import igentuman.nc.block_entity.MultiblockBuilderBE;
import igentuman.nc.setup.ModEntries;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import java.util.HashMap;

public class MultiblockBuilderContainer extends AbstractContainerMenu {

    protected final BlockPos pos;
    protected final MultiblockBuilderBE blockEntity;

    public MultiblockBuilderContainer(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf extraData) {
        this(containerId, playerInventory, extraData.readBlockPos());
    }

    public MultiblockBuilderContainer(int containerId, Inventory playerInventory, BlockPos pos) {
        super(ModEntries.get("multiblock_builder").menu().get(), containerId);
        this.pos = pos;
        var be = playerInventory.player.level().getBlockEntity(pos);
        this.blockEntity = be instanceof MultiblockBuilderBE builder ? builder : null;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    public BlockPos getPosition() {
        return pos;
    }

    public HashMap<BlockPos, Block> getBlocksMap() {
        if (blockEntity == null) return new HashMap<>();
        if (blockEntity.blockMap == null) {
            blockEntity.blockMap = new HashMap<>();
        }
        return blockEntity.blockMap;
    }

    public void setBlocksMap(HashMap<BlockPos, Block> blockMap) {
        if (blockEntity != null) {
            blockEntity.blockMap = blockMap;
        }
    }
}
