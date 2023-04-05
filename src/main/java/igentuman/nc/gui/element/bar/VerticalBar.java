package igentuman.nc.gui.element.bar;

import com.mojang.blaze3d.vertex.PoseStack;
import igentuman.nc.gui.IVerticalBarScreen;
import igentuman.nc.gui.element.NCGuiElement;
import net.minecraft.network.chat.Component;
import net.minecraftforge.energy.IEnergyStorage;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

import static igentuman.nc.util.TextUtils.scaledFormat;

public class VerticalBar extends NCGuiElement {
    protected double barValue;
    protected double maxValue;
    protected String hintKey = "";
    protected int xOffset;
    IVerticalBarScreen screen;

    public VerticalBar(int x, int y, IVerticalBarScreen screen, int max)  {
        this.x = x;
        this.y = y;
        xOffset = 96;
        this.barValue = 0;
        this.maxValue = max;
        this.screen = screen;
        width = 8;
        height = 88;
    }

    public List<Component> getTooltips() {
        return List.of(Component.translatable(hintKey, scaledFormat(barValue), scaledFormat(maxValue)));
    }

    @Override
    public void draw(PoseStack transform, int mX, int mY, float pTicks) {
        super.draw(transform, mX, mY, pTicks);
        int internal = height-2;
        int stored = (int)(internal*(barValue/maxValue));
        blit(transform, X(), Y(), 120, 0,  width, height);
        blit(transform, X()+1, Y()+1+internal-stored, xOffset, internal-stored,  width-2, stored);

    }

    public static class Heat extends VerticalBar{
        public Heat(int x, int y, IVerticalBarScreen screen, int maxHeat) {
            super(x, y, screen, maxHeat);
            xOffset = 102;
            hintKey = "heat.bar.amount";
        }
        @Override
        public void draw(PoseStack transform, int mX, int mY, float pTicks) {
            barValue = screen.getHeat();
            super.draw(transform, mX, mY, pTicks);
        }
    }

    public static class Energy extends VerticalBar {
        public Energy(int x, int y, IVerticalBarScreen screen,  int maxEnergy) {
            super(x, y, screen, maxEnergy);
            xOffset = 96;
            hintKey = "energy.bar.amount";
        }

        @Override
        public void draw(PoseStack transform, int mX, int mY, float pTicks) {
            barValue = screen.getEnergy();
            super.draw(transform, mX, mY, pTicks);
        }
    }
    public static class Coolant extends VerticalBar {
        public Coolant(int x, int y, IVerticalBarScreen screen, int maxEnergy) {
            super(x, y, screen, maxEnergy);
            xOffset = 108;
            hintKey = "coolant.bar.amount";
        }

        @Override
        public void draw(PoseStack transform, int mX, int mY, float pTicks) {
            barValue = screen.getCoolant();
            super.draw(transform, mX, mY, pTicks);
        }
    }

    public static class HotCoolant extends VerticalBar {
        public HotCoolant(int x, int y, IVerticalBarScreen screen, int maxEnergy) {
            super(x, y, screen, maxEnergy);
            xOffset = 114;
            hintKey = "hot_coolant.bar.amount";
        }

        @Override
        public void draw(PoseStack transform, int mX, int mY, float pTicks) {
            barValue = screen.getHotCoolant();
            super.draw(transform, mX, mY, pTicks);
        }
    }
}