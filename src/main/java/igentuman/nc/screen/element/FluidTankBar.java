package igentuman.nc.screen.element;

import igentuman.nc.util.GuiFluidRenderer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.function.IntSupplier;
import java.util.function.Supplier;

/** Vertical fluid bar for a single tank: bottom-filled by the fluid texture, tinted per fluid. */
public class FluidTankBar extends AbstractWidget {

    private static final int COLOR_BORDER = 0xFF555555;
    private static final int COLOR_BG = 0xFF222222;

    private final Supplier<FluidStack> fluidSupplier;
    private final IntSupplier capacitySupplier;

    public FluidTankBar(int x, int y, int w, int h, Supplier<FluidStack> fluidSupplier, IntSupplier capacitySupplier) {
        super(x, y, w, h, Component.empty());
        this.fluidSupplier = fluidSupplier;
        this.capacitySupplier = capacitySupplier;
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int x = getX();
        int y = getY();
        int w = width;
        int h = height;

        graphics.fill(x, y, x + w, y + h, COLOR_BORDER);
        graphics.fill(x + 1, y + 1, x + w - 1, y + h - 1, COLOR_BG);

        FluidStack fluid = fluidSupplier.get();
        int capacity = capacitySupplier.getAsInt();
        if (fluid != null && !fluid.isEmpty() && capacity > 0) {
            // Immediate-mode fluid draw: flush the batched background first so it stays behind.
            graphics.flush();
            GuiFluidRenderer.renderFluidTank(graphics, x + 1, y + 1, w - 2, h - 2, fluid, capacity);
        }

        if (isHovered) {
            GuiFluidRenderer.renderFluidTooltip(graphics, mouseX, mouseY, x, y, w, h,
                    fluid == null ? FluidStack.EMPTY : fluid, capacity);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
    }
}
