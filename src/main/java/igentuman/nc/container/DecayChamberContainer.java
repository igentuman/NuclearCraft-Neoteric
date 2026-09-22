package igentuman.nc.container;

import igentuman.nc.block_entity.MultiblockControllerBE;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;

public class DecayChamberContainer extends MultiblockControllerContainer {

    public DecayChamberContainer(int containerId, Inventory inventory, RegistryFriendlyByteBuf data) {
        super(containerId, inventory, data);
    }

    public DecayChamberContainer(int containerId, Inventory inventory,
                                 MultiblockControllerBE blockEntity, ContainerData data) {
        super(containerId, inventory, blockEntity, data);
    }
}
