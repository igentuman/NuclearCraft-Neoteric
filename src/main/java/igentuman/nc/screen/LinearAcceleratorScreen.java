package igentuman.nc.screen;

import igentuman.nc.block_entity.accelerator.LinearAcceleratorControllerBE;
import igentuman.nc.container.MultiblockControllerContainer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class LinearAcceleratorScreen extends AbstractAcceleratorControllerScreen {

    public LinearAcceleratorScreen(MultiblockControllerContainer menu, Inventory inventory, Component title) {
        super(menu, inventory, title, false);
    }

    @Override
    protected int coolantInputTank() {
        return LinearAcceleratorControllerBE.TANK_COOLANT_IN;
    }
}
