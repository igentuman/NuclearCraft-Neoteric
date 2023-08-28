package igentuman.nc.client.gui.processor;

import igentuman.nc.client.gui.element.slot.NormalSlot;
import igentuman.nc.container.NCProcessorContainer;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;


public class LeacherScreen<T extends NCProcessorContainer> extends NCProcessorScreen<T>{

    public LeacherScreen(AbstractContainerMenu abstractContainerMenu, Inventory inventory, Component component) {
        super(abstractContainerMenu, inventory, component);
    }

    @Override
    protected void addOtherSlots() {
        int ux = 154;

        if(menu.getProcessor().supportEnergyUpgrade) {
            widgets.add(new NormalSlot(ux, 77, "energy_upgrade"));
            ux -= 18;
        }
        if(menu.getProcessor().supportSpeedUpgrade) {
            widgets.add(new NormalSlot(ux, 77, "speed_upgrade"));
            ux -= 18;
        }

        if(menu.getProcessor().supportsCatalyst) {
            int[] xy = menu.getProcessor().getSlotsConfig().getSlotPositions().get(1);
            widgets.add(new NormalSlot(xy[0], xy[1], "catalyst"));
        }
    }
}
