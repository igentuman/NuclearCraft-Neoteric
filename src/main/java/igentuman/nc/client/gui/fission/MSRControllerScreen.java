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

import java.util.Optional;

import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.util.TextUtils.__;

public class MSRControllerScreen extends AbstractContainerScreen<MSRControllerContainer> implements IProgressScreen, IVerticalBarScreen {

    private static final ResourceLocation GUI = rl("textures/gui/fission/msr_controller.png");
    protected int relX;
    protected int relY;
    
    private VerticalBar heatBar;

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
        heatBar = new VerticalBar.Heat(8, 16, this, (int) menu.getMaxHeat());
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawCenteredString(font, menu.getTitle(), imageWidth / 2, titleLabelY, 0xffffff);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderTexture(0, GUI);
        updateRelativeCords();
        renderBackground(graphics);
        graphics.blit(GUI, relX, relY, 0, 0, imageWidth, imageHeight);
        
        // Draw heat bar
        heatBar.draw(graphics, mouseX - relX, mouseY - relY, partialTick);
        
        // Handle bar tooltips
        renderBarTooltips(graphics, mouseX - relX, mouseY - relY);
    }

    public MSRControllerContainer container()
    {
        return menu;
    }

    private void renderBarTooltips(GuiGraphics graphics, int pMouseX, int pMouseY) {
        if (heatBar.isMouseOver(pMouseX, pMouseY)) {
            heatBar.clearTooltips();
            heatBar.addTooltip(__("reactor.heating", container().getHeating()).withStyle(ChatFormatting.RED));
            graphics.renderTooltip(font, heatBar.getTooltips(), Optional.empty(), pMouseX+relX, pMouseY+relY);
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
        return 0.0; // MSR only produces steam, no energy storage
    }

    @Override
    public double getHeat() {
        return menu.getHeat() / menu.getMaxHeat();
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