package igentuman.nc.client.gui.element.slot;

import com.mojang.blaze3d.vertex.PoseStack;
import igentuman.nc.client.gui.element.NCGuiElement;
import igentuman.nc.client.gui.processor.side.SideConfigScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

import static igentuman.nc.handler.sided.SlotModePair.SlotMode.*;

public class BigSlot extends NCGuiElement {
    public int xOffset = 70;
    public int yOffset = 0;
    String type;
    public int color = OUTPUT.getColor();

    public BigSlot(int[] pos, String pType)  {
        this(pos[0], pos[1], pType);
    }

    public boolean onPress()
    {
        if(configFlag) {
            Minecraft.getInstance().forceSetScreen(new SideConfigScreen<>(screen, slotId));
            return true;
        }
        return false;
    }

    public BigSlot(int xMin, int yMin, String pType)  {
        super(xMin, yMin, 26, 26, null);
        x = xMin;
        y = yMin;
        width = 26;
        height = 26;
        type = pType;
        if(type.contains("_in")) {
            color = INPUT.getColor();
        }
    }

    @Override
    public void draw(GuiGraphics graphics, int mX, int mY, float pTicks) {
        super.draw(graphics, mX, mY, pTicks);
        if(type.contains("fluid")) {
            yOffset = 26;
        }

        //-5 because of padding
        graphics.blit(TEXTURE, X()-5, Y()-5, xOffset, yOffset,  26, 26);
        if(configFlag) {
            graphics.fill(X() - 5, Y() - 5, X() + 21, Y() + 21, color);
        }
    }
}