package igentuman.nc.screen.element;

import igentuman.nc.util.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.energy.IEnergyStorage;

import java.util.List;
import java.util.function.Supplier;

/** Vertical bar showing an energy store's fill level with a formatted stored/max tooltip. */
public class EnergyBar extends AbstractWidget {

    private static final int COLOR_BORDER = 0xFF555555;
    private static final int COLOR_BG = 0xFF222222;
    private static final int COLOR_FILL = 0xFF00AA00;
    private int width = 12;
    private int height = 70;

    private final Supplier<IEnergyStorage> energySupplier;

    public EnergyBar(int x, int y, Supplier<IEnergyStorage> energySupplier) {
        this(x, y, 12, 70, energySupplier);
    }

    public EnergyBar(int x, int y, int w, int h, Supplier<IEnergyStorage> energySupplier) {
        super(x, y, w, h, Component.empty());
        width = w;
        height = h;
        this.energySupplier = energySupplier;
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        IEnergyStorage energy = energySupplier.get();
        if (energy == null) return;

        int x = getX();
        int y = getY();
        int w = width;
        int h = height;

        graphics.fill(x, y, x + w, y + h, COLOR_BORDER);
        graphics.fill(x + 1, y + 1, x + w - 1, y + h - 1, COLOR_BG);

        int stored = energy.getEnergyStored();
        int max = energy.getMaxEnergyStored();
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
            int safeStored = energy.getEnergyStored();
            int safeMax = energy.getMaxEnergyStored();
            graphics.renderComponentTooltip(
                    Minecraft.getInstance().font,
                    List.of(Component.literal(TextUtils.formatEnergy(safeStored) + " / " + TextUtils.formatEnergy(safeMax))),
                    mouseX, mouseY
            );
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
    }
}
