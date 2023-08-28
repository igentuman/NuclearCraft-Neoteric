package igentuman.nc.client.gui.element.slot;

import com.mojang.blaze3d.vertex.PoseStack;
import igentuman.nc.client.gui.element.NCGuiElement;
import igentuman.nc.client.gui.processor.side.SideConfigScreen;
import net.minecraft.client.Minecraft;

import static igentuman.nc.handler.sided.SlotModePair.SlotMode.INPUT;
import static igentuman.nc.handler.sided.SlotModePair.SlotMode.OUTPUT;

public class HiddenSlot extends NCGuiElement {
    public int xOffset = 0;
    public int yOffset = 0;
    String type;
    public int color = OUTPUT.getColor();

    public HiddenSlot(int[] pos, String pType)  {
        this(pos[0], pos[1], pType);
    }

    public HiddenSlot(int xMin, int yMin, String pType)  {
        x = xMin;
        y = yMin;
        width = 18;
        height = 18;
        type = pType;
        if(type.contains("_in")) {
            color = INPUT.getColor();
        }
    }

    public boolean onPress()
    {
        return super.onPress();
    }

    @Override
    public void draw(PoseStack transform, int mX, int mY, float pTicks) {

    }
}