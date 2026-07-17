package igentuman.nc.client.gui.heat_exchanger;

import com.mojang.blaze3d.systems.RenderSystem;
import igentuman.nc.client.gui.element.NCGuiElement;
import igentuman.nc.client.gui.element.button.Button;
import igentuman.nc.client.gui.element.fluid.FluidTankRenderer;
import igentuman.nc.container.HeatExchangerPortContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static igentuman.nc.NuclearCraft.rl;

public class HeatExchangerPortScreen extends AbstractContainerScreen<HeatExchangerPortContainer> {
    protected final ResourceLocation GUI = rl("textures/gui/heat_exchanger/port.png");
    protected int relX;
    protected int relY;
    private Button.HeatExchangerPortRedstoneModeButton redstoneConfigBtn;
    public List<NCGuiElement> widgets = new ArrayList<>();

    public HeatExchangerPortContainer container() {
        return (HeatExchangerPortContainer) menu;
    }

    public HeatExchangerPortScreen(HeatExchangerPortContainer container, Inventory inv, Component name) {
        super(container, inv, name);
        imageWidth = 176;
        imageHeight = 176;
    }

    protected void addWidget(NCGuiElement widget) {
        widget.setScreen(this);
        widgets.add(widget);
    }

    protected void updateRelativeCords() {
        relX = (this.width - this.imageWidth) / 2;
        relY = (this.height - this.imageHeight) / 2;
        NCGuiElement.RELATIVE_X = relX;
        NCGuiElement.RELATIVE_Y = relY;
    }

    protected void init() {
        super.init();
        Minecraft mc = Minecraft.getInstance();
        updateRelativeCords();
        widgets.clear();
        redstoneConfigBtn = new Button.HeatExchangerPortRedstoneModeButton(150, 74, this, menu.getPosition());
        widgets.add(redstoneConfigBtn);
        if (container().isHotPort()) {
            addWidget(FluidTankRenderer.tank(getFluidTank(0)).id(0).size(18, 18).pos(56, 35).canVoid());
            addWidget(FluidTankRenderer.tank(getFluidTank(2)).id(2).size(24, 24).pos(112, 31).canVoid());
        } else {
            addWidget(FluidTankRenderer.tank(getFluidTank(1)).id(1).size(18, 18).pos(56, 35).canVoid());
            addWidget(FluidTankRenderer.tank(getFluidTank(3)).id(3).size(24, 24).pos(112, 31).canVoid());
        }
    }

    protected FluidTank getFluidTank(int i) {
        return menu.getFluidTank(i);
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
