package igentuman.nc.screen.element;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntSupplier;
import java.util.function.BooleanSupplier;

import static igentuman.nc.util.TextUtils.__;

public class AcceleratorThermalBar extends AbstractWidget {

    private static final int COLOR_BORDER = 0xFF555555;
    private static final int COLOR_BG = 0xFF222222;

    private final int width;
    private final int height;
    private final IntSupplier temperatureK;
    private final IntSupplier maximumTemperatureK;
    private final BooleanSupplier overheated;
    private final IntSupplier overheatCooldownTicks;

    public AcceleratorThermalBar(int x, int y, int w, int h, IntSupplier temperatureK, IntSupplier maximumTemperatureK,
                                  BooleanSupplier overheated, IntSupplier overheatCooldownTicks) {
        super(x, y, w, h, Component.empty());
        this.width = w;
        this.height = h;
        this.temperatureK = temperatureK;
        this.maximumTemperatureK = maximumTemperatureK;
        this.overheated = overheated;
        this.overheatCooldownTicks = overheatCooldownTicks;
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int x = getX();
        int y = getY();
        int w = width;
        int h = height;

        graphics.fill(x, y, x + w, y + h, COLOR_BORDER);
        graphics.fill(x + 1, y + 1, x + w - 1, y + h - 1, COLOR_BG);

        long current = temperatureK.getAsInt();
        long max = maximumTemperatureK.getAsInt();
        if (max > 0 && current > 0) {
            int fillHeight = (int) Math.min(h - 2, current * (h - 2) / max);
            int fillTop = y + h - 1 - fillHeight;
            int fillBottom = y + h - 1;
            int fillLeft = x + 1;
            int fillRight = x + w - 1;
            for (int row = fillTop; row < fillBottom; row++) {
                float t = (float) (row - fillTop) / Math.max(fillHeight - 1, 1);
                int r = (int) (255 * (1.0f - t));
                int b = (int) (255 * t);
                int color = 0xFF000000 | (r << 16) | b;
                graphics.fill(fillLeft, row, fillRight, row + 1, color);
            }
        }

        if (isHovered) {
            List<Component> tooltip = new ArrayList<>();
            tooltip.add(__("screen.nuclearcraft.accelerator.temperature", current, max));
            if (overheated.getAsBoolean()) {
                tooltip.add(__("screen.nuclearcraft.accelerator.overheated", overheatCooldownTicks.getAsInt()));
            }
            graphics.renderComponentTooltip(Minecraft.getInstance().font, tooltip, mouseX, mouseY);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
    }
}
