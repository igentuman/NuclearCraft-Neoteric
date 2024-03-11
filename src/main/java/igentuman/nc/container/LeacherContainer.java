package igentuman.nc.container;

import igentuman.nc.container.elements.NCSlotItemHandler;
import igentuman.nc.setup.registration.NCItems;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.entity.player.PlayerEntity;

public class LeacherContainer extends NCProcessorContainer<LeacherContainer> {

    public LeacherContainer(int windowId, BlockPos pos, PlayerInventory playerInventory, PlayerEntity player, String name) {
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
