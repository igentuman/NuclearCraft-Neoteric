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

public class EnergyBar extends AbstractWidget {

    private static final int COLOR_BORDER = 0xFF555555;
    private static final int COLOR_BG = 0xFF222222;
    private static final int COLOR_FILL = 0xFF00AA00;

    private final Supplier<IEnergyStorage> energySupplier;

    public EnergyBar(int x, int y, Supplier<IEnergyStorage> energySupplier) {
        super(x, y, 4, 60, Component.empty());
        this.energySupplier = energySupplier;
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        IEnergyStorage energy = energySupplier.get();
        if (energy == null) return;

        int x = getX();
        int y = getY();
        int w = 4;
        int h = 60;

        graphics.fill(x, y, x + w, y + h, COLOR_BORDER);
        graphics.fill(x + 1, y + 1, x + w - 1, y + h - 1, COLOR_BG);

        int stored = energy.getEnergyStored();
        int max = energy.getMaxEnergyStored();
        if (max > 0 && stored > 0) {
            int fillHeight = (int) ((double) stored / max * (h - 2));
            graphics.fill(x + 1, y + h - 1 - fillHeight, x + w - 1, y + h - 1, COLOR_FILL);
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
