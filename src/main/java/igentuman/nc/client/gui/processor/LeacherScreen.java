package igentuman.nc.client.gui.processor;

import com.mojang.blaze3d.vertex.PoseStack;
import igentuman.nc.block.entity.processor.LeacherBE;
import igentuman.nc.block.entity.processor.PumpBE;
import igentuman.nc.client.gui.element.NCGuiElement;
import igentuman.nc.client.gui.element.button.Checkbox;
import igentuman.nc.client.gui.element.slot.NormalSlot;
import igentuman.nc.container.NCProcessorContainer;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;


public class LeacherScreen<T extends NCProcessorContainer> extends NCProcessorScreen<T>{

    public LeacherScreen(AbstractContainerMenu abstractContainerMenu, Inventory inventory, Component component) {
        super(abstractContainerMenu, inventory, component);
    }

    public Checkbox[] pumpsCheckbox = new Checkbox[4];

    public PumpBE[] getPumps() {
        return ((LeacherBE)menu.getBlockEntity()).getPumpsForClient();
    }
    @Override
    protected void init() {
        super.init();
        for(int i = 0; i<4; i++) {
            pumpsCheckbox[i] = new Checkbox(68+i*13, 81, this, false);
            widgets.add(pumpsCheckbox[i]);
        }
    }

    @Override
    protected void renderWidgets(PoseStack matrix, float partialTicks, int mouseX, int mouseY) {
        for(int i = 0; i<4; i++) {

            boolean isValid = getPumps()[i] != null && getPumps()[i].isInSituValid();
            pumpsCheckbox[i].setChecked(isValid);
            pumpsCheckbox[i].setTooltipKey("leacher.tooltip."+ (isValid ? "valid" : "invalid") + "_pump");
        }
        for (NCGuiElement widget : widgets) {
            widget.draw(matrix, mouseX, mouseY, partialTicks);
        }
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
