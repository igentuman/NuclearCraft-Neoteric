package igentuman.nc.container;

import igentuman.nc.setup.ModEntries;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

public class FissionDesignerContainer extends AbstractContainerMenu {

    protected final BlockPos pos;

    public FissionDesignerContainer(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf extraData) {
        this(containerId, playerInventory, extraData.readBlockPos());
    }

    public FissionDesignerContainer(int containerId, Inventory playerInventory, BlockPos pos) {
        super(ModEntries.get("fission_reactor_designer").menu().get(), containerId);
        this.pos = pos;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    public BlockPos getBlockPos() {
        return pos;
    }

    public BlockPos getPosition() {
        return pos;
    }
}
