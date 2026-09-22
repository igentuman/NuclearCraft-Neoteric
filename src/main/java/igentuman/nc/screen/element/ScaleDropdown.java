package igentuman.nc.screen.element;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

/** Dropdown for the particle source's keV/MeV/GeV/TeV energy scale. */
public class ScaleDropdown extends AbstractWidget {

    public static final String[] SCALES = {"keV", "MeV", "GeV", "TeV"};
    private static final int ROW_HEIGHT = 14;

    private int selectedIndex = 1;
    private boolean open;
    private Consumer<Integer> onSelect;

    public ScaleDropdown(int x, int y, int width, int height) {
        super(x, y, width, height, Component.empty());
    }

    public ScaleDropdown setOnSelect(Consumer<Integer> consumer) {
        onSelect = consumer;
        return this;
    }

    public ScaleDropdown setSelectedIndex(int index) {
        selectedIndex = Math.max(0, Math.min(SCALES.length - 1, index));
        return this;
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public boolean isOpen() {
        return open;
    }

    public void close() {
        open = false;
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(getX(), getY(), getX() + width, getY() + height, 0xFF101010);
        graphics.fill(getX(), getY(), getX() + width, getY() + 1, 0xFF5A5A5A);
        graphics.fill(getX(), getY() + height - 1, getX() + width, getY() + height, 0xFF5A5A5A);
        graphics.fill(getX(), getY(), getX() + 1, getY() + height, 0xFF5A5A5A);
        graphics.fill(getX() + width - 1, getY(), getX() + width, getY() + height, 0xFF5A5A5A);
        Font font = Minecraft.getInstance().font;
        graphics.drawString(font, SCALES[selectedIndex], getX() + 4, getY() + (height - 8) / 2, 0xFFFFFF, false);
        graphics.drawString(font, "v", getX() + width - 7, getY() + (height - 8) / 2, 0xFFB0B0B0, false);
    }

    public void renderOverlay(GuiGraphics graphics, int mouseX, int mouseY) {
        if (!open) return;
        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 300);
        int top = getY() + height;
        int bottom = top + SCALES.length * ROW_HEIGHT;
        graphics.fill(getX() - 1, top - 1, getX() + width + 1, bottom + 1, 0xFF5A5A5A);
        graphics.fill(getX(), top, getX() + width, bottom, 0xF0101010);
        Font font = Minecraft.getInstance().font;
        for (int index = 0; index < SCALES.length; index++) {
            int rowY = top + index * ROW_HEIGHT;
            if (mouseX >= getX() && mouseX < getX() + width && mouseY >= rowY && mouseY < rowY + ROW_HEIGHT) {
                graphics.fill(getX(), rowY, getX() + width, rowY + ROW_HEIGHT, 0x60FFFFFF);
            }
            graphics.drawString(font, SCALES[index], getX() + 4, rowY + 3, 0xFFFFFF, false);
        }
        graphics.pose().popPose();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isMouseOver(mouseX, mouseY)) {
            open = !open;
            return true;
        }
        if (open) {
            int top = getY() + height;
            if (mouseX >= getX() && mouseX < getX() + width
                    && mouseY >= top && mouseY < top + SCALES.length * ROW_HEIGHT) {
                selectedIndex = (int) ((mouseY - top) / ROW_HEIGHT);
                open = false;
                if (onSelect != null) onSelect.accept(selectedIndex);
                return true;
            }
            close();
        }
        return false;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
    }
}
