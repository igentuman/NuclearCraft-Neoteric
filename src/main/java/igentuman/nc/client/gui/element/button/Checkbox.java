package igentuman.nc.client.gui.element.button;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import igentuman.nc.client.gui.element.NCGuiElement;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

public class Checkbox extends NCGuiElement {
    protected AbstractContainerScreen screen;
    private int xTexStart;
    private int yTexStart;
    private int textureWidth;
    private int textureHeight;
    protected NCImageButton btn;
    private int yDiffTex;


    public boolean isChecked() {
        return isChecked;
    }

    private boolean isChecked = false;

    public Checkbox(int xPos, int yPos, AbstractContainerScreen screen, boolean checked)  {
        x = xPos;
        y = yPos;
        width = 12;
        height = 12;
        this.screen = screen;
        this.isChecked = checked;
        xTexStart = checked ? 11 : 0;
        btn = new NCImageButton(X(), Y(), 11, 11, xTexStart, 178, 11, TEXTURE, null);
    }

    @Override
    public void draw(PoseStack transform, int mX, int mY, float pTicks) {
        super.draw(transform, mX, mY, pTicks);
        xTexStart = isChecked() ? 11 : 0;
        btn.xTexStart = xTexStart;
        btn.render(transform, mX, mY, pTicks);
    }

    @Override
    public void renderButton(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        int i = this.yTexStart;
        if (!this.isActive()) {
            i += this.yDiffTex * 2;
        } else if (this.isHoveredOrFocused()) {
            i += this.yDiffTex;
        }
        RenderSystem.enableDepthTest();
        xTexStart = isChecked() ? 11 : 0;

        blit(pPoseStack, this.x, this.y, (float)this.xTexStart, (float)i, this.width, this.height, this.textureWidth, this.textureHeight);
        if (this.isHovered) {
            this.renderToolTip(pPoseStack, pMouseX, pMouseY);
        }
    }

    public NCGuiElement setChecked(boolean checked) {
        isChecked = checked;
        return this;
    }


    public NCGuiElement setTooltipKey(String key) {
        tooltips.clear();
        tooltips.add(new TranslatableComponent(key));
        return this;
    }
}
