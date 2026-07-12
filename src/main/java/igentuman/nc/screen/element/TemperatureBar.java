package igentuman.nc.screen.element;

import igentuman.nc.util.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.DoubleSupplier;

import static igentuman.nc.util.TextUtils.__;

/** Vertical bar for the reactor casing temperature (reactorHeat / maxHeat), black-to-red gradient. */
public class TemperatureBar extends AbstractWidget {

    private static final int COLOR_BORDER = 0xFF555555;
    private static final int COLOR_BG = 0xFF222222;

    private final DoubleSupplier currentSupplier;
    private final DoubleSupplier maxSupplier;

    public TemperatureBar(int x, int y, int w, int h, DoubleSupplier currentSupplier, DoubleSupplier maxSupplier) {
        super(x, y, w, h, Component.empty());
        this.currentSupplier = currentSupplier;
        this.maxSupplier = maxSupplier;
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int x = getX();
        int y = getY();
        int w = width;
        int h = height;

        graphics.fill(x, y, x + w, y + h, COLOR_BORDER);
        graphics.fill(x + 1, y + 1, x + w - 1, y + h - 1, COLOR_BG);

        double stored = currentSupplier.getAsDouble();
        double max = maxSupplier.getAsDouble();
        if (max > 0 && stored > 0) {
            int fillHeight = (int) (Math.min(stored / max, 1.0) * (h - 2));
            int fillTop = y + h - 1 - fillHeight;
            int fillBottom = y + h - 1;
            int fillLeft = x + 1;
            int fillRight = x + w - 1;
            for (int row = fillTop; row < fillBottom; row++) {
                float t = (float) (row - fillTop) / Math.max(fillHeight - 1, 1);
                int r = (int) (255 * (1.0f - t));
                int color = 0xFF000000 | (r << 16);
                graphics.fill(fillLeft, row, fillRight, row + 1, color);
            }
        }

        if (isHovered) {
            graphics.renderComponentTooltip(
                    Minecraft.getInstance().font,
                    List.of(__("screen.nuclearcraft.fusion.temperature",
                            TextUtils.formatHeat(stored) + " / " + TextUtils.formatHeat(max))),
                    mouseX, mouseY
            );
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
    }
}
