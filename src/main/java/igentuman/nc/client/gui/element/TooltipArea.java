package igentuman.nc.client.gui.element;

import igentuman.nc.client.gui.element.button.NCImageButton;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;

import static igentuman.nc.util.TextUtils.__;

public class TooltipArea extends NCGuiElement {
    protected AbstractContainerScreen<?> screen;
    protected NCImageButton btn;

    public TooltipArea(int xPos, int yPos, int width, int height) {
        super(xPos, yPos, 12, 12, Component.empty());
        x = xPos;
        y = yPos;
        btn = new NCImageButton(X(), Y(), width, height, 0, 178, 11, TEXTURE, null);
    }

    @Override
    public void renderButton(PoseStack graphics, int pMouseX, int pMouseY, float pPartialTick) {
         if (this.isHovered) {
            this.renderToolTip(graphics, pMouseX, pMouseY);
        }
    }

    public NCGuiElement setTooltipKey(String key) {
        tooltips.clear();
        tooltips.add(__(key));
        return this;
    }
}
