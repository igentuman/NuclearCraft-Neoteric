package igentuman.nc.client.gui.element.slot;

import com.mojang.blaze3d.vertex.PoseStack;
import igentuman.nc.client.gui.element.NCGuiElement;
import igentuman.nc.client.gui.processor.side.SideConfigScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

import static igentuman.nc.handler.sided.SlotModePair.SlotMode.INPUT;
import static igentuman.nc.handler.sided.SlotModePair.SlotMode.OUTPUT;

public class VerticalLongSlot extends NCGuiElement {
    public int xOffset = 0;
    public int yOffset = 0;
    String type;
    public int color = OUTPUT.getColor();

    public VerticalLongSlot(int xMin, int yMin)  {
        super(xMin, yMin, 8, 50, null);
        x = xMin;
        y = yMin;
        width = 8;
        height = 50;
    }

    public boolean onPress()
    {
        if(configFlag) {
            this.playDownSound(Minecraft.getInstance().getSoundManager());
            Minecraft.getInstance().forceSetScreen(new SideConfigScreen<>(screen, slotId));
            return true;
        } else {
           return super.onPress();
        }
    }

    @Override
    public void draw(GuiGraphics graphics, int mX, int mY, float pTicks) {
        super.draw(graphics, mX, mY, pTicks);
        xOffset = 18;
        yOffset = 90;

        //-1 because of border
        graphics.blit(TEXTURE, X()-1, Y()-1, xOffset, yOffset,  width, height);
        if(configFlag) {
            graphics.fill(X() - 1, Y() - 1, X()+width-1, Y()+height-1, color);
        }
    }
}