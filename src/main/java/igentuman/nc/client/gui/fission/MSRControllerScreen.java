package igentuman.nc.client.gui.fission;

import igentuman.nc.client.gui.IProgressScreen;
import igentuman.nc.client.gui.IVerticalBarScreen;
import igentuman.nc.container.MSRControllerContainer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.util.TextUtils.__;

public class MSRControllerScreen extends AbstractContainerScreen<MSRControllerContainer> implements IProgressScreen, IVerticalBarScreen {

    private static final ResourceLocation GUI = rl("textures/gui/fission_controller.png");

    public MSRControllerScreen(MSRControllerContainer container, Inventory inv, Component name) {
        super(container, inv, name);
        imageWidth = 176;
        imageHeight = 166;
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderLabels(graphics, mouseX, mouseY);
        
        graphics.drawString(font, __("msr_controller"), 8, 6, 0x404040, false);
        
        String status = menu.isPowered() ? "Active" : "Inactive";
        graphics.drawString(font, __("Status: " + status), 8, 20, 0x404040, false);
        
        graphics.drawString(font, __("Energy: " + menu.getEnergyPerTick() + " FE/t"), 8, 30, 0x404040, false);
        
        graphics.drawString(font, __("Heat: " + String.format("%.1f", menu.getHeat()) + " / " + menu.getMaxHeat()), 8, 40, 0x404040, false);
        
        graphics.drawString(font, __("Efficiency: " + String.format("%.1f", menu.getEfficiency() * 100) + "%"), 8, 50, 0x404040, false);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        renderBackground(graphics);
        graphics.blit(GUI, leftPos, topPos, 0, 0, imageWidth, imageHeight);
    }

    @Override
    public double getProgress() {
        return 0;
    }

    @Override
    public double getEnergy() {
        return 0;
    }

    @Override
    public double getHeat() {
        return 0;
    }

    @Override
    public double getCoolant() {
        return 0;
    }

    @Override
    public double getHotCoolant() {
        return 0;
    }
}