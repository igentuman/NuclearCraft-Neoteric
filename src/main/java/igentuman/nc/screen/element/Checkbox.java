package igentuman.nc.screen.element;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.Supplier;

/** Toggleable checkbox widget that fills with a checked/hovered/unchecked color and shows a supplied tooltip. */
public class Checkbox extends AbstractWidget {

    private static final int COLOR_BORDER = 0xFF444444;
    private static final int COLOR_UNCHECKED = 0xFF888888;
    private static final int COLOR_HOVERED = 0x8800CC00;
    private static final int COLOR_CHECKED = 0xFF00AA00;

    private boolean checked;
    private final Supplier<List<Component>> tooltipSupplier;

    public Checkbox(int x, int y, int size, boolean checked, Supplier<List<Component>> tooltipSupplier) {
        super(x, y, size, size, Component.empty());
        this.checked = checked;
        this.tooltipSupplier = tooltipSupplier;
    }

    public boolean isChecked() {
        return checked;
    }

    public Checkbox setChecked(boolean checked) {
        this.checked = checked;
        return this;
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int x = getX();
        int y = getY();
        int w = getWidth();
        int h = getHeight();

        graphics.fill(x, y, x + w, y + h, COLOR_BORDER);

        int fillColor;
        if (checked) {
            fillColor = COLOR_CHECKED;
        } else if (isHovered()) {
            fillColor = COLOR_HOVERED;
        } else {
            fillColor = COLOR_UNCHECKED;
        }
        graphics.fill(x + 1, y + 1, x + w - 1, y + h - 1, fillColor);

        if (isHovered && tooltipSupplier != null) {
            List<Component> tooltips = tooltipSupplier.get();
            if (tooltips != null && !tooltips.isEmpty()) {
                graphics.renderComponentTooltip(Minecraft.getInstance().font, tooltips, mouseX, mouseY);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isActive() || !visible) return false;
        if (mouseX >= getX() && mouseX < getX() + getWidth() && mouseY >= getY() && mouseY < getY() + getHeight()) {
            checked = !checked;
            playDownSound(Minecraft.getInstance().getSoundManager());
            return true;
        }
        return false;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
    }
}
