package igentuman.nc.client.gui.processor;

import com.mojang.blaze3d.matrix.MatrixStack;
import igentuman.nc.block.entity.processor.LeacherBE;
import igentuman.nc.block.entity.processor.PumpBE;
import igentuman.nc.client.gui.element.NCGuiElement;
import igentuman.nc.client.gui.element.button.Checkbox;
import igentuman.nc.client.gui.element.slot.NormalSlot;
import igentuman.nc.container.NCProcessorContainer;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.inventory.Inventory;

import java.util.List;

import static igentuman.nc.block.entity.processor.LeacherBE.*;


public class LeacherScreen extends NCProcessorScreen {

    public LeacherScreen(NCProcessorContainer container, PlayerInventory inventory, ITextComponent component) {
        super(container, inventory, component);
    }

    public NCProcessorContainer getProcessorMenu() {
        return (NCProcessorContainer) menu;
    }

    public Checkbox[] pumpsCheckbox = new Checkbox[4];

    public PumpBE[] getPumps() {
        return ((LeacherBE)getProcessorMenu().getBlockEntity()).getPumpsForClient();
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
    protected void renderLabels(MatrixStack matrixStack, int mouseX, int mouseY) {
        super.renderLabels(matrixStack, mouseX, mouseY);
        switch (((LeacherBE)getProcessorMenu().getBlockEntity()).leacherState) {
            case WRONG_POSITION:
                drawString(matrixStack, font, new TranslationTextComponent("nc.label.leacher_wrong_position"), 30, 16, 0xff0000);
                break;
            case NO_SOURCE:
                drawString(matrixStack, font, new TranslationTextComponent("nc.label.leacher_no_source"), 30, 16, 0xff0000);
                break;
            case NO_ACID:
                drawString(matrixStack, font, new TranslationTextComponent("nc.label.leacher_no_acid"), 30, 16, 0xff0000);
                break;
        }
    }

    @Override
    protected void renderWidgets(MatrixStack matrix, float partialTicks, int mouseX, int mouseY) {
        for(int i = 0; i<4; i++) {

            boolean isValid = getPumps()[i] != null && getPumps()[i].isInSituValid();
            pumpsCheckbox[i].setChecked(isValid);
            pumpsCheckbox[i].setTooltipKey("leacher.tooltip."+ (isValid ? "valid" : "invalid") + "_pump");
        }
        for (NCGuiElement widget : (List<NCGuiElement>)widgets) {
            widget.draw(matrix, mouseX, mouseY, partialTicks);
        }
    }

    @Override
    protected void addOtherSlots() {
        int ux = 154;

        if(getProcessorMenu().getProcessor().supportEnergyUpgrade) {
            widgets.add(new NormalSlot(ux, 77, "energy_upgrade"));
            ux -= 18;
        }
        if(getProcessorMenu().getProcessor().supportSpeedUpgrade) {
            widgets.add(new NormalSlot(ux, 77, "speed_upgrade"));
            ux -= 18;
        }

        if(getProcessorMenu().getProcessor().supportsCatalyst) {
            int[] xy = getProcessorMenu().getProcessor().getSlotsConfig().getSlotPositions().get(1);
            widgets.add(new NormalSlot(xy[0], xy[1], "catalyst"));
        }
    }
}
