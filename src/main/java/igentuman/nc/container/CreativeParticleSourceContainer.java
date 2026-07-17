package igentuman.nc.container;

import igentuman.nc.container.elements.NCSlotItemHandler;
import igentuman.nc.setup.registration.NCItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class CreativeParticleSourceContainer<T extends AbstractContainerMenu> extends NCProcessorContainer<T> {

    public CreativeParticleSourceContainer(int windowId, BlockPos pos, Inventory playerInventory, Player player, String name) {
        super(windowId, pos, playerInventory, player, name);
    }

    @Override
    protected void processorSlots() {
        addMainSlots();
    }

}
