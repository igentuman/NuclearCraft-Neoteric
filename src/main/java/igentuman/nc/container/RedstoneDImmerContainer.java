package igentuman.nc.container;

import igentuman.nc.block.entity.RedstoneDimmerBE;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.setup.registration.NCBlocks.REDSTONE_DIMMER_BLOCK;
import static igentuman.nc.setup.registration.NCBlocks.REDSTONE_DIMMER_CONTAINER;
import static igentuman.nc.util.TextUtils.__;

public class RedstoneDImmerContainer extends AbstractContainerMenu {

    protected final RedstoneDimmerBE blockEntity;
    protected final Player playerEntity;
    protected final IItemHandler playerInventory;

    public RedstoneDImmerContainer(int pContainerId, BlockPos pos, Inventory playerInventory) {
        super(REDSTONE_DIMMER_CONTAINER.get(), pContainerId);
        this.playerEntity = playerInventory.player;
        this.playerInventory =  new InvWrapper(playerInventory);
        blockEntity = (RedstoneDimmerBE) playerEntity.getCommandSenderWorld().getBlockEntity(pos);
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
                REDSTONE_DIMMER_BLOCK.get()
        );
    }

    public Component getTitle() {
        return __("block."+MODID+".redstone_dimmer");
    }

    public BlockPos getBlockPos() {
        return blockEntity.getBlockPos();
    }
}
