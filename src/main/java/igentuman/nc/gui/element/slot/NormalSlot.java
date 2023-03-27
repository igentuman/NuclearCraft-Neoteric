package igentuman.nc.gui.element.slot;

import com.mojang.blaze3d.vertex.PoseStack;
import igentuman.nc.gui.element.NCGuiElement;

public class NormalSlot extends NCGuiElement {
    public int xOffset = 0;
    public int yOffset = 0;
    String type = "items_in";

    public NormalSlot(int[] pos, String pType)  {
        this(pos[0], pos[1], pType);
        type = pType;
    }

    public NormalSlot(int xMin, int yMin, String pType)  {
        x = xMin;
        y = yMin;
        type = pType;
    }

    @Override
    public void draw(PoseStack transform, int mX, int mY, float pTicks) {
        super.draw(transform, mX, mY, pTicks);
        if(type.contains("fluid")) {
            xOffset = 18;
        }
        if(type.contains("_out")) {
            yOffset = 36;
        }
        if(type.equals("energy_upgrade")) {
            yOffset = 90;
        }
        if(type.equals("speed_upgrade")) {
            yOffset = 72;
        }
        //-1 because of border
        blit(transform, X()-1, Y()-1, xOffset, yOffset,  18, 18);
    }
}