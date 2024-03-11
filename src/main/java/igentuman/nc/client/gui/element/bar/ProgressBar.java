package igentuman.nc.client.gui.element.bar;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.matrix.MatrixStack;
import igentuman.nc.client.gui.IProgressScreen;
import igentuman.nc.client.gui.element.NCGuiElement;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.util.TextUtils.numberFormat;
import static net.minecraft.client.gui.AbstractGui.blit;

public class ProgressBar extends NCGuiElement {

    public int bar = 0;
    protected static ResourceLocation ATLAS = new ResourceLocation(MODID, "textures/gui/progress.png");
    IProgressScreen container;

    public static List<int[]> bars = Arrays.asList(
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

    public ProgressBar(int xMin, int yMin, IProgressScreen container)  {
        super(xMin, yMin, 36, 15, new TranslationTextComponent(""));
        x = xMin;
        y = yMin;
        width = 36;
        height = 15;
        this.container = container;

    }

    public ProgressBar(int xMin, int yMin, IProgressScreen container, int barNumber)  {
        this(xMin, yMin, container);
        bar = barNumber;
        if(bar > 14) {
            height = 36;
            y -= 10;
        }
    }


    public List<ITextComponent> getTooltips() {
        //return Arrays.asList(new TranslationTextComponent("tooltip.machine.progress", numberFormat(container.getProgress()*100)));
        return Arrays.asList();
    }

    @Override
    public void draw(MatrixStack transform, int mX, int mY, float pTicks) {
        super.draw(transform, mX, mY, pTicks);
       // RenderSystem.text(0, ATLAS);
        int texOffset = bars.get(bar)[0];
        int teyOffset = bars.get(bar)[1];
      //  blit(transform, X(), Y(), texOffset, teyOffset,  width, height);
        //blit(transform, X(), Y(), texOffset, teyOffset-height-1, (int) (container.getProgress()*width), height);
    }
}