package igentuman.nc.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import igentuman.nc.client.gui.element.NCGuiElement;
import igentuman.nc.client.gui.element.button.Button;
import igentuman.nc.container.StorageContainerContainer;
import igentuman.nc.container.StorageContainerItemContainer;
import igentuman.nc.util.annotation.NothingNullByDefault;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static igentuman.nc.NuclearCraft.rl;

@NothingNullByDefault
public class StorageContainerItemScreen extends AbstractContainerScreen<StorageContainerItemContainer<?>> {

    private final ResourceLocation GUI;
    private Button.Magnet magnetBtn;
    public List<NCGuiElement> widgets = new ArrayList<>();
    protected int relX;
    protected int relY;

    public StorageContainerItemScreen(StorageContainerItemContainer container, Inventory inv, Component name) {
        super(container, inv, name);
        GUI = rl("textures/gui/storage/"+container.getTier()+".png");
        imageWidth = getColls()*18+20;
        imageHeight = (getRows()+4)*18+20;
        updateRelativeCords();
    }
    protected void init() {
        super.init();
        updateRelativeCords();
        widgets.clear();
        magnetBtn = new Button.Magnet(imageWidth - 34, imageHeight - 83, this);
        // Refresh button position after relative coordinates are set
        magnetBtn.refreshPosition();
        magnetBtn.visible = true;
        magnetBtn.active = true;
        addWidget(magnetBtn);
    }

    protected void addWidget(NCGuiElement widget) {
        widget.setScreen(this);
        widgets.add(widget);
    }

    private void renderWidgets(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {

        if(magnetBtn != null) {
            magnetBtn.setEnabled(getMenu().isMagnetModeEnabled());
            magnetBtn.refreshPosition();
        }
        for(NCGuiElement widget: widgets) {
            widget.draw(graphics, mouseX, mouseY, partialTicks);
        }
    }

    protected void updateRelativeCords()
    {
        relX = (this.width - this.imageWidth) / 2;
        relY = (this.height - this.imageHeight) / 2;
        NCGuiElement.RELATIVE_X = relX;
        NCGuiElement.RELATIVE_Y = relY;
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        for(NCGuiElement widget : widgets) {
            if(widget.mouseClicked(pMouseX, pMouseY, pButton)) {
                return true;
            }
        }
        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }

    private void renderTooltips(GuiGraphics graphics, int pMouseX, int pMouseY) {
        for(NCGuiElement widget: widgets) {
            if(widget.isMouseOver(pMouseX, pMouseY)) {
                graphics.renderTooltip(font, widget.getTooltips(),
                        Optional.empty(), pMouseX, pMouseY);
            }
        }
    }

    private int getRows() {
        return menu.getRows();
    }

    private int getColls() {
        return menu.getColls();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics matrixStack, int mouseX, int mouseY) {
        renderTooltips(matrixStack, mouseX-relX, mouseY-relY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShaderTexture(0, GUI);
        updateRelativeCords();
        graphics.blit(GUI, relX, relY, 0, 0, this.imageWidth, this.imageHeight);
        renderWidgets(graphics, partialTicks, mouseX, mouseY);
    }
}