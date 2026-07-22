package igentuman.nc.screen.element;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import static igentuman.nc.NuclearCraft.rl;

public final class NcPanel {

    private static final ResourceLocation SLOTS = rl("textures/gui/slots.png");

    private NcPanel() {}

    public static void drawPanel(GuiGraphics g, int x, int y, int w, int h) {
        g.fill(x, y, x + w, y + h, 0xFF373737);
        g.fill(x + 1, y + 1, x + w - 1, y + h - 1, 0xFFC6C6C6);
        g.fill(x + 1, y + 1, x + w - 1, y + 3, 0xFFFFFFFF);
        g.fill(x + 1, y + 1, x + 3, y + h - 1, 0xFFFFFFFF);
        g.fill(x + w - 3, y + 3, x + w - 1, y + h - 1, 0xFF555555);
        g.fill(x + 3, y + h - 3, x + w - 1, y + h - 1, 0xFF555555);
    }

    public static void drawCentered(GuiGraphics g, Font font, Component text, int cx, int y, int color) {
        g.drawString(font, text, cx - font.width(text) / 2, y, color, false);
    }

    public static void drawSlot(GuiGraphics g, int itemX, int itemY) {
        g.blit(SLOTS, itemX - 1, itemY - 1, 0, 0, 18, 18);
    }

    public static void drawOutputSlot(GuiGraphics g, int itemX, int itemY) {
        g.blit(SLOTS, itemX - 1, itemY - 1, 0, 36, 18, 18);
    }
}
