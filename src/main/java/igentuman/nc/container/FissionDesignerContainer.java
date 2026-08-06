package igentuman.nc.container;

import igentuman.nc.setup.registration.FissionDesignerRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.util.TextUtils.__;

public class FissionDesignerContainer extends AbstractContainerMenu {

    protected final BlockPos pos;
    protected final Player playerEntity;

    public FissionDesignerContainer(int pContainerId, BlockPos pos, Inventory playerInventory) {
        super(FissionDesignerRegistration.FISSION_DESIGNER_CONTAINER.get(), pContainerId);
        this.pos = pos;
        this.playerEntity = playerInventory.player;
    }

    @Override
    public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player playerIn) {
        return true;
    }

    public Component getTitle() {
        return __("block." + MODID + ".fission_reactor_designer");
    }

    public BlockPos getBlockPos() {
        return pos;
    }

    public BlockPos getPosition() {
        return pos;
    }
}
