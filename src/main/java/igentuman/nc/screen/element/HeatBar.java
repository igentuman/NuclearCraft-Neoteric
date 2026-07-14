package igentuman.nc.screen.element;

import igentuman.nc.util.HeatBuffer;
import igentuman.nc.util.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.Supplier;

import static igentuman.nc.util.TextUtils.__;

/** Vertical bar showing a heat buffer's fill with a tooltip of current/max heat and gain/cooldown rates. */
public class HeatBar extends AbstractWidget {

    private static final int COLOR_BORDER = 0xFF555555;
    private static final int COLOR_BG = 0xFF222222;
    private static final int COLOR_FILL = 0xFF00AA00;
    private int width = 12;
    private int height = 70;

    private final Supplier<HeatBuffer> heatBufferSupplier;

    public HeatBar(int x, int y, Supplier<HeatBuffer> heatBufferSupplier) {
        this(x, y, 12, 70, heatBufferSupplier);
    }

    public HeatBar(int x, int y, int w, int h, Supplier<HeatBuffer> heatBufferSupplier) {
        super(x, y, w, h, Component.empty());
        this.heatBufferSupplier = heatBufferSupplier;
        width = w;
        height = h;
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        HeatBuffer heatBuffer = heatBufferSupplier.get();
        if (heatBuffer == null) return;

        int x = getX();
        int y = getY();
        int w = width;
        int h = height;

        graphics.fill(x, y, x + w, y + h, COLOR_BORDER);
        graphics.fill(x + 1, y + 1, x + w - 1, y + h - 1, COLOR_BG);

        double stored = heatBuffer.currentHeat;
        double max = heatBuffer.capacity;
        if (max > 0 && stored > 0) {
            int fillHeight = (int) ((double) stored / max * (h - 2));
            int fillTop = y + h - 1 - fillHeight;
            int fillBottom = y + h - 1;
            int fillLeft = x + 1;
            int fillRight = x + w - 1;
            for (int row = fillTop; row < fillBottom; row++) {
                float t = (float) (row - fillTop) / Math.max(fillHeight - 1, 1);
                int r = (int) (255 * (1.0f - t));
                int g = 0;
                int b = 0;
                int color = 0xFF000000 | (r << 16) | (g << 8) | b;
                graphics.fill(fillLeft, row, fillRight, row + 1, color);
            }
        }

        if (isHovered) {
            graphics.renderComponentTooltip(
                    Minecraft.getInstance().font,
                    List.of(
                            __("screen.nuclearcraft.heat").append(": ").append(TextUtils.formatHeat(heatBuffer.currentHeat) + " / " + TextUtils.formatHeat(heatBuffer.capacity)),
                            __("screen.nuclearcraft.heat_rate", TextUtils.formatHeat(heatBuffer.heatPerTick)),
                            __("screen.nuclearcraft.cooldown_rate", TextUtils.formatHeat(heatBuffer.cooldownPerTick)),
                            __("screen.nuclearcraft.net_heat", TextUtils.formatHeat(heatBuffer.netRate()))
                    ),
                    mouseX, mouseY
            );
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
    }
}
