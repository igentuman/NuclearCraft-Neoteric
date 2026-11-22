package igentuman.nc.screen.element;

import net.minecraft.client.input.MouseButtonEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import igentuman.nc.network.PacketFusionAmplificationAdjust;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.function.IntSupplier;

/** Horizontal slider tuning the fusion reactor RF amplification adjustment (1-100). */
public class FusionAmplifierSlider extends AbstractWidget {

    private static final int KNOB_W = 6;

    private final BlockPos pos;
    private final IntSupplier value;
    private boolean dragging;
    private int dragValue = 50;
    private int lastSent = -1;

    public FusionAmplifierSlider(int x, int y, int width, int height, BlockPos pos, IntSupplier value) {
        super(x, y, width, height, Component.empty());
        this.pos = pos;
        this.value = value;
    }

    public boolean isSliderDragging() {
        return dragging;
    }

    private int displayValue() {
        return dragging ? dragValue : Math.min(100, Math.max(1, value.getAsInt()));
    }

    private int trackX0() {
        return getX() + 1;
    }

    private int trackX1() {
        return getX() + width - 1 - KNOB_W;
    }

    private int valueToX(int v) {
        return trackX0() + Math.round((v - 1) / 99f * (trackX1() - trackX0()));
    }

    private int xToValue(double mouseX) {
        double frac = (mouseX - trackX0()) / (double) (trackX1() - trackX0());
        return Math.min(100, Math.max(1, (int) Math.round(1 + frac * 99)));
    }

    private void send(int v) {
        if (v != lastSent) {
            lastSent = v;
            ClientPacketDistributor.sendToServer(new PacketFusionAmplificationAdjust(pos, v));
        }
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        int x = getX();
        int y = getY();
        int mid = y + height / 2;
        g.fill(x, mid - 1, x + width, mid + 1, 0xFF2B2B2B);
        int kx = valueToX(displayValue());
        g.fill(kx, y, kx + KNOB_W, y + height, 0xFF888888);
        g.fill(kx + 1, y + 1, kx + KNOB_W - 1, y + height - 1, 0xFFCFCFCF);
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean doubleClick) {
        double mouseX = event.x();
        dragging = true;
        dragValue = xToValue(mouseX-3);
        send(dragValue);
    }

    @Override
    protected void onDrag(MouseButtonEvent event, double dragX, double dragY) {
        double mouseX = event.x();
        dragValue = xToValue(mouseX-3);
        send(dragValue);
    }

    @Override
    public void onRelease(MouseButtonEvent event) {
        double mouseX = event.x();
        if (dragging) {
            send(xToValue(mouseX-3));
            dragging = false;
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
    }
}
