package igentuman.nc.client.gui.accelerator;

import com.mojang.blaze3d.systems.RenderSystem;
import igentuman.nc.client.gui.IProgressScreen;
import igentuman.nc.client.gui.IVerticalBarScreen;
import igentuman.nc.client.gui.element.GuiParticle;
import igentuman.nc.client.gui.element.NCGuiElement;
import igentuman.nc.client.gui.element.bar.ProgressBar;
import igentuman.nc.client.gui.element.bar.VerticalBar;
import igentuman.nc.client.gui.element.button.Button;
import igentuman.nc.client.gui.element.button.Checkbox;
import igentuman.nc.client.gui.element.fluid.FluidTankRenderer;
import igentuman.nc.container.TargetChamberControllerContainer;
import igentuman.nc.container.TargetChamberPortContainer;
import igentuman.nc.content.particles.ParticleStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.fluids.capability.templates.FluidTank;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.util.TextUtils.__;
import static igentuman.nc.util.TextUtils.applyFormat;

public class TargetChamberPortScreen extends AbstractContainerScreen<TargetChamberPortContainer> implements IVerticalBarScreen {
    protected final ResourceLocation GUI = rl("textures/gui/processor.png");
    protected int relX;
    protected int relY;
    private int xCenter;

    public TargetChamberPortContainer container()
    {
        return menu;
    }

    public List<NCGuiElement> widgets = new ArrayList<>();
    private VerticalBar energyBar;
    private Button.TargetChamberPortRedstoneModeButton modeBtn;

    public TargetChamberPortScreen(TargetChamberPortContainer container, Inventory inv, Component name) {
        super(container, inv, name);
        imageWidth = 176;
        imageHeight = 200;
    }

    protected void updateRelativeCords()
    {
        relX = (this.width - this.imageWidth) / 2;
        relY = (this.height - this.imageHeight) / 2;
        NCGuiElement.RELATIVE_X = relX;
        NCGuiElement.RELATIVE_Y = relY;
    }

    protected void addWidget(NCGuiElement widget)
    {
        widget.setScreen(this);
        widgets.add(widget);
    }

    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        for(NCGuiElement widget : widgets) {
            if(widget.mouseClicked(pMouseX, pMouseY, pButton)) {
                return true;
            }
        }
        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }

    protected void init() {
        super.init();
        updateRelativeCords();
        widgets.clear();
        energyBar = new VerticalBar.Energy(7, 16,  this, container().getMaxEnergy());
        modeBtn = new Button.TargetChamberPortRedstoneModeButton(150, 64, this, menu.getPosition());
        widgets.add(modeBtn);
        if (hasFluidTanks()) {
            addWidget(FluidTankRenderer.tank(getFluidTank(0)).id(0).size(16, 16).pos(51, 55).canVoid());
            addWidget(FluidTankRenderer.tank(getFluidTank(1)).id(1).size(16, 16).pos(111, 55).canVoid());
        }
    }

    private boolean hasFluidTanks() {
        return menu.hasFluidTanks();
    }

    protected FluidTank getFluidTank(int i) {
        return menu.getFluidTank(i);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        xCenter = getGuiLeft()-imageWidth/2;
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    private void renderWidgets(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        modeBtn.setMode(container().getComparatorMode());
        modeBtn.strength = container().getAnalogSignalStrength();
        for(NCGuiElement widget: widgets) {
            widget.draw(graphics, mouseX, mouseY, partialTicks);
        }
        energyBar.draw(graphics, mouseX, mouseY, partialTicks);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawCenteredString(font,  menu.getTitle(), imageWidth/2, 5, 0xffffff);
        renderTooltips(graphics, mouseX-relX, mouseY-relY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShaderTexture(0, GUI);
        updateRelativeCords();
        graphics.blit(GUI, relX, relY, 0, 0, this.imageWidth, this.imageHeight);
        renderWidgets(graphics, partialTicks, mouseX, mouseY);
    }

    private void renderTooltips(GuiGraphics graphics, int pMouseX, int pMouseY) {
        for(NCGuiElement widget: widgets) {
            if(widget.isMouseOver(pMouseX, pMouseY)) {
                graphics.renderTooltip(font, widget.getTooltips(),
                        Optional.empty(), pMouseX, pMouseY);
            }
        }

        energyBar.clearTooltips();
        energyBar.addTooltip(__("tooltip.nc.energy.per_tick", container().energyPerTick()));
        if(energyBar.isMouseOver(pMouseX, pMouseY+10)) {
            graphics.renderTooltip(font, energyBar.getTooltips(),
                    Optional.empty(), pMouseX, pMouseY);
        }
    }

    @Override
    public double getEnergy() {
        return container().getEnergy();
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
