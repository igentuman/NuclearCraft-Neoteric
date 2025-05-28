package igentuman.nc.client.gui.element.button;

import com.mojang.blaze3d.systems.RenderSystem;
import igentuman.nc.NuclearCraft;
import igentuman.nc.client.gui.element.NCGuiElement;
import igentuman.nc.network.toServer.PacketSliderChanged;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

import static igentuman.nc.util.TextUtils.__;

public class SliderHorizontal extends NCGuiElement {
    protected AbstractContainerScreen screen;
    private int xTexStart;
    private int yTexStart;
    private int textureWidth = 256;
    private int textureHeight = 256;
    protected NCImageButton btn;
    private int yDiffTex;
    private boolean isPressed = false;
    private BlockPos pos;
    private int startX;
    private int buttonId = 0;

    public SliderHorizontal(int xPos, int yPos, int width, AbstractContainerScreen<?> screen, BlockPos pos)  {
        super(xPos, yPos, width, 12, Component.empty());
        x = xPos;
        y = yPos;
        startX = x;
        this.pos = pos;
        this.width = width;
        height = 12;
        this.screen = screen;
        xTexStart = 0;
        btn = new NCImageButton(X(), Y(), 4, 8, 0, 169, -9, TEXTURE, pButton -> {

        });
    }
    public SliderHorizontal(int xPos, int yPos, int width, AbstractContainerScreen<?> screen, BlockPos pos, int btnId)  {
        this(xPos, yPos, width, screen, pos);
        buttonId = btnId;
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        if(X() - 1 <= pMouseX && pMouseX < X() + width + 1 && Y()-2 <= pMouseY && pMouseY < Y() + height+2) {
            isPressed = true;
            return isPressed;
        }
        mouseReleased(pMouseX, pMouseY, pButton);
        return false;
    }

    @Override
    public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
        isPressed = false;
        return false;
    }

    @Override
    public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
        mouseMove((int)pMouseX, (int)pMouseY);
        return false;
    }

    public void mouseMove(int x, int y) {
        if (isPressed) {
            int maxX = startX+screen.getGuiLeft()+width-3;
            int minX = startX+screen.getGuiLeft();
            x = Math.min(maxX, x);
            x = Math.max(minX, x);
            btn.setX(x);
            int xpos = maxX-x;
            int ratio = 100;
            if(xpos > 0) {
                ratio = 100-xpos*100/(width - 3);
            }
            NuclearCraft.packetHandler().sendToServer(new PacketSliderChanged(pos, ratio, buttonId));
        }
    }

    public void drawSlide(GuiGraphics graphics) {
        RenderSystem.setShaderTexture(0, TEXTURE);
        graphics.blit(TEXTURE, this.x+ screen.getGuiLeft(), this.y+2+screen.getGuiTop(), 5, 175, this.width, 3, this.textureWidth, this.textureHeight);
    }

    @Override
    public void draw(GuiGraphics graphics, int mX, int mY, float pTicks) {
        super.draw(graphics, mX, mY, pTicks);
        btn.xTexStart = xTexStart;
        drawSlide(graphics);
        btn.render(graphics, mX, mY, pTicks);
    }

    @Override
    public void renderButton(GuiGraphics graphics, int pMouseX, int pMouseY, float pPartialTick) {
        int i = this.yTexStart;
        if (!this.isActive()) {
            i += this.yDiffTex * 2;
        } else if (this.isHoveredOrFocused()) {
            i += this.yDiffTex;
        }
        RenderSystem.enableDepthTest();

        graphics.blit(TEXTURE, this.x, this.y, (float)this.xTexStart, (float)i, this.width, this.height, this.textureWidth, this.textureHeight);
        if (this.isHovered) {
            this.renderToolTip(graphics, pMouseX, pMouseY);
        }
    }


    public NCGuiElement setTooltipKey(String key) {
        tooltips.clear();
        tooltips.add(__(key));
        return this;
    }

    public void slideTo(int ratio) {
        btn.setX(startX+screen.getGuiLeft()+width*ratio/100);
    }
}
