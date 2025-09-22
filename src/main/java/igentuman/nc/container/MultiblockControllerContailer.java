package igentuman.nc.container;

import igentuman.nc.block.entity.MultiblockControllerBE;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;

import static igentuman.nc.setup.Registration.MULTIBLOCK_REPORT_CONTAINER;

public class MultiblockControllerContailer extends AbstractContainerMenu {
    protected final MultiblockControllerBE blockEntity;
    protected final Player playerEntity;

    public MultiblockControllerContailer(int pContainerId, BlockPos pos, Inventory playerInventory) {
        super(MULTIBLOCK_REPORT_CONTAINER.get(), pContainerId);
        this.playerEntity = playerInventory.player;
        blockEntity = (MultiblockControllerBE) playerEntity.getCommandSenderWorld().getBlockEntity(pos);
    }

    public MultiblockControllerContailer(BlockPos pos) {
        super(MULTIBLOCK_REPORT_CONTAINER.get(), 777);
        playerEntity = Minecraft.getInstance().player;
        blockEntity = (MultiblockControllerBE) playerEntity.getCommandSenderWorld().getBlockEntity(pos);
    }

    @Override
    public ItemStack quickMoveStack(Player pPlayer, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player playerIn) {
        return true;
    }

    public boolean isCasingValid() {
        return blockEntity.isCasingValid;
    }

    public int[] getDimensions() {
        return new int[]{getHeight(), getWidth(), getDepth()};
    }

    public int getDepth() {
        return blockEntity.getDepth();
    }

    public int getWidth() {
        return blockEntity.getWidth();
    }

    public int getHeight()
    {
        return blockEntity.getHeight();
    }

    public HashMap<String, String> getReportItems() {
        return blockEntity.getAnalyzeReport();
    }

    public long validationDuration() {
        return blockEntity.validationTime;
    }


    public long validationCount() {
        return blockEntity.validationsCounter;
    }

    public long ticksCount() {
        return blockEntity.multiblockTicksCounter;
    }
}
