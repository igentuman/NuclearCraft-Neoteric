package igentuman.nc.gui.element.slot;

import com.mojang.blaze3d.vertex.PoseStack;
import igentuman.nc.gui.element.NCGuiElement;

public class BigSlot extends NCGuiElement {
    public int xOffset = 70;
    public int yOffset = 0;
    String type = "items_in";

    public BigSlot(int[] pos, String pType)  {
        this(pos[0], pos[1], pType);
        type = pType;
    }

    public BigSlot(int xMin, int yMin, String pType)  {
        x = xMin;
        y = yMin;
        type = pType;
    }

    @Override
    public void draw(PoseStack transform) {
        super.draw(transform);
        if(type.contains("fluid")) {
            yOffset = 26;
        }

        //-1 because of border
        blit(transform, X()-5, Y()-5, xOffset, yOffset,  26, 26);
    }
}