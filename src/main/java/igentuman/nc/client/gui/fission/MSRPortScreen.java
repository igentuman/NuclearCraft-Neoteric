package igentuman.nc.client.gui.fission;

import com.mojang.blaze3d.systems.RenderSystem;
import igentuman.nc.client.gui.element.NCGuiElement;
import igentuman.nc.client.gui.element.button.Button;
import igentuman.nc.container.MSRPortContainer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static igentuman.nc.NuclearCraft.rl;

public class MSRPortScreen extends AbstractContainerScreen<MSRPortContainer> {
    protected final ResourceLocation GUI = rl("textures/gui/fission/port.png");
    protected int relX;
    protected int relY;
    private Button.MSRPortRedstoneModeButton redstoneConfigBtn;
    public List<NCGuiElement> widgets = new ArrayList<>();

    public MSRPortContainer container() {
        return (MSRPortContainer) menu;
    }

    public MSRPortScreen(MSRPortContainer container, Inventory inv, Component name) {
        super(container, inv, name);
        imageWidth = 176;
        imageHeight = 176;
    }

    protected void updateRelativeCords() {
        relX = (this.width - this.imageWidth) / 2;
        relY = (this.height - this.imageHeight) / 2;
        NCGuiElement.RELATIVE_X = relX;
        NCGuiElement.RELATIVE_Y = relY;
    }

    protected void init() {
        super.init();
        updateRelativeCords();
        widgets.clear();
        redstoneConfigBtn = new Button.MSRPortRedstoneModeButton(150, 74, this, menu.getPosition());
        widgets.add(redstoneConfigBtn);
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    private void renderWidgets(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        redstoneConfigBtn.setMode(getMenu().getComparatorMode());
        redstoneConfigBtn.strength = getMenu().getAnalogSignalStrength();
        for (NCGuiElement widget : widgets) {
            widget.draw(graphics, mouseX, mouseY, partialTicks);
        }
    }

    @Override
    protected void renderLabels(@NotNull GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawCenteredString(font, menu.getTitle(), imageWidth / 2, titleLabelY, 0xffffff);
        renderTooltips(graphics, mouseX - relX, mouseY - relY);
    }

    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        for (NCGuiElement widget : widgets) {
            if (widget.mouseClicked(pMouseX, pMouseY, pButton)) {
                return true;
            }
        }
        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShaderTexture(0, GUI);
        updateRelativeCords();
        graphics.blit(GUI, relX, relY, 0, 0, this.imageWidth, this.imageHeight);
        renderWidgets(graphics, partialTicks, mouseX, mouseY);
    }

    private void renderTooltips(GuiGraphics graphics, int pMouseX, int pMouseY) {
        for (NCGuiElement widget : widgets) {
            if (widget.isMouseOver(pMouseX, pMouseY)) {
                graphics.renderTooltip(font, widget.getTooltips(),
                        Optional.empty(), pMouseX, pMouseY);
            }
        }
    }
}
