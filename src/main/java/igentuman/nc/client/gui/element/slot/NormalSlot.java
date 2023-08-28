package igentuman.nc.client.gui.element.slot;

import com.mojang.blaze3d.vertex.PoseStack;
import igentuman.nc.client.gui.element.NCGuiElement;
import igentuman.nc.client.gui.processor.side.SideConfigScreen;
import net.minecraft.client.Minecraft;

import static igentuman.nc.handler.sided.SlotModePair.SlotMode.*;

public class NormalSlot extends NCGuiElement {
    public int xOffset = 0;
    public int yOffset = 0;
    String type;
    public int color = OUTPUT.getColor();

    public NormalSlot(int[] pos, String pType)  {
        this(pos[0], pos[1], pType);
    }

    public NormalSlot(int xMin, int yMin, String pType)  {
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
        if(configFlag) {
            this.playDownSound(Minecraft.getInstance().getSoundManager());
            Minecraft.getInstance().forceSetScreen(new SideConfigScreen<>(screen, slotId));
            return true;
        } else {
           return super.onPress();
        }
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
        switch (type) {
            case "energy_upgrade" -> yOffset = 90;
            case "speed_upgrade" -> yOffset = 72;
            case "fission_cell" -> yOffset = 108;
            case "catalyst" -> yOffset = 126;
        }

        //-1 because of border
        blit(transform, X()-1, Y()-1, xOffset, yOffset,  18, 18);
        if(configFlag) {
            fill(transform, X() - 1, Y() - 1, X()+17, Y()+17, color);
        }
    }
}