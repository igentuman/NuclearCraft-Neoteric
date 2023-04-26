package igentuman.nc.client.gui.element.bar;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import igentuman.nc.client.gui.IProgressScreen;
import igentuman.nc.client.gui.element.NCGuiElement;
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

    public List<int[]> bars = new ArrayList<>();

    public ProgressBar(int xMin, int yMin, IProgressScreen container)  {
        x = xMin;
        y = yMin;
        width = 36;
        height = 15;
        this.container = container;
        bars.add(new int[] {0, 16});

        bars.add(new int[] {0, 16});
        bars.add(new int[] {0, 47});
        bars.add(new int[] {0, 78});
        bars.add(new int[] {0, 109});
        bars.add(new int[] {0, 140});
        bars.add(new int[] {0, 171});
        bars.add(new int[] {0, 201});

        bars.add(new int[] {40, 16});
        bars.add(new int[] {40, 47});
        bars.add(new int[] {40, 78});
        bars.add(new int[] {40, 109});
        bars.add(new int[] {40, 138});
        bars.add(new int[] {40, 169});
        bars.add(new int[] {40, 201});

        bars.add(new int[] {74, 38});
        bars.add(new int[] {111, 38});
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
        return List.of(Component.translatable("tooltip.machine.progress", numberFormat(container.getProgress()*100)));
    }

    @Override
    public void draw(PoseStack transform, int mX, int mY, float pTicks) {
        super.draw(transform, mX, mY, pTicks);
        RenderSystem.setShaderTexture(0, ATLAS);
        int texOffset = bars.get(bar)[0];
        int teyOffset = bars.get(bar)[1];
        blit(transform, X(), Y(), texOffset, teyOffset,  width, height);
        blit(transform, X(), Y(), texOffset, teyOffset-height, (int) (container.getProgress()*width), height);
    }
}