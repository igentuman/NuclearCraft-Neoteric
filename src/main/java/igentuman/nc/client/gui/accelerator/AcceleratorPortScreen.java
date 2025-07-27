package igentuman.nc.client.gui.accelerator;

import com.mojang.blaze3d.systems.RenderSystem;
import igentuman.nc.client.gui.IProgressScreen;
import igentuman.nc.client.gui.IVerticalBarScreen;
import igentuman.nc.client.gui.element.NCGuiElement;
import igentuman.nc.client.gui.element.bar.ProgressBar;
import igentuman.nc.client.gui.element.bar.VerticalBar;
import igentuman.nc.client.gui.element.button.Button;
import igentuman.nc.client.gui.element.fluid.FluidTankRenderer;
import igentuman.nc.container.AcceleratorPortContainer;
import igentuman.nc.container.TurbinePortContainer;
import net.minecraft.ChatFormatting;
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
import static igentuman.nc.block.entity.NuclearCraftBE.isGTEUCapEnabled;
import static igentuman.nc.util.ModUtil.isGtLoaded;
import static igentuman.nc.util.TextUtils.*;

public class AcceleratorPortScreen extends AbstractContainerScreen<AcceleratorPortContainer> implements IProgressScreen, IVerticalBarScreen {
    protected final ResourceLocation GUI = rl("textures/gui/accelerators/accelerator_source.png");
    protected int relX;
    protected int relY;
    private int xCenter;
    private Button.TurbinePortRedstoneModeButton redstoneConfigBtn;

    public AcceleratorPortContainer container()
    {
        return (AcceleratorPortContainer)menu;
    }

    public List<NCGuiElement> widgets = new ArrayList<>();

    private VerticalBar energyBar;

    public AcceleratorPortScreen(AcceleratorPortContainer container, Inventory inv, Component name) {
        super(container, inv, name);
        imageWidth = 176;
        imageHeight = 176;
    }

    protected void addWidget(NCGuiElement widget)
    {
        widget.setScreen(this);
        widgets.add(widget);
    }

    protected void updateRelativeCords()
    {
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
        //redstoneConfigBtn = new Button.TurbinePortRedstoneModeButton(150, 54, this, menu.getPosition());
        //widgets.add(redstoneConfigBtn);
        energyBar = new VerticalBar.Energy(7, 6,  this, container().getMaxEnergy());
        addWidget(FluidTankRenderer.tank(getFluidTank(0)).id(0).size(16, 16).pos(80, 43).canVoid());
    }

    protected FluidTank getFluidTank(int i) {
        return menu.getFluidTank(i);
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        xCenter = getGuiLeft()-imageWidth/2;
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    private void renderWidgets(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        //redstoneConfigBtn.setMode(getMenu().getComparatorMode());
        //redstoneConfigBtn.strength = getMenu().getAnalogSignalStrength();
        for(NCGuiElement widget: widgets) {
            widget.draw(graphics, mouseX, mouseY, partialTicks);
        }
        if(energyBar != null) {
            energyBar.draw(graphics, mouseX, mouseY, partialTicks);
        }
    }

    @Override
    protected void renderLabels(@NotNull GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawCenteredString(font,  menu.getTitle(), imageWidth/2, titleLabelY, 0xffffff);
        renderTooltips(graphics, mouseX-relX, mouseY-relY);
    }

    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        for(NCGuiElement widget : widgets) {
            if(widget.mouseClicked(pMouseX, pMouseY, pButton)) {
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

        for(NCGuiElement widget: widgets) {
           if(widget.isMouseOver(pMouseX, pMouseY)) {
               graphics.renderTooltip(font, widget.getTooltips(),
                       Optional.empty(), pMouseX, pMouseY);
           }
        }

        if(energyBar.isMouseOver(pMouseX, pMouseY)) {
            energyBar.clearTooltips();
            if(isGtLoaded() && isGTEUCapEnabled()) {
                energyBar.addTooltip(applyFormat(__("tooltip.eu.per_tick", scaledFormat(menu.getEnergyRequired())), ChatFormatting.YELLOW));
                energyBar.addTooltip(applyFormat(__("tooltip.eu.tier", menu.getTier()), ChatFormatting.YELLOW));
            } else {
                energyBar.addTooltip(applyFormat(__("tooltip.nc.energy.per_tick", scaledFormat(container().getEnergyRequired())).withStyle(ChatFormatting.AQUA)));
            }
            graphics.renderTooltip(font, energyBar.getTooltips(),
                    Optional.empty(), pMouseX, pMouseY);
        }

    }

    @Override
    public double getProgress() {
        return 0;
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

    public int getAnalogSignalStrength()
    {
        return container().getAnalogSignalStrength();
    }
}
