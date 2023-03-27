package igentuman.nc.gui.element.bar;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import igentuman.nc.container.NCProcessorContainer;
import igentuman.nc.gui.NCProcessorScreen;
import igentuman.nc.gui.element.NCGuiElement;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.energy.IEnergyStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static igentuman.nc.NuclearCraft.MODID;

public class ProgressBar extends NCGuiElement {
    protected static ResourceLocation ATLAS = new ResourceLocation(MODID, "textures/gui/progress.png");
    NCProcessorContainer container;

    public List<int[]> bars = new ArrayList<>();

    public ProgressBar(int xMin, int yMin, NCProcessorContainer container)  {
        x = xMin;
        y = yMin;
        this.container = container;
        bars.add(new int[] {0, 16});
        bars.add(new int[] {0, 46});
        bars.add(new int[] {0, 62});
    }


    public List<Component> getTooltips() {
        return List.of(Component.translatable("tooltip.processor.progress", container.getProgress()));
    }

    @Override
    public void draw(PoseStack transform, int mX, int mY, float pTicks) {
        super.draw(transform, mX, mY, pTicks);
        RenderSystem.setShaderTexture(0, ATLAS);
        int texOffset = bars.get(0)[0];
        int teyOffset = bars.get(0)[1];
        blit(transform, X(), Y(), texOffset, teyOffset,  36, 15);
        blit(transform, X(), Y(), texOffset, teyOffset-16,  container.getProgress()/100*36, 15);
    }
}