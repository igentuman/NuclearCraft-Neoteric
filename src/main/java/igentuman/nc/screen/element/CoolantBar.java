package igentuman.nc.screen.element;

import igentuman.nc.util.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.List;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

import static igentuman.nc.util.TextUtils.__;

/** Dual coolant-loop bar: cold coolant (blue, left half) in, hot coolant (orange, right half) out. */
public class CoolantBar extends AbstractWidget {

    private static final int COLOR_BORDER = 0xFF555555;
    private static final int COLOR_BG = 0xFF222222;
    private static final int COLOR_COOLANT = 0xFF00AAFF;
    private static final int COLOR_HOT_COOLANT = 0xFFFFAA00;

    private final Supplier<FluidStack> coolantSupplier;
    private final Supplier<FluidStack> hotCoolantSupplier;
    private final IntSupplier capacitySupplier;

    public CoolantBar(int x, int y, int w, int h,
                      Supplier<FluidStack> coolantSupplier,
                      Supplier<FluidStack> hotCoolantSupplier,
                      IntSupplier capacitySupplier) {
        super(x, y, w, h, Component.empty());
        this.coolantSupplier = coolantSupplier;
        this.hotCoolantSupplier = hotCoolantSupplier;
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

        FluidStack coolant = coolantSupplier.get();
        FluidStack hot = hotCoolantSupplier.get();
        int capacity = capacitySupplier.getAsInt();
        if (capacity > 0) {
            int mid = x + w / 2;
            if (coolant != null && !coolant.isEmpty()) {
                int fillHeight = (int) ((long) (h - 2) * coolant.getAmount() / capacity);
                graphics.fill(x + 1, y + h - 1 - fillHeight, mid, y + h - 1, COLOR_COOLANT);
            }
            if (hot != null && !hot.isEmpty()) {
                int fillHeight = (int) ((long) (h - 2) * hot.getAmount() / capacity);
                graphics.fill(mid, y + h - 1 - fillHeight, x + w - 1, y + h - 1, COLOR_HOT_COOLANT);
            }
        }

        if (isHovered) {
            graphics.renderComponentTooltip(
                    Minecraft.getInstance().font,
                    List.of(
                            __("screen.nuclearcraft.boiling.coolant").append(": ")
                                    .append(TextUtils.formatLiquid(coolant == null ? 0 : coolant.getAmount())),
                            __("screen.nuclearcraft.boiling.hot_coolant").append(": ")
                                    .append(TextUtils.formatLiquid(hot == null ? 0 : hot.getAmount()))
                    ),
                    mouseX, mouseY
            );
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
    }
}
