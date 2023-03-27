package igentuman.nc.gui.element.bar;

import com.mojang.blaze3d.vertex.PoseStack;
import igentuman.nc.gui.element.NCGuiElement;
import net.minecraft.network.chat.Component;
import net.minecraftforge.energy.IEnergyStorage;

import java.util.List;

public class EnergyBar extends NCGuiElement {
    private final IEnergyStorage energy;

    public EnergyBar(int xMin, int yMin, IEnergyStorage energy)  {
        this.energy = energy;
        x = xMin;
        y = yMin;
    }

    public List<Component> getTooltips() {
        return List.of(Component.literal(energy.getEnergyStored()+"/"+energy.getMaxEnergyStored()+" FE"));
    }

    @Override
    public void draw(PoseStack transform, int mX, int mY, float pTicks) {
        super.draw(transform, mX, mY, pTicks);
        int stored = (int)(86*(energy.getEnergyStored()/(float)energy.getMaxEnergyStored()));
        blit(transform, X(), Y(), 36, 0,  18, 88);
        blit(transform, X()+1, Y()+1, 54, 0,  17, stored);
    }
}