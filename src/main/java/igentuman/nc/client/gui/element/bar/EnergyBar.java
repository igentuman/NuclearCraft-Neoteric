package igentuman.nc.client.gui.element.bar;

import com.mojang.blaze3d.vertex.PoseStack;
import igentuman.nc.client.gui.element.NCGuiElement;
import net.minecraft.network.chat.Component;
import net.minecraftforge.energy.IEnergyStorage;

import java.util.List;

import static igentuman.nc.util.TextUtils.scaledFormat;

public class EnergyBar extends NCGuiElement {
    private final IEnergyStorage energy;

    public EnergyBar(int xMin, int yMin, IEnergyStorage energy)  {
        this.energy = energy;
        x = xMin;
        y = yMin;
        width = 16;
        height = 88;
    }

    public List<Component> getTooltips() {
        tooltips.add(Component.literal(scaledFormat(energy.getEnergyStored())+"/"+scaledFormat(energy.getMaxEnergyStored())+" FE"));
        return tooltips;
    }

    @Override
    public void draw(PoseStack transform, int mX, int mY, float pTicks) {
        super.draw(transform, mX, mY, pTicks);
        int stored = (int)(86*(energy.getEnergyStored()/(float)energy.getMaxEnergyStored()));
        blit(transform, X(), Y(), 36, 0,  18, 88);
        blit(transform, X()+1, Y()+1+86-stored, 54, 0,  16, stored);
    }
}