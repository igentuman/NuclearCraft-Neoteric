package igentuman.nc.client.gui.fission;

import com.mojang.blaze3d.systems.RenderSystem;
import igentuman.nc.client.gui.IProgressScreen;
import igentuman.nc.client.gui.IVerticalBarScreen;
import igentuman.nc.client.gui.element.NCGuiElement;
import igentuman.nc.client.gui.element.bar.VerticalBar;
import igentuman.nc.container.MSRControllerContainer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.util.TextUtils.__;
import static igentuman.nc.util.TextUtils.roundFormat;

public class MSRControllerScreen extends AbstractContainerScreen<MSRControllerContainer> implements IProgressScreen, IVerticalBarScreen {

    private static final ResourceLocation GUI = rl("textures/gui/fission/msr_controller.png");
    protected int relX;
    protected int relY;
    
    private List<NCGuiElement> widgets = new ArrayList<>();
    private VerticalBar heatBar;
    private VerticalBar pressureBar;
    private VerticalBar saltBar;
    private VerticalBar depletedBar;

    public MSRControllerScreen(MSRControllerContainer container, Inventory inv, Component name) {
        super(container, inv, name);
        imageWidth = 176;
        imageHeight = 176;
    }

    protected void updateRelativeCords()
    {
        relX = (this.width - this.imageWidth) / 2;
        relY = (this.height - this.imageHeight) / 2;
        NCGuiElement.RELATIVE_X = relX;
        NCGuiElement.RELATIVE_Y = relY;
    }

    @Override
    protected void init() {
        super.init();
        updateRelativeCords();
        widgets.clear();
        
        heatBar = new VerticalBar.Heat(8, 16, this, (int) menu.getMaxHeat());
        pressureBar = new VerticalBar(17, 16, this, (int) menu.getMaxPressure());
        pressureBar.setTooltipKey("msr.pressure.bar.amount");
        
        saltBar = new VerticalBar.Coolant(imageWidth - 16, 16, this, 50000);
        depletedBar = new VerticalBar.HotCoolant(imageWidth - 25, 16, this, 50000);
        
        widgets.add(heatBar);
        widgets.add(pressureBar);
        widgets.add(saltBar);
        widgets.add(depletedBar);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawCenteredString(font, menu.getTitle(), imageWidth / 2, titleLabelY, 0xffffff);
        
        // Render stats
        graphics.pose().pushPose();
        graphics.pose().scale(0.7f, 0.7f, 0.7f);
        int y = 30;
        graphics.drawString(font, __("msr.reactivity", roundFormat(menu.getReactivity())), 45, y, 0x00ff00);
        graphics.drawString(font, __("msr.status", menu.isCritical() ? __("msr.critical") : __("msr.subcritical")), 45, y + 12, menu.isCritical() ? 0x00ff00 : 0xff0000);
        if (menu.isLocked()) {
            graphics.drawString(font, __("msr.locked"), 45, y + 24, 0xff0000);
        }
        graphics.pose().popPose();
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderTexture(0, GUI);
        updateRelativeCords();
        renderBackground(graphics);
        graphics.blit(GUI, relX, relY, 0, 0, imageWidth, imageHeight);
        
        for (NCGuiElement widget : widgets) {
            widget.draw(graphics, mouseX, mouseY, partialTick);
        }
        
        renderBarTooltips(graphics, mouseX - relX, mouseY - relY);
    }

    private void renderBarTooltips(GuiGraphics graphics, int pMouseX, int pMouseY) {
        for (NCGuiElement widget : widgets) {
            if (widget.isMouseOver(pMouseX, pMouseY)) {
                graphics.renderTooltip(font, widget.getTooltips(), Optional.empty(), pMouseX + relX, pMouseY + relY);
            }
        }
    }
    
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    public double getProgress() {
        return menu.isPowered() ? 1.0 : 0.0;
    }

    @Override
    public double getEnergy() {
        return menu.getPressure(); // Reusing energy slot for pressure in VerticalBar logic if needed, but we use custom bars
    }

    @Override
    public double getHeat() {
        return menu.getHeat();
    }

    @Override
    public double getCoolant() {
        return menu.getSalt();
    }

    @Override
    public double getHotCoolant() {
        return menu.getDepleted();
    }
}
