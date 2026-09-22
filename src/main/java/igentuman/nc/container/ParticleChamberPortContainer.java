package igentuman.nc.container;

import igentuman.nc.block_entity.MultiblockPortBE;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;

public class ParticleChamberPortContainer extends MultiblockPortContainer {

    public ParticleChamberPortContainer(int containerId, Inventory inventory, RegistryFriendlyByteBuf data) {
        super(containerId, inventory, data);
    }

    public ParticleChamberPortContainer(int containerId, Inventory inventory,
                                        MultiblockPortBE blockEntity, ContainerData data) {
        super(containerId, inventory, blockEntity, data);
    }
}
