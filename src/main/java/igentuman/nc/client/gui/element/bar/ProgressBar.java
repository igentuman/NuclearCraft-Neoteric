package igentuman.nc.client.gui.element.bar;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import igentuman.nc.client.gui.IProgressScreen;
import igentuman.nc.client.gui.element.NCGuiElement;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.util.TextUtils.numberFormat;

public class ProgressBar extends NCGuiElement {

    public int bar = 0;
    protected static ResourceLocation ATLAS = new ResourceLocation(MODID, "textures/gui/progress.png");
    IProgressScreen container;

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

    public ProgressBar(int xMin, int yMin, IProgressScreen container)  {
        super(xMin, yMin, 36, 15, Component.empty());
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


    public List<Component> getTooltips() {
        //return List.of(Component.translatable("tooltip.machine.progress", numberFormat(container.getProgress()*100)));
        return List.of();
    }

    @Override
    public void draw(GuiGraphics graphics, int mX, int mY, float pTicks) {
        super.draw(graphics, mX, mY, pTicks);
        RenderSystem.setShaderTexture(0, ATLAS);
        int texOffset = bars.get(bar)[0];
        int teyOffset = bars.get(bar)[1];
        graphics.blit(TEXTURE, X(), Y(), texOffset, teyOffset,  width, height);
        graphics.blit(TEXTURE, X(), Y(), texOffset, teyOffset-height-1, (int) (container.getProgress()*width), height);
    }
}