package igentuman.nc.client.gui.accelerator;

import com.mojang.blaze3d.systems.RenderSystem;
import igentuman.nc.client.gui.IProgressScreen;
import igentuman.nc.client.gui.IVerticalBarScreen;
import igentuman.nc.client.gui.element.GuiParticle;
import igentuman.nc.client.gui.element.NCGuiElement;
import igentuman.nc.client.gui.element.bar.VerticalBar;
import igentuman.nc.client.gui.element.button.Button;
import igentuman.nc.client.gui.element.button.Checkbox;
import igentuman.nc.client.gui.element.fluid.FluidTankRenderer;
import igentuman.nc.container.LinearAcceleratorContainer;
import igentuman.nc.content.particles.ParticleStack;
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
import static igentuman.nc.client.gui.element.fluid.FluidTankRenderer.TooltipMode.SHOW_AMOUNT_AND_CAPACITY;
import static igentuman.nc.content.particles.ParticleStack.getParticleStack;
import static igentuman.nc.util.ModUtil.isGtLoaded;
import static igentuman.nc.util.TextUtils.*;

public class LinearAcceleratorControllerScreen extends AbstractContainerScreen<LinearAcceleratorContainer> implements IProgressScreen, IVerticalBarScreen {
    protected final ResourceLocation GUI = rl("textures/gui/accelerators/linear_controller.png");
    protected int relX;
    protected int relY;
    private int xCenter;

    public LinearAcceleratorContainer container()
    {
        return menu;
    }

    public List<NCGuiElement> widgets = new ArrayList<>();
    public Checkbox checkboxCasing;
    public Checkbox checkboxInterior;
    private VerticalBar energyBar;
    private VerticalBar heatBar;
    private VerticalBar coolantBar;
    public Component casingTootip = Component.empty();
    public Component interiorTootip = Component.empty();
    public GuiParticle guiParticle;
    private Button.MultiblockAnalyze analyzeBtn;

    public LinearAcceleratorControllerScreen(LinearAcceleratorContainer container, Inventory inv, Component name) {
        super(container, inv, name);
        imageWidth = 196;
        imageHeight = 109;
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
        checkboxCasing = new Checkbox(imageWidth-19, 80, this,  isCasingValid());
        checkboxInterior =  new Checkbox(imageWidth-32, 80, this,  isInteriorValid());
        //widgets.add(new ProgressBar(74, 35, this,  7));
        energyBar = new VerticalBar.Energy(7, 20,  this, container().getMaxEnergy());
        heatBar = new VerticalBar.Heat(17, 20,  this, container().getMaxHeat());
        coolantBar = new VerticalBar.Coolant(27, 20,  this, container().maxCoolant());
        guiParticle = new GuiParticle(40, 21);
        analyzeBtn = new Button.MultiblockAnalyze(170, 60, this, menu.getPosition());
        widgets.add(analyzeBtn);
        widgets.add(new FluidTankRenderer(getFluidTank(2), SHOW_AMOUNT_AND_CAPACITY,6, 73, 28, 21));
    }

    protected void addWidget(NCGuiElement widget)
    {
        widget.setScreen(this);
        widgets.add(widget);
    }

    protected FluidTank getFluidTank(int i) {
        return menu.getFluidTank(i);
    }

    private boolean isInteriorValid() {
        return  container().isInteriorValid();
    }

