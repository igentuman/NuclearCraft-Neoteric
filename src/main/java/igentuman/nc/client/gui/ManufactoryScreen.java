package igentuman.nc.client.gui;

import igentuman.nc.container.NCProcessorContainer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class ManufactoryScreen <CONTAINER extends NCProcessorContainer> extends NCProcessorScreen<CONTAINER> {

    public ManufactoryScreen(CONTAINER container, Inventory inv, Component name) {
        super(container, inv, name);
    }

    public ManufactoryScreen(AbstractContainerMenu abstractContainerMenu, Inventory inventory, Component component) {
        super((CONTAINER) abstractContainerMenu, inventory, component);
    }

}
