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
        width = 40;
        height = 15;
        this.container = container;
        bars.add(new int[] {0, 16});
        bars.add(new int[] {0, 47});
        bars.add(new int[] {0, 78});
        bars.add(new int[] {0, 107});
        bars.add(new int[] {0, 138});
        bars.add(new int[] {0, 169});
        bars.add(new int[] {0, 201});
    }

    public ProgressBar(int xMin, int yMin, IProgressScreen container, int barNumber)  {
        this(xMin, yMin, container);
        bar = barNumber;
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
        blit(transform, X(), Y(), texOffset, teyOffset,  36, 15);
        blit(transform, X(), Y(), texOffset, teyOffset-15, (int) (container.getProgress()*36), 15);
    }
}