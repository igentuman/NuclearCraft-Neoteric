package igentuman.nc.screen;

import igentuman.nc.block_entity.fission.MsrControllerBE;
import igentuman.nc.container.MsrControllerContainer;
import igentuman.nc.container.MultiblockControllerContainer;
import igentuman.nc.network.PacketMsrVoidFuel;
import igentuman.nc.screen.element.MsrRateSlider;
import igentuman.nc.screen.element.MsrVoidFuelButton;
import igentuman.nc.screen.element.TemperatureBar;
import igentuman.nc.setup.ModEntries;
import igentuman.nc.util.GuiFluidRenderer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;
import java.util.Random;

import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.util.TextUtils.__;
import static igentuman.nc.util.TextUtils.numberFormat;
import static igentuman.nc.util.TextUtils.roundFormat;
import static igentuman.nc.util.TextUtils.scaledFormat;

public class MsrControllerScreen extends MultiblockControllerScreen {

    private static final ResourceLocation TEXTURE = rl("textures/gui/fission/msr_controller.png");
    private static final int SHEET = 256;
    private static final int WINDOW_X = 127;
    private static final int WINDOW_Y = 19;
    private static final int PEBBLE_RENDER_CAP = 300;

    private MsrRateSlider inputSlider;
    private MsrRateSlider outputSlider;

    public MsrControllerScreen(MultiblockControllerContainer menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        imageWidth = 176;
        imageHeight = 176;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    private MsrControllerContainer msr() {
        return (MsrControllerContainer) menu;
    }

    @Override
    protected void init() {
        super.init();
        progressBar.visible = false;

        if (slotWidgets.size() >= 4) {
            removeWidget(slotWidgets.get(3));
            removeWidget(slotWidgets.get(1));
            slotWidgets.remove(3);
            slotWidgets.remove(1);
        }

        infoCheckbox.setX(leftPos + 164);
        infoCheckbox.setY(topPos + 6);

        MsrControllerContainer c = msr();

        addRenderableWidget(new TemperatureBar(leftPos + 8, topPos + 16, 8, 70,
                c::getTemperature, () -> MsrControllerBE.MAX_TEMPERATURE));

        inputSlider = new MsrRateSlider(leftPos + 20, topPos + 72, 60, 6,
                menu.getPosition(), 0, c::getInputRate);
        outputSlider = new MsrRateSlider(leftPos + 96, topPos + 72, 60, 6,
                menu.getPosition(), 1, c::getOutputRate);
        addRenderableWidget(inputSlider);
        addRenderableWidget(outputSlider);

        addRenderableWidget(new MsrVoidFuelButton(leftPos + 156, topPos + 70,
                ModEntries.get("msr_fuel_cell").block().toStack(),
                () -> PacketDistributor.sendToServer(new PacketMsrVoidFuel(menu.getPosition())),
                () -> List.of(__("gui.nc.msr.void_pebbles.tooltip"))));
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight, SHEET, SHEET);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        drawCenteredString(guiGraphics, this.font, this.title, this.imageWidth / 2, this.titleLabelY, 0xFFFFFF);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0x404040, false);

        MsrControllerContainer c = msr();
        if (!menu.isFormed()) return;

        renderFuelWindow(guiGraphics, c, mouseX - leftPos, mouseY - topPos);
        renderStats(guiGraphics, c);
    }

    private void renderFuelWindow(GuiGraphics g, MsrControllerContainer c, int mouseX, int mouseY) {
        FluidStack cold = c.fluid(MsrControllerContainer.TANK_COLD);
        int coldCap = c.capacity(MsrControllerContainer.TANK_COLD);
        int hotCap = c.capacity(MsrControllerContainer.TANK_HOT);
        GuiFluidRenderer.renderFluidTank(g, WINDOW_X + 3, WINDOW_Y + 3, 36, 36, cold, coldCap);

        g.pose().pushPose();
        g.pose().translate(0, 0, 200);
        Random rnd = new Random(200);
        g.pose().scale(0.5f, 0.5f, 0.5f);
        double cx = 17.0, cy = 17.0, r = 17.0;
        int qty = Math.min(PEBBLE_RENDER_CAP, c.getPebblesQty());
        int placed = 0;
        while (placed < qty) {
            int px = rnd.nextInt(33);
            int py = rnd.nextInt(33);
            double dx = px - cx, dy = py - cy;
            if (dx * dx + dy * dy > r * r) continue;
            g.blit(TEXTURE, (WINDOW_X + 2 + px) * 2, (WINDOW_Y + 5 + py) * 2, 250, 0, 5, 5, SHEET, SHEET);
            placed++;
        }
        g.pose().scale(2f, 2f, 2f);
        g.blit(TEXTURE, WINDOW_X, WINDOW_Y, 178, 0, 52, 50, SHEET, SHEET);
        g.pose().popPose();

        GuiFluidRenderer.renderFluidTooltip(g, mouseX, mouseY, WINDOW_X + 3, WINDOW_Y + 3, 32, 32, cold, coldCap);
    }

    private void renderStats(GuiGraphics g, MsrControllerContainer c) {
        g.pose().pushPose();
        g.pose().scale(0.7f, 0.7f, 0.7f);
        int y = 30;
        MutableComponent status = c.getFuelCellsCount() == 0
                ? __("msr.non_functional")
                : (c.isCritical() ? __("msr.critical") : __("msr.subcritical"));
        g.drawString(font, __("msr.reactivity", numberFormat(c.getReactivity())), 18, y, 0x00ff00, false);
        g.drawString(font, __("msr.status", status), 18, y + 12,
                c.isCritical() ? 0x00ff00 : ChatFormatting.WHITE.getColor(), false);
        g.drawString(font, __("msr.temperature", scaledFormat(c.getTemperature())), 18, y + 22,
                c.getTemperature() < MsrControllerBE.MAX_TEMPERATURE ? 0x00ff00 : 0xff0000, false);
        g.drawString(font, __("msr.depletion", numberFormat(c.getDepletion())), 18, y + 32, 0x00ff00, false);
        if (c.getOverheatTimer() > 0) {
            g.drawString(font, __("msr.overheat", roundFormat((600 - c.getOverheatTimer()) / 20.0)),
                    18, y + 42, 0xff0000, false);
        }
        g.pose().popPose();
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (inputSlider != null && inputSlider.isSliderDragging()) {
            return inputSlider.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        }
        if (outputSlider != null && outputSlider.isSliderDragging()) {
            return outputSlider.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }
}
