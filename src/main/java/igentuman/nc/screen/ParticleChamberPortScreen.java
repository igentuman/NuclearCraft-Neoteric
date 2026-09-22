package igentuman.nc.screen;

import igentuman.nc.container.MultiblockPortContainer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class ParticleChamberPortScreen extends MultiblockPortScreen {

    public ParticleChamberPortScreen(MultiblockPortContainer menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }
}
