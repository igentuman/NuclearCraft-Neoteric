package igentuman.nc.screen.element;

import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import static igentuman.nc.NuclearCraft.rl;

public final class NcPanel {

    private static final Identifier SLOTS = rl("textures/gui/slots.png");

    private NcPanel() {}

    public static void drawPanel(GuiGraphicsExtractor g, int x, int y, int w, int h) {
        g.fill(x, y, x + w, y + h, 0xFF373737);
        g.fill(x + 1, y + 1, x + w - 1, y + h - 1, 0xFFC6C6C6);
        g.fill(x + 1, y + 1, x + w - 1, y + 3, 0xFFFFFFFF);
        g.fill(x + 1, y + 1, x + 3, y + h - 1, 0xFFFFFFFF);
        g.fill(x + w - 3, y + 3, x + w - 1, y + h - 1, 0xFF555555);
        g.fill(x + 3, y + h - 3, x + w - 1, y + h - 1, 0xFF555555);
    }

    public static void drawCentered(GuiGraphicsExtractor g, Font font, Component text, int cx, int y, int color) {
        g.text(font, text, cx - font.width(text) / 2, y, igentuman.nc.util.GuiText.legacy(color), false);
    }

    public static void drawSlot(GuiGraphicsExtractor g, int itemX, int itemY) {
        g.blit(RenderPipelines.GUI_TEXTURED, SLOTS, itemX - 1, itemY - 1, 0, 0, 18, 18, 256, 256);
    }

    public static void drawOutputSlot(GuiGraphicsExtractor g, int itemX, int itemY) {
        g.blit(RenderPipelines.GUI_TEXTURED, SLOTS, itemX - 1, itemY - 1, 0, 36, 18, 18, 256, 256);
    }
}
