package igentuman.nc.client.gui.element.button;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.matrix.MatrixStack;
import igentuman.nc.client.gui.element.NCGuiElement;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.util.text.TranslationTextComponent;

public class Checkbox extends NCGuiElement {
    protected ContainerScreen screen;
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

    public Checkbox(int xPos, int yPos, ContainerScreen screen, boolean checked)  {
        super(xPos, yPos, 12, 12, new TranslationTextComponent(""));
        x = xPos;
        y = yPos;
        width = 12;
        height = 12;
        this.screen = screen;
        this.isChecked = checked;
        xTexStart = checked ? 11 : 0;
        btn = new NCImageButton(X(), Y(), 11, 11, xTexStart, 178, 11, TEXTURE);
    }

    @Override
    public void draw(MatrixStack transform, int mX, int mY, float pTicks) {
        super.draw(transform, mX, mY, pTicks);
        xTexStart = isChecked() ? 11 : 0;
        btn.xTexStart = xTexStart;
        btn.render(transform, mX, mY, pTicks);
    }

    @Override
    public void renderButton(MatrixStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTick) {
        int i = this.yTexStart;
        /*if (!this.isActive()) {
            i += this.yDiffTex * 2;
        } else */if (this.isHoveredOrFocused()) {
            i += this.yDiffTex;
        }
        RenderSystem.enableDepthTest();
        xTexStart = isChecked() ? 11 : 0;

        blit(pMatrixStack, this.x, this.y, (float)this.xTexStart, (float)i, this.width, this.height, this.textureWidth, this.textureHeight);
        if (this.isHovered) {
            this.renderToolTip(pMatrixStack, pMouseX, pMouseY);
        }
    }

    public NCGuiElement setChecked(boolean checked) {
        isChecked = checked;
        return this;
    }


    public NCGuiElement setTooltipKey(String key) {
        tooltips.clear();
        tooltips.add(new TranslationTextComponent(key));
        return this;
    }
}
