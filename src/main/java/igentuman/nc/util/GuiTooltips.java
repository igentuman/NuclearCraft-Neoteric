package igentuman.nc.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import org.joml.Vector2f;

import java.util.List;

/**
 * Deferred GUI tooltips issued from translated contexts.
 *
 * <p>{@code setTooltipForNextFrame} runs after the screen pose is popped, so coordinates given in a
 * translated space (for example inside {@code extractLabels}, which is offset by {@code leftPos/topPos})
 * would land up-left of the cursor. These helpers map local coordinates through the current pose first.
 */
public final class GuiTooltips {

    private GuiTooltips() {
    }

    /** Maps a point in the current pose's space to screen coordinates. */
    public static int[] toScreen(GuiGraphicsExtractor graphics, int x, int y) {
        Vector2f p = graphics.pose().transformPosition(new Vector2f(x, y));
        return new int[]{Math.round(p.x), Math.round(p.y)};
    }

    /** Sets a multi-line tooltip at a point given in the current pose's space. */
    public static void components(GuiGraphicsExtractor graphics, List<? extends Component> lines, int x, int y) {
        int[] p = toScreen(graphics, x, y);
        graphics.setComponentTooltipForNextFrame(Minecraft.getInstance().font, List.copyOf(lines), p[0], p[1]);
    }

    /** Sets a single-line tooltip at a point given in the current pose's space. */
    public static void component(GuiGraphicsExtractor graphics, Component line, int x, int y) {
        int[] p = toScreen(graphics, x, y);
        graphics.setTooltipForNextFrame(Minecraft.getInstance().font, line, p[0], p[1]);
    }
}
