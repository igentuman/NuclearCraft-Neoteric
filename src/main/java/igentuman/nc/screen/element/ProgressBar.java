package igentuman.nc.screen.element;

import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.List;

import static igentuman.nc.NuclearCraft.rl;

/** Widget rendering a machine's recipe progress by overlaying a filled arrow from a selectable atlas sprite. */
public class ProgressBar extends AbstractWidget {
    private int x;
    private int y;
    public int bar = 0;
    private double progress;

    protected static Identifier ATLAS = rl("textures/gui/progress_bars.png");

    public static List<int[]> bars = List.of(
            new int[] {0, 16},
            new int[] {0, 16},
            new int[] {0, 47},
            new int[] {0, 78},
            new int[] {0, 109},
            new int[] {0, 140},
            new int[] {0, 171},
            new int[] {0, 202},
            new int[] {37, 16},
            new int[] {37, 47},
            new int[] {37, 78},
            new int[] {37, 109},
            new int[] {37, 140},
            new int[] {37, 171},
            new int[] {37, 202},
            new int[] {74, 38},
            new int[] {111, 38}
    );

    public ProgressBar(int xMin, int yMin)  {
        super(xMin, yMin, 36, 15, Component.empty());
        x = xMin;
        y = yMin;
        width = 36;
        height = 15;
    }

    public ProgressBar(int xMin, int yMin, int barNumber)  {
        this(xMin, yMin);
        bar = barNumber;
        if(bar > 14) {
            height = 36;
            y -= 10;
        }
    }

    public void setProgress(int progress) {
        this.progress = (double)progress/100D;
    }


    public List<Component> getTooltips() {
        //return List.of(__("tooltip.machine.progress", numberFormat(container.getProgress()*100)));
        return List.of();
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        int texOffset = bars.get(bar)[0];
        int teyOffset = bars.get(bar)[1];
        graphics.blit(RenderPipelines.GUI_TEXTURED, ATLAS, x, y, texOffset, teyOffset,  width, height, 256, 256);
        graphics.blit(RenderPipelines.GUI_TEXTURED, ATLAS, x, y, texOffset, teyOffset-height-1, (int) (progress*width), height, 256, 256);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }
}
