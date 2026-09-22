package igentuman.nc.container;

import igentuman.nc.block_entity.GlobalBlockEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;

public class CreativeParticleSourceContainer extends UniversalProcessorContainer {

    public CreativeParticleSourceContainer(int containerId, Inventory inventory, RegistryFriendlyByteBuf data) {
        super(containerId, inventory, data);
    }

    public CreativeParticleSourceContainer(int containerId, Inventory inventory,
                                           GlobalBlockEntity blockEntity, ContainerData data) {
        super(containerId, inventory, blockEntity, data);
    }
}
