package igentuman.nc.client.gui.element;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

public class ScaleDropdown extends NCGuiElement implements IDropdown {

    public static final String[] SCALES = {"keV", "MeV", "GeV", "TeV"};
    protected static final int ROW_HEIGHT = 14;

    protected int selectedIndex = 1;
    protected boolean open = false;
    protected Consumer<Integer> onSelect;

    public ScaleDropdown(int x, int y, int width, int height) {
        super(x, y, width, height, Component.empty());
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public ScaleDropdown setOnSelect(Consumer<Integer> consumer) {
        this.onSelect = consumer;
        return this;
    }

    public ScaleDropdown setSelectedIndex(int index) {
        selectedIndex = Math.max(0, Math.min(SCALES.length - 1, index));
        return this;
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    @Override
    public boolean isOpen() {
        return open;
    }

    @Override
    public void close() {
        open = false;
    }

    @Override
    public void draw(PoseStack graphics, int mouseX, int mouseY, float partialTicks) {
        Font font = Minecraft.getInstance().font;
        GuiComponent.fill(graphics, X(), Y(), X() + width, Y() + height, 0xFF101010);
        GuiComponent.fill(graphics, X(), Y(), X() + width, Y() + 1, 0xFF5A5A5A);
        GuiComponent.fill(graphics, X(), Y() + height - 1, X() + width, Y() + height, 0xFF5A5A5A);
        GuiComponent.fill(graphics, X(), Y(), X() + 1, Y() + height, 0xFF5A5A5A);
        GuiComponent.fill(graphics, X() + width - 1, Y(), X() + width, Y() + height, 0xFF5A5A5A);
        font.draw(graphics, SCALES[selectedIndex], X() + 4, Y() + (height - 8) / 2, 0xFFFFFF);
        font.draw(graphics, Component.literal("v"), X() + width - 7, Y() + (height - 8) / 2, 0xFFB0B0B0);
    }

    @Override
    public void drawOverlay(PoseStack graphics, int mouseX, int mouseY, float partialTicks) {
        if (!open) {
            return;
        }
        graphics.pushPose();
        graphics.translate(0, 0, 300);
        Font font = Minecraft.getInstance().font;
        int top = Y() + height;
        int bottom = top + SCALES.length * ROW_HEIGHT;
        GuiComponent.fill(graphics, X() - 1, top - 1, X() + width + 1, bottom + 1, 0xFF5A5A5A);
        GuiComponent.fill(graphics, X(), top, X() + width, bottom, 0xF0101010);
        for (int i = 0; i < SCALES.length; i++) {
            int rowY = top + i * ROW_HEIGHT;
            boolean hovered = mouseX >= X() && mouseX < X() + width && mouseY >= rowY && mouseY < rowY + ROW_HEIGHT;
            if (hovered) {
                GuiComponent.fill(graphics, X(), rowY, X() + width, rowY + ROW_HEIGHT, 0x60FFFFFF);
            }
            font.draw(graphics, SCALES[i], X() + 4, rowY + (ROW_HEIGHT - 8) / 2, 0xFFFFFF);
        }
        graphics.popPose();
    }

    protected boolean inButton(double mouseX, double mouseY) {
        return mouseX >= X() && mouseX < X() + width && mouseY >= Y() && mouseY < Y() + height;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (inButton(mouseX, mouseY)) {
            open = !open;
            return true;
        }
        if (open) {
            int top = Y() + height;
            if (mouseX >= X() && mouseX < X() + width && mouseY >= top && mouseY < top + SCALES.length * ROW_HEIGHT) {
                int idx = (int) ((mouseY - top) / ROW_HEIGHT);
                if (idx >= 0 && idx < SCALES.length) {
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
}
