package igentuman.nc.container;

import igentuman.nc.container.elements.NCSlotItemHandler;
import igentuman.nc.setup.registration.NCItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class LeacherContainer<T extends AbstractContainerMenu> extends NCProcessorContainer<T> {

    public LeacherContainer(int windowId, BlockPos pos, Inventory playerInventory, Player player, String name) {
        super(windowId, pos, playerInventory, player, name);
    }

    @Override
    protected void processorSlots() {
        addMainSlots();
        int ux = 154;
        int i = 0;

        if(getProcessor().supportEnergyUpgrade) {
            int idx = i;
            addSlot(new NCSlotItemHandler(blockEntity.upgradesHandler, idx, ux, 77)
                    .allowed(NCItems.NC_ITEMS.get("upgrade_energy").get()));
            i++;
            ux -= 18;
        }

        if(getProcessor().supportSpeedUpgrade) {
            int idx = i;
            addSlot(new NCSlotItemHandler(blockEntity.upgradesHandler, idx, ux, 77)
                    .allowed(NCItems.NC_ITEMS.get("upgrade_speed").get()));
            ux -= 18;
        }

        if(getProcessor().supportsCatalyst) {
            int[] xy = getProcessor().getSlotsConfig().getSlotPositions().get(1);
            addSlot(new NCSlotItemHandler(blockEntity.catalystHandler, 0, xy[0], xy[1]));
        }
    }

}
