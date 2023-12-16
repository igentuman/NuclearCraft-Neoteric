package igentuman.nc.client.gui.element.bar;

import com.mojang.blaze3d.vertex.PoseStack;
import igentuman.nc.client.gui.FusionCoreScreen;
import igentuman.nc.client.gui.IVerticalBarScreen;
import igentuman.nc.client.gui.element.NCGuiElement;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.Supplier;

import static igentuman.nc.util.TextUtils.scaledFormat;

public class VerticalBar extends NCGuiElement {
    protected double barValue;
    protected double maxValue;
    protected String hintKey = "";
    protected int xOffset;
    protected int backgroundXoffset = 120;
    IVerticalBarScreen screen;

    public VerticalBar(int x, int y, IVerticalBarScreen screen, long max)  {
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
        if(hintKey.isEmpty()) return tooltips;
        tooltips.add(Component.translatable(hintKey, scaledFormat(barValue), scaledFormat(maxValue)));
        return tooltips;
    }

    @Override
    public void draw(PoseStack transform, int mX, int mY, float pTicks) {
        super.draw(transform, mX, mY, pTicks);
        int internal = height-2;
        int stored = (int)Math.min(internal, internal*(barValue/maxValue));
        blit(transform, X(), Y(), backgroundXoffset, 0,  width, height);
        blit(transform, X()+1, Y()+1+internal-stored, xOffset, internal-stored,  width-2, stored);

    }

    public void setTooltipKey(String s) {
        hintKey = s;
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

    public static class HeatLong extends VerticalBar{
        Supplier<Double> heat;
        public HeatLong(int x, int y, IVerticalBarScreen screen, long maxHeat) {
            super(x, y, screen, maxHeat);
            xOffset = 134;
            height = 97;
            backgroundXoffset = 146;
            hintKey = "heat.bar.amount";
        }

        public HeatLong(int x, int y, FusionCoreScreen screen, long maxHeat, Supplier<Double> o) {
            this(x, y, screen, maxHeat);
            heat = o;
        }

        @Override
        public void draw(PoseStack transform, int mX, int mY, float pTicks) {
            if(heat != null) {
                barValue = heat.get();
            } else {
                barValue = screen.getHeat();
            }
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

    public static class EnergyLong extends VerticalBar {
        public EnergyLong(int x, int y, IVerticalBarScreen screen,  int maxEnergy) {
            super(x, y, screen, maxEnergy);
            xOffset = 128;
            height = 97;
            backgroundXoffset = 146;

            hintKey = "energy.bar.amount";
        }

        @Override
        public void draw(PoseStack transform, int mX, int mY, float pTicks) {
            barValue = screen.getEnergy();
            super.draw(transform, mX, mY, pTicks);
        }
    }
    public static class Coolant extends VerticalBar {
        public Coolant(int x, int y, IVerticalBarScreen screen, int capacity) {
            super(x, y, screen, capacity);
            xOffset = 108;
            hintKey = "coolant.bar.amount";
        }

        @Override
        public void draw(PoseStack transform, int mX, int mY, float pTicks) {
            barValue = screen.getCoolant();
            super.draw(transform, mX, mY, pTicks);
        }
    }

    public static class CoolantLong extends VerticalBar {
        public CoolantLong(int x, int y, IVerticalBarScreen screen, int capacity) {
            super(x, y, screen, capacity);
            xOffset = 140;
            height = 97;
            backgroundXoffset = 146;
            hintKey = "";
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
            hintKey = "";
        }

        @Override
        public void draw(PoseStack transform, int mX, int mY, float pTicks) {
            barValue = screen.getHotCoolant();
            super.draw(transform, mX, mY, pTicks);
        }
    }
}