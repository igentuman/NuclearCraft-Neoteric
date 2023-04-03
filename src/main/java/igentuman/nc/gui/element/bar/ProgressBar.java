package igentuman.nc.gui.element.bar;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import igentuman.nc.container.NCProcessorContainer;
import igentuman.nc.gui.IProgressScreen;
import igentuman.nc.gui.NCProcessorScreen;
import igentuman.nc.gui.element.NCGuiElement;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.energy.IEnergyStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static igentuman.nc.NuclearCraft.MODID;

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
        return List.of(Component.translatable("tooltip.machine.progress", container.getProgress()));
    }

    @Override
    public void draw(PoseStack transform, int mX, int mY, float pTicks) {
        super.draw(transform, mX, mY, pTicks);
        RenderSystem.setShaderTexture(0, ATLAS);
        int texOffset = bars.get(bar)[0];
        int teyOffset = bars.get(bar)[1];
        blit(transform, X(), Y(), texOffset, teyOffset,  36, 15);
        blit(transform, X(), Y(), texOffset, teyOffset-16,  container.getProgress()/100*36, 15);
    }
}