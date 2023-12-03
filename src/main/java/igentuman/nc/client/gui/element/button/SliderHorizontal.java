package igentuman.nc.client.gui.element.button;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import igentuman.nc.NuclearCraft;
import igentuman.nc.client.gui.element.NCGuiElement;
import igentuman.nc.client.gui.processor.side.SideConfigSlotSelectionScreen;
import igentuman.nc.network.toServer.PacketGuiButtonPress;
import igentuman.nc.network.toServer.PacketSliderChanged;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

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

    public SliderHorizontal(int xPos, int yPos, int width, AbstractContainerScreen<?> screen, BlockPos pos)  {
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

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        if(X() <= pMouseX && pMouseX < X() + width && Y()-1 <= pMouseY && pMouseY < Y() + height+1) {
            isPressed = true;
            return isPressed;
        }
        mouseReleased(pMouseX, pMouseY, pButton);
        return false;
    }

    @Override
    public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
        isPressed = false;
        return isPressed;
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
            btn.x = x;
            btn.x = Math.min(maxX, btn.x);
            btn.x = Math.max(minX, btn.x);
            int xpos = maxX-btn.x;
            int ratio = 100;
            if(xpos > 0) {
                ratio = 100-xpos*100/(width - 3);
            }
            NuclearCraft.packetHandler().sendToServer(new PacketSliderChanged(pos, ratio, 0));
        }
    }

    public void drawSlide(PoseStack transform) {
        RenderSystem.setShaderTexture(0, TEXTURE);
        blit(transform, this.x+ screen.getGuiLeft(), this.y+2+screen.getGuiTop(), 5, 175, this.width, 3, this.textureWidth, this.textureHeight);
    }

    @Override
    public void draw(PoseStack transform, int mX, int mY, float pTicks) {
        super.draw(transform, mX, mY, pTicks);
        btn.xTexStart = xTexStart;
        drawSlide(transform);
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

        blit(pPoseStack, this.x, this.y, (float)this.xTexStart, (float)i, this.width, this.height, this.textureWidth, this.textureHeight);
        if (this.isHovered) {
            this.renderToolTip(pPoseStack, pMouseX, pMouseY);
        }
    }


    public NCGuiElement setTooltipKey(String key) {
        tooltips.clear();
        tooltips.add(Component.translatable(key));
        return this;
    }

    public void slideTo(int rfAmplifiersPowerRatio) {
        btn.x = startX+screen.getGuiLeft()+width*rfAmplifiersPowerRatio/100;
    }
}
