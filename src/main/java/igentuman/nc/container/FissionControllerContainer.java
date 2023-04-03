package igentuman.nc.container;

import igentuman.nc.block.entity.fission.FissionControllerBE;
import igentuman.nc.setup.multiblocks.FissionReactor;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.energy.IEnergyStorage;

import static igentuman.nc.NuclearCraft.MODID;

public class FissionControllerContainer extends AbstractContainerMenu {
    protected FissionControllerBE blockEntity;
    protected Player playerEntity;

    protected String name = "fission_reactor_controller";
    public FissionControllerContainer(int pContainerId, BlockPos pos, Inventory playerInventory) {
        super(FissionReactor.FISSION_CONTROLLER_CONTAINER.get(), pContainerId);
        this.playerEntity = playerInventory.player;
        blockEntity = (FissionControllerBE) playerEntity.getCommandSenderWorld().getBlockEntity(pos);
    }

    @Override
    public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
        return null;
    }

    @Override
    public boolean stillValid(Player playerIn) {
        return stillValid(
                ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos()),
                playerEntity,
                FissionReactor.MULTI_BLOCKS.get(name).get()
        );
    }

    public Component getTitle() {
        return Component.translatable("block."+MODID+"."+name);
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

    public boolean isInteriorValid() {
        return blockEntity.isInternalValid;
    }

    public BlockPos getValidationResultData() {
        return  blockEntity.validationResult.errorBlock;
    }

    public String getValidationResultKey() {
        return  blockEntity.validationResult.messageKey;
    }

    public int getEnergy() {
        return blockEntity.energy;
    }

    public int getHeat() {
        return blockEntity.heat;
    }

    public int getProgress() {
        return blockEntity.getDepletionProgress();
    }
}
