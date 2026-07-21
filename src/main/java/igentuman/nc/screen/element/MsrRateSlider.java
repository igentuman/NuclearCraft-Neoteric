package igentuman.nc.screen.element;

import igentuman.nc.network.PacketMsrRateAdjust;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.function.IntSupplier;

/** Horizontal slider (0-100 buckets/tick) tuning a molten salt reactor rate; buttonId 0 = salt input, 1 = salt output. */
public class MsrRateSlider extends AbstractWidget {

    private static final int KNOB_W = 6;
    private static final int MAX = 100;

    private final BlockPos pos;
    private final int buttonId;
    private final IntSupplier value;
    private boolean dragging;
    private int dragValue;
    private int lastSent = -1;

    public MsrRateSlider(int x, int y, int width, int height, BlockPos pos, int buttonId, IntSupplier value) {
        super(x, y, width, height, Component.empty());
        this.pos = pos;
        this.buttonId = buttonId;
        this.value = value;
    }

    public boolean isSliderDragging() {
        return dragging;
    }

    public int displayValue() {
        return dragging ? dragValue : Math.min(MAX, Math.max(0, value.getAsInt()));
    }

    private int trackX0() {
        return getX() + 1;
    }

    private int trackX1() {
        return getX() + width - 1 - KNOB_W;
    }

    private int valueToX(int v) {
        return trackX0() + Math.round(v / (float) MAX * (trackX1() - trackX0()));
    }

    private int xToValue(double mouseX) {
        double frac = (mouseX - trackX0()) / (double) (trackX1() - trackX0());
        return Math.min(MAX, Math.max(0, (int) Math.round(frac * MAX)));
    }

    private void send(int v) {
        if (v != lastSent) {
            lastSent = v;
            PacketDistributor.sendToServer(new PacketMsrRateAdjust(pos, buttonId, v));
        }
    }

    @Override
    protected void renderWidget(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        int x = getX();
        int y = getY();
        int mid = y + height / 2;
        g.fill(x, mid - 1, x + width, mid + 1, 0xFF2B2B2B);
        int kx = valueToX(displayValue());
        g.fill(kx, y, kx + KNOB_W, y + height, 0xFF888888);
        g.fill(kx + 1, y + 1, kx + KNOB_W - 1, y + height - 1, 0xFFCFCFCF);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        dragging = true;
        dragValue = xToValue(mouseX - 3);
        send(dragValue);
    }

    @Override
    protected void onDrag(double mouseX, double mouseY, double dragX, double dragY) {
        dragValue = xToValue(mouseX - 3);
        send(dragValue);
    }

    @Override
    public void onRelease(double mouseX, double mouseY) {
        if (dragging) {
            send(xToValue(mouseX - 3));
            dragging = false;
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
    }
}