    private boolean isCasingValid() {
        return  container().isCasingValid();
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        xCenter = getGuiLeft()-imageWidth/2;
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    private void renderWidgets(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        for(NCGuiElement widget: widgets) {
            widget.draw(graphics, mouseX, mouseY, partialTicks);
        }
        checkboxCasing.setChecked(isCasingValid()).draw(graphics, mouseX, mouseY, partialTicks);
        if(isCasingValid()) {
            checkboxCasing.setTooltipKey("multiblock.casing.complete");
        } else {
            checkboxCasing.setTooltipKey("multiblock.casing.incomplete");
        }
        checkboxCasing.addTooltip(casingTootip);

        checkboxInterior.setChecked(isInteriorValid() && isCasingValid()).draw(graphics, mouseX, mouseY, partialTicks);
        if(isInteriorValid() && isCasingValid()) {
            checkboxInterior.setTooltipKey("multiblock.interior.complete");
        } else {
            checkboxInterior.setTooltipKey("multiblock.interior.incomplete");
        }
        checkboxInterior.addTooltip(interiorTootip);
        if(isInteriorValid() && isCasingValid()) {
            checkboxInterior.addTooltip(__("tooltip.nc.accelerator.focus", container().getFocus()));
            checkboxInterior.addTooltip(__("tooltip.nc.accelerator.quadroupoles", container().getQuadroupoles()));
            checkboxInterior.addTooltip(__("tooltip.nc.accelerator.dipoles", container().getDipoles()));
            checkboxInterior.addTooltip(__("tooltip.nc.accelerator.voltage", container().getVoltage()));
            checkboxInterior.addTooltip(__("tooltip.nc.accelerator.amplifiers", container().getAmplifiers()));
            checkboxInterior.addTooltip(__("tooltip.nc.accelerator.coolers", container().getCoolers()));
        }
        energyBar.draw(graphics, mouseX, mouseY, partialTicks);
        heatBar.draw(graphics, mouseX, mouseY, partialTicks);
        coolantBar.draw(graphics, mouseX, mouseY, partialTicks);
        if(hasParticle()) {
            guiParticle.drawParticleStack(graphics, getParticleStack());
        }
    }

    @Override
    protected void renderLabels(@NotNull GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawCenteredString(font,  menu.getTitle(), imageWidth/2, titleLabelY, 0xffffff);
        if(isCasingValid()) {
            casingTootip = applyFormat(__("tooltip.nc.structure.size", getMultiblockHeight(), getMultiblockWidth(), getMultiblockDepth()), ChatFormatting.GOLD);
        } else {
            casingTootip = applyFormat(__(getValidationResultKey(), getValidationResultData()), ChatFormatting.RED);
        }

        if(isCasingValid()) {
            if (isInteriorValid()) {
                 graphics.drawString(font, __("tooltip.nc.accelerator.voltage", container().getVoltage()), 37, 50, 0xffffff);
                 graphics.drawString(font, __("tooltip.nc.accelerator.efficiency", numberFormat(container().getEfficiency())+"%"), 37, 60, 0xffffff);
                 graphics.drawString(font, __("tooltip.nc.accelerator.strength", numberFormat(container().getStrength())), 37, 70, 0xffffff);
            } else {
                interiorTootip = applyFormat(__(getValidationResultKey(), getValidationResultData()), ChatFormatting.RED);
            }
        }

        renderTooltips(graphics, mouseX-relX, mouseY-relY);
    }

    private Object getValidationResultData() {
        return container().getValidationResultData().toShortString();
    }

    private String getValidationResultKey() {
        return container().getValidationResultKey();
    }

    private int getMultiblockHeight() {
        return container().getHeight();
    }

    private int getMultiblockWidth() {
        return container().getWidth();
    }

    private int getMultiblockDepth() {
        return container().getDepth();
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShaderTexture(0, GUI);
        updateRelativeCords();
        graphics.blit(GUI, relX, relY, 0, 0, this.imageWidth, this.imageHeight);
        renderWidgets(graphics, partialTicks, mouseX, mouseY);
    }

    private boolean hasParticle() {
        return container().hasParticle();
    }

    private ParticleStack getParticleStack() {
        return container().getParticleStack();
    }

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
        if(guiParticle.isMouseOver(pMouseX, pMouseY)) {
            if(hasParticle()) {
                guiParticle.renderTooltip(graphics, getParticleStack(), pMouseX, pMouseY);
            }
        }
        if(checkboxCasing.isMouseOver(pMouseX, pMouseY)) {
            graphics.renderTooltip(font, checkboxCasing.getTooltips(),
                    Optional.empty(), pMouseX, pMouseY);
        }
        if(checkboxInterior.isMouseOver(pMouseX, pMouseY)) {
            graphics.renderTooltip(font, checkboxInterior.getTooltips(),
                    Optional.empty(), pMouseX, pMouseY);
        }

        if(heatBar.isMouseOver(pMouseX, pMouseY)) {
            heatBar.clearTooltips();
            heatBar.addTooltip(__("reactor.cooling", container().getCooling()).withStyle(ChatFormatting.AQUA));
            heatBar.addTooltip(__("reactor.heating", container().getHeating()).withStyle(ChatFormatting.RED));
            heatBar.addTooltip(__("reactor.net_heat", container().getNetHeat()).withStyle(ChatFormatting.GOLD));
            graphics.renderTooltip(font, heatBar.getTooltips(),
                    Optional.empty(), pMouseX, pMouseY);
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
    public double getEnergy() {
        return container().getEnergy();
    }

    @Override
    public double getHeat() {
        return container().getHeat();
    }

    @Override
    public double getCoolant() {
        return 0;
    }

    @Override
    public double getHotCoolant() {
        return 0;
    }

    @Override
    public double getProgress() {
        return 0;
    }
}
