package igentuman.nc.container;

import igentuman.nc.block_entity.MultiblockControllerBE;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;

public class RingAcceleratorContainer extends MultiblockControllerContainer {

    public RingAcceleratorContainer(int containerId, Inventory inventory, RegistryFriendlyByteBuf data) {
        super(containerId, inventory, data);
    }

    public RingAcceleratorContainer(int containerId, Inventory inventory,
                                    MultiblockControllerBE blockEntity, ContainerData data) {
        super(containerId, inventory, blockEntity, data);
    }

    @Override
    protected void layoutPlayerSlots(Inventory playerInventory) {
    }
}
