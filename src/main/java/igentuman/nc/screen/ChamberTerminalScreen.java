package igentuman.nc.screen;

import igentuman.nc.block_entity.kugelblitz.ChamberTerminalBE;
import igentuman.nc.container.MultiblockControllerContainer;
import igentuman.nc.screen.element.KugelblitzSlider;
import igentuman.nc.screen.element.ProgressBar;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.Objects;

import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.util.TextUtils.__;
import static igentuman.nc.util.TextUtils.scaledFormat;

/** Kugelblitz chamber terminal screen: energy-conversion + quantum-frequency sliders and black-hole readouts. */
public class ChamberTerminalScreen extends MultiblockControllerScreen {

    private static final ResourceLocation TEXTURE = rl("textures/gui/kugelblitz_controller.png");
    private static final int TEXT = 0xFFFFFF;
    private static final float SCALE = 0.75f;

    private KugelblitzSlider rateSlider;
    private KugelblitzSlider frequencySlider;

    public ChamberTerminalScreen(MultiblockControllerContainer menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        imageWidth = 214;
        imageHeight = 186;
        this.inventoryLabelX = 27;
        this.inventoryLabelY = 95;
    }

    @Override
    protected void init() {
        super.init();
        rateSlider = new KugelblitzSlider(leftPos + 6, topPos + 70, 119, 8,
                menu.getPosition(), 0, () -> be() != null ? Objects.requireNonNull(be()).energyConvertionRate : 7);
        frequencySlider = new KugelblitzSlider(leftPos + 6, topPos + 90, 119, 8,
                menu.getPosition(), 1, () -> be() != null ? Math.round(be().frequency / 0.15f) : 0);
        addRenderableWidget(rateSlider);
        addRenderableWidget(frequencySlider);

        if (energyBar != null) {
            energyBar.setX(leftPos + 198);
            energyBar.setY(topPos + 108);
        }
        if (infoCheckbox != null) {
            infoCheckbox.setX(leftPos + 6);
            infoCheckbox.setY(topPos + 108);
        }
        if (progressBar != null) {
            removeWidget(progressBar);
        }
        progressBar = new ProgressBar(leftPos + 152, topPos + 81);
        addRenderableWidget(progressBar);
    }

    private ChamberTerminalBE be() {
        return menu.getBlockEntity() instanceof ChamberTerminalBE be ? be : null;
    }

    private void drawScaled(GuiGraphics g, Component text, int x, int y) {
        g.pose().pushPose();
        g.pose().scale(SCALE, SCALE, 1f);
        g.drawString(this.font, text, Math.round(x / SCALE), Math.round(y / SCALE), TEXT, false);
        g.pose().popPose();
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        drawCenteredString(g, this.font, this.title, imageWidth / 2, 7, TEXT);
        if (!(be() instanceof ChamberTerminalBE be)) return;

        drawScaled(g, __("screen.nuclearcraft.kugelblitz.rate", be.energyConvertionRate), 6, 62);
        drawScaled(g, __("screen.nuclearcraft.kugelblitz.frequency", (int) be.frequency), 6, 82);

        if (!menu.isFormed()) return;
        drawScaled(g, __("screen.nuclearcraft.kugelblitz.mass", scaledFormat(be.mass)), 6, 16);
        drawScaled(g, __("screen.nuclearcraft.kugelblitz.evaporation", scaledFormat(be.evaporation)), 6, 25);
        drawScaled(g, __("screen.nuclearcraft.kugelblitz.feeding", scaledFormat(be.feeding)), 6, 34);
        Component stability = __("screen.nuclearcraft.kugelblitz.stability", be.blackholeStability)
                .withStyle(be.blackholeStability < 40 ? ChatFormatting.RED : ChatFormatting.GREEN);
        drawScaled(g, stability, 6, 43);
        drawScaled(g, __("screen.nuclearcraft.kugelblitz.energy", scaledFormat(be.energyPerTick)), 6, 52);

    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (rateSlider != null && rateSlider.isSliderDragging()) {
            return rateSlider.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        }
        if (frequencySlider != null && frequencySlider.isSliderDragging()) {
            return frequencySlider.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        g.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);
        progressBar.setProgress(menu.getProgress());
    }
}
