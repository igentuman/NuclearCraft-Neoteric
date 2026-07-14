package igentuman.nc.screen.element;

import igentuman.nc.util.BoilingBuffer;
import igentuman.nc.util.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.Supplier;

import static igentuman.nc.util.TextUtils.__;

/** Vertical bar showing a boiling buffer's coolant (blue) and hot coolant (orange) with a rate tooltip. */
public class BoilingBar extends AbstractWidget {

    private static final int COLOR_BORDER = 0xFF555555;
    private static final int COLOR_BG = 0xFF222222;
    private static final int COLOR_COOLANT = 0xFF00AAFF;
    private static final int COLOR_HOT_COOLANT = 0xFFFFAA00;
    private int width = 12;
    private int height = 70;

    private final Supplier<BoilingBuffer> boilingBufferSupplier;

    public BoilingBar(int x, int y, Supplier<BoilingBuffer> boilingBufferSupplier) {
        this(x, y, 12, 70, boilingBufferSupplier);
    }

    public BoilingBar(int x, int y, int w, int h, Supplier<BoilingBuffer> boilingBufferSupplier) {
        super(x, y, w, h, Component.empty());
        this.boilingBufferSupplier = boilingBufferSupplier;
        width = w;
        height = h;
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        BoilingBuffer boilingBuffer = boilingBufferSupplier.get();
        if (boilingBuffer == null) return;

        int x = getX();
        int y = getY();
        int w = width;
        int h = height;

        graphics.fill(x, y, x + w, y + h, COLOR_BORDER);
        graphics.fill(x + 1, y + 1, x + w - 1, y + h - 1, COLOR_BG);

        double max = boilingBuffer.capacity;
        if (max > 0) {
            int mid = x + w / 2;
            // Coolant (blue)
            if (boilingBuffer.coolantAmount > 0) {
                int fillHeight = (int) (boilingBuffer.coolantAmount / max * (h - 2));
                graphics.fill(x + 1, y + h - 1 - fillHeight, mid, y + h - 1, COLOR_COOLANT);
            }
            // Hot coolant (orange)
            if (boilingBuffer.hotCoolantAmount > 0) {
                int fillHeight = (int) (boilingBuffer.hotCoolantAmount / max * (h - 2));
                graphics.fill(mid, y + h - 1 - fillHeight, x + w - 1, y + h - 1, COLOR_HOT_COOLANT);
            }
        }

        if (isHovered) {
            graphics.renderComponentTooltip(
                    Minecraft.getInstance().font,
                    List.of(
                            __("screen.nuclearcraft.boiling.capacity").append(": ").append(TextUtils.formatLiquid((int) boilingBuffer.capacity)),
                            __("screen.nuclearcraft.boiling.coolant").append(": ").append(TextUtils.formatLiquid((int) boilingBuffer.coolantAmount)),
                            __("screen.nuclearcraft.boiling.hot_coolant").append(": ").append(TextUtils.formatLiquid((int) boilingBuffer.hotCoolantAmount)),
                            __("screen.nuclearcraft.boiling.rate").append(": ").append(TextUtils.formatLiquid((int) boilingBuffer.boilingRate) + " / " + TextUtils.formatLiquid((int) boilingBuffer.maxBoilingRate))
                    ),
                    mouseX, mouseY
            );
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
    }
}
