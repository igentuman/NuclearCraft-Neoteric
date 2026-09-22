package igentuman.nc.screen;

import igentuman.nc.container.MultiblockPortContainer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class AcceleratorPortScreen extends MultiblockPortScreen {

    public AcceleratorPortScreen(MultiblockPortContainer menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }
}
