package igentuman.nc.container;

import igentuman.nc.block_entity.MultiblockControllerBE;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.Slot;

public class TargetChamberContainer extends MultiblockControllerContainer {

    public TargetChamberContainer(int containerId, Inventory inventory, RegistryFriendlyByteBuf data) {
        super(containerId, inventory, data);
    }

    public TargetChamberContainer(int containerId, Inventory inventory,
                                  MultiblockControllerBE blockEntity, ContainerData data) {
        super(containerId, inventory, blockEntity, data);
    }

    protected void layoutPlayerSlots(Inventory playerInventory) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 118 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, 8 + col * 18, 176));
        }
    }
}
