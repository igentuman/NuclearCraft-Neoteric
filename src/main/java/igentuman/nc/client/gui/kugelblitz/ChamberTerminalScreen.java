package igentuman.nc.client.gui.kugelblitz;

import com.mojang.blaze3d.systems.RenderSystem;
import igentuman.nc.client.gui.IProgressScreen;
import igentuman.nc.client.gui.IVerticalBarScreen;
import igentuman.nc.client.gui.element.NCGuiElement;
import igentuman.nc.client.gui.element.bar.ProgressBar;
import igentuman.nc.client.gui.element.bar.VerticalBar;
import igentuman.nc.client.gui.element.button.Button;
import igentuman.nc.client.gui.element.button.Checkbox;
import igentuman.nc.client.gui.element.button.SliderHorizontal;
import igentuman.nc.client.gui.element.fluid.FluidTankRenderer;
import igentuman.nc.container.ChamberTerminalContainer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.font.FontManager;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.fluids.capability.templates.FluidTank;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.util.TextUtils.*;

public class ChamberTerminalScreen extends AbstractContainerScreen<ChamberTerminalContainer> implements IProgressScreen, IVerticalBarScreen {
    protected final ResourceLocation GUI = new ResourceLocation(MODID, "textures/gui/kugelblitz/controller.png");
    protected int relX;
    protected int relY;
    private int xCenter;
    private FluidTankRenderer feedingRateTank;
    private SliderHorizontal energyTransferRateSlider;
    private SliderHorizontal frequencySlider;
    private FontManager fontManager = new FontManager(Minecraft.getInstance().textureManager);
    private Font smallFont = fontManager.createFont();
    public ChamberTerminalContainer container()
    {
        return (ChamberTerminalContainer)menu;
    }

    public List<NCGuiElement> widgets = new ArrayList<>();
    public Checkbox checkboxCasing;
    public Checkbox checkboxInterior;
    private VerticalBar energyBar;
    private Button.ReactorMode modeBtn;

    public Component casingTootip = Component.empty();
    public Component interiorTootip = Component.empty();

    public ChamberTerminalScreen(ChamberTerminalContainer container, Inventory inv, Component name) {
        super(container, inv, name);
        imageWidth = 214;
        imageHeight = 186;
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

    @Override
    public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
        for(NCGuiElement widget : widgets) {
            if(widget.mouseReleased(pMouseX, pMouseY, pButton)) {
                return true;
            }
        }
        return super.mouseReleased(pMouseX, pMouseY, pButton);
    }

    @Override
    public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
        for(NCGuiElement widget : widgets) {
            if(widget.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY)) {
                return true;
            }
        }
        return super.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
    }

    protected void init() {
        super.init();
        Minecraft mc = Minecraft.getInstance();
        updateRelativeCords();
        widgets.clear();
        checkboxCasing = new Checkbox(6, 104, this,  isCasingValid());
        checkboxInterior =  new Checkbox(6, 122, this,  isInteriorValid());
        energyBar = new VerticalBar.Energy(200, 104,  this, container().getMaxEnergy());
        energyTransferRateSlider = new SliderHorizontal(6, 70, 119, this, menu.getBlockPos());
        frequencySlider = new SliderHorizontal(6, 90, 119, this, menu.getBlockPos(), 1);
        frequencySlider.slideTo(container().getFrequency());
        energyTransferRateSlider.slideTo(container().getEnergyRate());
        widgets.add(energyTransferRateSlider);
        widgets.add(frequencySlider);
        widgets.add(new ProgressBar(150, 80, this,  7));
        //modeBtn = new Button.ReactorMode(150, 54, this, menu.getPosition());
       // widgets.add(modeBtn);
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
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
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

        checkboxInterior.setChecked(isInteriorValid()).draw(graphics, mouseX, mouseY, partialTicks);
        if(isInteriorValid()) {
            checkboxInterior.setTooltipKey("multiblock.interior.complete");
        } else {
            checkboxInterior.setTooltipKey("multiblock.interior.incomplete");
        }
        checkboxInterior.addTooltip(interiorTootip);
        if(isInteriorValid()) {
        }
        energyBar.draw(graphics, mouseX, mouseY, partialTicks);

    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {

        graphics.drawCenteredString(font,  menu.getTitle(), imageWidth/2, titleLabelY, 0xffffff);

        graphics.pose().pushPose();
        graphics.pose().scale(0.5f, 0.5f, 0.5f);
        graphics.drawString(font, Component.translatable("label.kugelblitz.frequency", container().getFrequency()), 12, 164, 0x8AFF8A);
        graphics.drawString(font, Component.translatable("label.kugelblitz.transformation"), 12, 128, 0x8AFF8A);
        int w = font.width(Component.translatable("label.kugelblitz.energy_gen"));
        graphics.drawCenteredString(font, Component.translatable("label.kugelblitz.energy_gen"), 248-w/2, 128, 0x8AFF8A);
        graphics.pose().popPose();

        if(!isCasingValid()) {
            casingTootip = applyFormat(Component.translatable(getValidationResultKey(), getValidationResultData()), ChatFormatting.RED);
        }

        if(isCasingValid()) {
            if (isInteriorValid()) {
                if(container().hasBlackhole()) {
                    graphics.drawString(font, Component.translatable("label.kugelblitz.blackhole_mass", formatMass(container().getMass())), 6, 16, 0x8AFF8A);
                    graphics.drawString(font, Component.translatable("label.kugelblitz.evaporation", formatMass(container().getEvaporation())), 6, 27, 0x8AFF8A);
                    graphics.drawString(font, Component.translatable("label.kugelblitz.feeding", formatMass(container().getFeeding())), 6, 38, 0x8AFF8A);
                }
            } else {
                interiorTootip = applyFormat(Component.translatable(getValidationResultKey(), getValidationResultData()), ChatFormatting.RED);
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
        if(checkboxCasing.isMouseOver(pMouseX, pMouseY)) {
            graphics.renderTooltip(font, checkboxCasing.getTooltips(),
                    Optional.empty(), pMouseX, pMouseY);
        }
        if(checkboxInterior.isMouseOver(pMouseX, pMouseY)) {
            graphics.renderTooltip(font, checkboxInterior.getTooltips(),
                    Optional.empty(), pMouseX, pMouseY);
        }
        energyBar.clearTooltips();
        energyBar.addTooltip(Component.translatable("reactor.forge_energy_per_tick", formatEnergy(container().energyPerTick())));
        if(energyBar.isMouseOver(pMouseX, pMouseY+10)) {
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
}
