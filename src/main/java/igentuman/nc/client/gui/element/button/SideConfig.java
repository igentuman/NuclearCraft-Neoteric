package igentuman.nc.client.gui.element.button;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import igentuman.nc.NuclearCraft;
import igentuman.nc.client.gui.element.NCGuiElement;
import igentuman.nc.client.gui.side.SideConfigWindowScreen;
import igentuman.nc.network.toServer.PacketSideConfigToggle;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;

public class SideConfig extends NCGuiElement {
    protected SideConfigWindowScreen screen;
    private int xTexStart;
    private int yTexStart;
    private int textureWidth;
    private int textureHeight;
    protected SideBtn btn;
    private int yDiffTex;
    private int slotId;
    private int direction;

    public SideConfig(int xPos, int yPos, int slotId, SideConfigWindowScreen screen, int direction)  {
        x = xPos;
        y = yPos;
        width = 18;
        height = 18;
        this.screen = screen;
        xTexStart = 108;
        this.slotId = slotId;
        btn = new SideBtn(X(), Y(), pButton -> {
            NuclearCraft.packetHandler().sendToServer(new PacketSideConfigToggle(screen.getPosition(), slotId, direction));
        });

        this.direction = direction;
    }

    public void onPress() {
        btn.onPress();
    }

    @Override
    public void draw(PoseStack transform, int mX, int mY, float pTicks) {
        super.draw(transform, mX, mY, pTicks);
        //btn.render(transform, mX, mY, pTicks);
        renderButton(transform, mX, mY, pTicks);
    }
    public void getXOffset() {
        switch (screen.getSlotMode(direction, slotId)) {
            case INPUT:
                xTexStart = 126;
                break;
            case OUTPUT:
                xTexStart = 144;
                break;
            case DISABLED:
                xTexStart = 162;
                break;
            default:
                xTexStart = 108;
                break;
        }
    }

    @Override
    public void renderButton(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        int i = this.yTexStart;
        if(this.isHoveredOrFocused()) {
            i += this.yDiffTex;
        }
        RenderSystem.enableDepthTest();
        getXOffset();
        btn.setXOffset(xTexStart);
        btn.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
        if (this.isHovered) {
            this.renderToolTip(pPoseStack, pMouseX, pMouseY);
        }
    }

    public static class SideBtn extends ImageButton {
        private int xTexStart;
        private int yTexStart;
        private ResourceLocation resourceLocation;
        private int yDiffTex;
        private int textureWidth;
        private int textureHeight;

        public SideBtn(int x, int y, OnPress onPress) {
            super(x, y, 18, 18, 108, 220, 18, TEXTURE, onPress);
            xTexStart = 108;
            yTexStart = 220;
            yDiffTex = 18;
            textureWidth = 256;
            textureHeight = 256;
            resourceLocation = TEXTURE;
        }

        public void setXOffset(int xTexStart) {
            this.xTexStart = xTexStart;
        }

        public void renderButton(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, this.resourceLocation);
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
    }
}
