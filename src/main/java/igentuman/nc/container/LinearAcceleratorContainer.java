package igentuman.nc.container;

import igentuman.nc.block_entity.MultiblockControllerBE;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;

public class LinearAcceleratorContainer extends MultiblockControllerContainer {

    public LinearAcceleratorContainer(int containerId, Inventory inventory, RegistryFriendlyByteBuf data) {
        super(containerId, inventory, data);
    }

    public LinearAcceleratorContainer(int containerId, Inventory inventory,
                                      MultiblockControllerBE blockEntity, ContainerData data) {
        super(containerId, inventory, blockEntity, data);
    }

}
