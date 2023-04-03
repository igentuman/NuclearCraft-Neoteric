package igentuman.nc.gui.element.bar;

import com.mojang.blaze3d.vertex.PoseStack;
import igentuman.nc.gui.element.NCGuiElement;
import net.minecraft.network.chat.Component;
import net.minecraftforge.energy.IEnergyStorage;

import java.util.List;

public class VerticalBar extends NCGuiElement {
    private int barValue;
    private int maxValue;
    private String hintKey = "";
    protected int xOffset;

    public VerticalBar(int x, int y, int barValue, int max)  {
        this.x = x;
        this.y = y;
        xOffset = 96;
        this.barValue = barValue;
        this.maxValue = max;
    }

    public List<Component> getTooltips() {
        return List.of(Component.translatable(hintKey, barValue, maxValue));
    }

    @Override
    public void draw(PoseStack transform, int mX, int mY, float pTicks) {
        super.draw(transform, mX, mY, pTicks);
        int stored = (int)(86*(barValue/maxValue));
        blit(transform, X(), Y(), 120, 0,  8, 88);
        blit(transform, X()+1, Y()+1+86-stored, xOffset, 0,  6, stored);

    }

    public static class Heat extends VerticalBar{
        public Heat(int x, int y, int heat, int maxHeat) {
            super(x, y, heat, maxHeat);
            xOffset = 102;
        }
    }

    public static class Energy extends VerticalBar {
        public Energy(int x, int y, int energy, int maxEnergy) {
            super(x, y, energy, maxEnergy);
            xOffset = 96;
        }
    }
    public static class Coolant extends VerticalBar {
        public Coolant(int x, int y, int energy, int maxEnergy) {
            super(x, y, energy, maxEnergy);
            xOffset = 108;
        }
    }

    public static class HotCoolant extends VerticalBar {
        public HotCoolant(int x, int y, int energy, int maxEnergy) {
            super(x, y, energy, maxEnergy);
            xOffset = 114;
        }
    }
}