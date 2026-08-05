package igentuman.nc.screen.element;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

/** Dropdown for selecting the active editing layer of the design grid. */
public class LayerDropdown extends AbstractWidget {

    protected static final int ROW_HEIGHT = 14;
    protected static final int MAX_VISIBLE_ROWS = 8;

    protected int layerCount = 1;
    protected int selectedIndex = 0;
    protected boolean open = false;
    protected Consumer<Integer> onSelect;

    public LayerDropdown(int x, int y, int width, int height) {
        super(x, y, width, height, Component.empty());
    }

    public LayerDropdown setOnSelect(Consumer<Integer> consumer) {
        this.onSelect = consumer;
        return this;
    }

    public LayerDropdown setLayerCount(int count) {
        this.layerCount = Math.max(1, count);
        this.selectedIndex = Math.min(selectedIndex, layerCount - 1);
        return this;
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public LayerDropdown setSelectedIndex(int index) {
        selectedIndex = Math.max(0, Math.min(layerCount - 1, index));
        return this;
    }

    protected String label(int index) {
        return "Layer " + (index + 1) + " / " + layerCount;
    }

    public boolean isOpen() {
        return open;
    }

    public void close() {
        open = false;
    }

    public void draw(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        Font font = Minecraft.getInstance().font;
        graphics.fill(getX(), getY(), getX() + width, getY() + height, 0xFF101010);
        graphics.fill(getX(), getY(), getX() + width, getY() + 1, 0xFF5A5A5A);
        graphics.fill(getX(), getY() + height - 1, getX() + width, getY() + height, 0xFF5A5A5A);
        graphics.fill(getX(), getY(), getX() + 1, getY() + height, 0xFF5A5A5A);
        graphics.fill(getX() + width - 1, getY(), getX() + width, getY() + height, 0xFF5A5A5A);
        graphics.drawString(font, label(selectedIndex), getX() + 4, getY() + (height - 8) / 2, 0xFFFFFF);
        graphics.drawString(font, Component.literal("v"), getX() + width - 7, getY() + (height - 8) / 2, 0xFFB0B0B0);
    }

    protected int visibleRows() {
        return Math.min(layerCount, MAX_VISIBLE_ROWS);
    }

    public void drawOverlay(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        if (!open) {
            return;
        }
        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 300);
        Font font = Minecraft.getInstance().font;
        int rows = visibleRows();
        int top = getY() + height;
        int bottom = top + rows * ROW_HEIGHT;
        graphics.fill(getX() - 1, top - 1, getX() + width + 1, bottom + 1, 0xFF5A5A5A);
        graphics.fill(getX(), top, getX() + width, bottom, 0xF0101010);
        for (int i = 0; i < rows; i++) {
            int rowY = top + i * ROW_HEIGHT;
            boolean hovered = mouseX >= getX() && mouseX < getX() + width && mouseY >= rowY && mouseY < rowY + ROW_HEIGHT;
            if (hovered) {
                graphics.fill(getX(), rowY, getX() + width, rowY + ROW_HEIGHT, 0x60FFFFFF);
            }
            graphics.drawString(font, label(i), getX() + 4, rowY + (ROW_HEIGHT - 8) / 2, 0xFFFFFF);
        }
        graphics.pose().popPose();
    }

    protected boolean inButton(double mouseX, double mouseY) {
        return mouseX >= getX() && mouseX < getX() + width && mouseY >= getY() && mouseY < getY() + height;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (inButton(mouseX, mouseY)) {
            open = !open;
            return true;
        }
        if (open) {
            int rows = visibleRows();
            int top = getY() + height;
            if (mouseX >= getX() && mouseX < getX() + width && mouseY >= top && mouseY < top + rows * ROW_HEIGHT) {
                int idx = (int) ((mouseY - top) / ROW_HEIGHT);
                if (idx >= 0 && idx < rows) {
                    selectedIndex = idx;
                    open = false;
                    if (onSelect != null) {
                        onSelect.accept(selectedIndex);
                    }
                }
                return true;
            }
            open = false;
        }
        return false;
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        draw(graphics, mouseX, mouseY, partialTicks);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
    }
}
