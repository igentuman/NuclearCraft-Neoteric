package igentuman.nc.client.gui.heat_exchanger;

import com.mojang.blaze3d.systems.RenderSystem;
import igentuman.nc.client.gui.IProgressScreen;
import igentuman.nc.client.gui.IVerticalBarScreen;
import igentuman.nc.client.gui.element.NCGuiElement;
import igentuman.nc.client.gui.element.bar.ProgressBar;
import igentuman.nc.client.gui.element.bar.VerticalBar;
import igentuman.nc.client.gui.element.button.Button;
import igentuman.nc.client.gui.element.button.Checkbox;
import igentuman.nc.client.gui.element.fluid.FluidTankRenderer;
import igentuman.nc.client.gui.element.slot.NormalSlot;
import igentuman.nc.container.HeatExchangerControllerContainer;
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
import static igentuman.nc.util.TextUtils.*;

public class HeatExchangerControllerScreen extends AbstractContainerScreen<HeatExchangerControllerContainer> implements IProgressScreen, IVerticalBarScreen {
    protected final ResourceLocation GUI = rl("textures/gui/heat_exchanger/controller.png");
    protected int relX;
    protected int relY;

    public HeatExchangerControllerContainer container() {
        return menu;
    }

    public List<NCGuiElement> widgets = new ArrayList<>();
    public Checkbox checkboxCasing;
    public Checkbox checkboxInterior;
    private VerticalBar energyBar;
    private VerticalBar heatBar;
    public Component casingTootip = Component.empty();
    public Component interiorTootip = Component.empty();

    public HeatExchangerControllerScreen(HeatExchangerControllerContainer container, Inventory inv, Component name) {
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
        Minecraft mc = Minecraft.getInstance();
        updateRelativeCords();
        widgets.clear();
        checkboxCasing = new Checkbox(imageWidth - 19, 80, this, isCasingValid());
        checkboxInterior = new Checkbox(imageWidth - 32, 80, this, isInteriorValid());
        widgets.add(new ProgressBar(74, 34, this, 7));
        energyBar = new VerticalBar.Energy(10, 16, this, container().getMaxEnergy());
        heatBar = new VerticalBar.Heat(22, 16, this, container().getMaxHeat());
        addWidget(new NormalSlot(new int[]{50, 18}, "fluid_in"));
        addWidget(new NormalSlot(new int[]{50, 52}, "fluid_in"));
        addWidget(new NormalSlot(new int[]{120, 18}, "fluid_out"));
        addWidget(new NormalSlot(new int[]{120, 52}, "fluid_out"));
        // tank 0 = hot in, 2 = hot out, 1 = cold in, 3 = cold out
        addWidget(FluidTankRenderer.tank(getFluidTank(0)).id(0).size(17, 17).pos(50, 18).canVoid());
        addWidget(FluidTankRenderer.tank(getFluidTank(2)).id(2).size(17, 17).pos(120, 18).canVoid());
        addWidget(FluidTankRenderer.tank(getFluidTank(1)).id(1).size(17, 17).pos(50, 52).canVoid());
        addWidget(FluidTankRenderer.tank(getFluidTank(3)).id(3).size(17, 17).pos(120, 52).canVoid());
    }

    protected void addWidget(NCGuiElement widget) {
        widget.setScreen(this);
        widgets.add(widget);
    }

    protected FluidTank getFluidTank(int i) {
        return menu.getFluidTank(i);
    }

    private boolean isInteriorValid() {
        return container().isInteriorValid();
    }

    private boolean isCasingValid() {
        return container().isCasingValid();
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    private void renderWidgets(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        for (NCGuiElement widget : widgets) {
            widget.draw(graphics, mouseX, mouseY, partialTicks);
        }
        checkboxCasing.setChecked(isCasingValid()).draw(graphics, mouseX, mouseY, partialTicks);
        if (isCasingValid()) {
            checkboxCasing.setTooltipKey("multiblock.casing.complete");
        } else {
            checkboxCasing.setTooltipKey("multiblock.casing.incomplete");
        }
        checkboxCasing.addTooltip(casingTootip);

        checkboxInterior.setChecked(isInteriorValid() && isCasingValid()).draw(graphics, mouseX, mouseY, partialTicks);
        if (isInteriorValid() && isCasingValid()) {
            checkboxInterior.setTooltipKey("multiblock.interior.complete");
        } else {
            checkboxInterior.setTooltipKey("multiblock.interior.incomplete");
        }
        checkboxInterior.addTooltip(interiorTootip);
        if (isInteriorValid() && isCasingValid()) {
            checkboxInterior.addTooltip(__("heat_exchanger.blocks", container().getHeatExchangers()));
        }
        energyBar.draw(graphics, mouseX, mouseY, partialTicks);
        heatBar.draw(graphics, mouseX, mouseY, partialTicks);
    }

    @Override
    protected void renderLabels(@NotNull GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawCenteredString(font, menu.getTitle(), imageWidth / 2, titleLabelY, 0xffffff);
        if (isCasingValid()) {
            casingTootip = applyFormat(__("tooltip.nc.structure.size", getMultiblockHeight(), getMultiblockWidth(), getMultiblockDepth()), ChatFormatting.GOLD);
        } else {
            casingTootip = applyFormat(__(getValidationResultKey(), getValidationResultData()), ChatFormatting.RED);
        }

        if (isCasingValid() && !isInteriorValid()) {
            interiorTootip = applyFormat(__(getValidationResultKey(), getValidationResultData()), ChatFormatting.RED);
        }

        renderTooltips(graphics, mouseX - relX, mouseY - relY);
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

    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        for (NCGuiElement widget : widgets) {
            if (widget.mouseClicked(pMouseX, pMouseY, pButton)) {
                return true;
            }
        }
        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }

    private void renderTooltips(GuiGraphics graphics, int pMouseX, int pMouseY) {
        for (NCGuiElement widget : widgets) {
            if (widget.isMouseOver(pMouseX, pMouseY)) {
                graphics.renderTooltip(font, widget.getTooltips(),
                        Optional.empty(), pMouseX, pMouseY);
            }
        }
        if (checkboxCasing.isMouseOver(pMouseX, pMouseY)) {
            graphics.renderTooltip(font, checkboxCasing.getTooltips(),
                    Optional.empty(), pMouseX, pMouseY);
        }
        if (checkboxInterior.isMouseOver(pMouseX, pMouseY)) {
            graphics.renderTooltip(font, checkboxInterior.getTooltips(),
                    Optional.empty(), pMouseX, pMouseY);
        }
        if (container().getMaxEnergy() > 0) {
            energyBar.clearTooltips();
            if (container().isRunning()) {
                energyBar.addTooltip(__(energyUseLine(), container().energyPerTick()));
            }
            if (energyBar.isMouseOver(pMouseX, pMouseY)) {
                graphics.renderTooltip(font, energyBar.getTooltips(),
                        Optional.empty(), pMouseX, pMouseY);
            }
        }
        heatBar.clearTooltips();
        if (heatBar.isMouseOver(pMouseX, pMouseY)) {
            graphics.renderTooltip(font, heatBar.getTooltips(),
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
        return container().getProgress();
    }
}
