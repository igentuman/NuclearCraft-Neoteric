package igentuman.nc.client.gui.fission;

import com.mojang.blaze3d.systems.RenderSystem;
import igentuman.nc.client.gui.IProgressScreen;
import igentuman.nc.client.gui.IVerticalBarScreen;
import igentuman.nc.NuclearCraft;
import igentuman.nc.client.gui.element.NCGuiElement;
import igentuman.nc.client.gui.element.NCTextField;
import igentuman.nc.client.gui.element.bar.VerticalBar;
import igentuman.nc.client.gui.element.button.Button;
import igentuman.nc.client.gui.element.button.Checkbox;
import igentuman.nc.container.MSRControllerContainer;
import igentuman.nc.network.toServer.PacketSliderChanged;
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
import static igentuman.nc.util.TextUtils.applyFormat;
import static igentuman.nc.util.TextUtils.roundFormat;

public class MSRControllerScreen extends AbstractContainerScreen<MSRControllerContainer> implements IProgressScreen, IVerticalBarScreen {

    private static final ResourceLocation GUI = rl("textures/gui/fission/msr_controller.png");
    protected int relX;
    protected int relY;
    
    private List<NCGuiElement> widgets = new ArrayList<>();
    private VerticalBar heatBar;
    private NCTextField inputRateField;
    private NCTextField outputRateField;
    public Checkbox checkboxCasing;
    public Checkbox checkboxInterior;
    public Component casingTootip = Component.empty();
    public Component interiorTootip = Component.empty();

    public MSRControllerContainer container() {
        return (MSRControllerContainer) menu;
    }

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

        checkboxCasing = new Checkbox(imageWidth - 19, 80, this, isCasingValid());
        checkboxInterior = new Checkbox(imageWidth - 32, 80, this, isInteriorValid());

        widgets.add(new Button.ReportIssue(163, 6, this, container().getPosition()));

        // Molten-salt rate controls (buckets/tick): left = input, right = output
        inputRateField = new NCTextField(8, 60, 40, 12, true);
        inputRateField.setValue(String.valueOf(container().getInputRate()));
        inputRateField.setOnChange(value -> sendRate(value, 0));
        inputRateField.addTooltip(__("msr.input_rate.tooltip"));
        outputRateField = new NCTextField(imageWidth - 48, 60, 40, 12, true);
        outputRateField.setValue(String.valueOf(container().getOutputRate()));
        outputRateField.setOnChange(value -> sendRate(value, 1));
        outputRateField.addTooltip(__("msr.output_rate.tooltip"));
        widgets.add(inputRateField);
        widgets.add(outputRateField);
    }

    private void sendRate(String value, int buttonId) {
        int rate;
        try {
            rate = value.isEmpty() ? 0 : (int) Double.parseDouble(value);
        } catch (NumberFormatException e) {
            rate = 0;
        }
        NuclearCraft.packetHandler().sendToServer(new PacketSliderChanged(container().getPosition(), Math.max(0, rate), buttonId));
    }

    private boolean isCasingValid() {
        return container().isCasingValid();
    }

    private boolean isInteriorValid() {
        return container().isInteriorValid();
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawCenteredString(font, menu.getTitle(), imageWidth / 2, titleLabelY, 0xffffff);

        if (isCasingValid()) {
            casingTootip = applyFormat(__("tooltip.nc.structure.size", container().getHeight(), container().getWidth(), container().getDepth()), ChatFormatting.GOLD);
        } else {
            casingTootip = applyFormat(__(container().getValidationResultKey(), container().getValidationResultData().toShortString()), ChatFormatting.RED);
        }

        if (isCasingValid()) {
            if (isInteriorValid()) {
                interiorTootip = applyFormat(__("reactor.fuel_cells", container().getFuelCellsCount()), ChatFormatting.GOLD);
            } else {
                interiorTootip = applyFormat(__(container().getValidationResultKey(), container().getValidationResultData().toShortString()), ChatFormatting.RED);
            }
        }

        // Render stats
        graphics.pose().pushPose();
        graphics.pose().scale(0.7f, 0.7f, 0.7f);
        int y = 30;
        graphics.drawString(font, __("msr.reactivity", roundFormat(menu.getReactivity())), 45, y, 0x00ff00);
        graphics.drawString(font, __("msr.status", menu.isCritical() ? __("msr.critical") : __("msr.subcritical")), 45, y + 12, menu.isCritical() ? 0x00ff00 : 0xff0000);
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

        checkboxCasing.setChecked(isCasingValid()).draw(graphics, mouseX, mouseY, partialTick);
        if (isCasingValid()) {
            checkboxCasing.setTooltipKey("multiblock.casing.complete");
        } else {
            checkboxCasing.setTooltipKey("multiblock.casing.incomplete");
        }
        checkboxCasing.addTooltip(casingTootip);

        checkboxInterior.setChecked(isInteriorValid()).draw(graphics, mouseX, mouseY, partialTick);
        if (isInteriorValid()) {
            checkboxInterior.setTooltipKey("multiblock.interior.complete");
        } else {
            checkboxInterior.setTooltipKey("multiblock.interior.incomplete");
        }
        checkboxInterior.addTooltip(interiorTootip);

        renderBarTooltips(graphics, mouseX - relX, mouseY - relY);
    }

    private void renderBarTooltips(GuiGraphics graphics, int pMouseX, int pMouseY) {
        if(heatBar.isMouseOver(pMouseX, pMouseY)) {
            heatBar.clearTooltips();
            heatBar.addTooltip(__("reactor.heating", container().getHeating()).withStyle(ChatFormatting.RED));
        }
        for (NCGuiElement widget : widgets) {
            if (widget.isMouseOver(pMouseX, pMouseY)) {
                graphics.renderTooltip(font, widget.getTooltips(), Optional.empty(), pMouseX + relX, pMouseY + relY);
            }
        }
        if (checkboxCasing.isMouseOver(pMouseX, pMouseY)) {
            graphics.renderTooltip(font, checkboxCasing.getTooltips(), Optional.empty(), pMouseX + relX, pMouseY + relY);
        }
        if (checkboxInterior.isMouseOver(pMouseX, pMouseY)) {
            graphics.renderTooltip(font, checkboxInterior.getTooltips(), Optional.empty(), pMouseX + relX, pMouseY + relY);
        }
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        for (NCGuiElement widget : widgets) {
            if (widget.mouseClicked(pMouseX, pMouseY, pButton)) {
                return true;
            }
        }
        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }

    @Override
    public boolean keyPressed(int key, int scanCode, int modifiers) {
        for (NCGuiElement widget : widgets) {
            if (widget.keyPressed(key, scanCode, modifiers)) {
                return true;
            }
        }
        return super.keyPressed(key, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char c, int modifiers) {
        for (NCGuiElement widget : widgets) {
            if (widget.charTyped(c, modifiers)) {
                return true;
            }
        }
        return super.charTyped(c, modifiers);
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
        return 0;
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
        return menu.getHotSalt();
    }
}
