package igentuman.nc.client.gui.element.slot;

import com.mojang.blaze3d.vertex.PoseStack;
import igentuman.nc.client.gui.element.NCGuiElement;
import igentuman.nc.client.gui.side.SideConfigWindowScreen;
import net.minecraft.client.Minecraft;

public class BigSlot extends NCGuiElement {
    public int xOffset = 70;
    public int yOffset = 0;
    String type;
    public int color = 0x804A0404;

    public BigSlot(int[] pos, String pType)  {
        this(pos[0], pos[1], pType);
    }

    public void onPress()
    {
        if(configFlag) {
            Minecraft.getInstance().forceSetScreen(new SideConfigWindowScreen<>(screen, slotId));
        }
    }

    public BigSlot(int xMin, int yMin, String pType)  {
        x = xMin;
        y = yMin;
        width = 26;
        height = 26;
        type = pType;
        if(type.contains("_in")) {
            color = 0x8004224a;
        }
    }

    @Override
    public void draw(PoseStack transform, int mX, int mY, float pTicks) {
        super.draw(transform, mX, mY, pTicks);
        if(type.contains("fluid")) {
            yOffset = 26;
        }

        //-5 because of padding
        blit(transform, X()-5, Y()-5, xOffset, yOffset,  26, 26);
        if(configFlag) {
            fill(transform, X() - 5, Y() - 5, X() + 21, Y() + 21, color);
        }
    }
}