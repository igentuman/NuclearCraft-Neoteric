package igentuman.nc.client.gui.element.button;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraft.client.gui.components.Button;
@OnlyIn(Dist.CLIENT)
public class NCImageButton extends Button {
   private final ResourceLocation resourceLocation;
   public int xTexStart;
   public int yTexStart;
   public int yDiffTex;
   private final int textureWidth;
   private final int textureHeight;

   public NCImageButton(int pX, int pY, int pWidth, int pHeight, int pXTexStart, int pYTexStart, ResourceLocation pResourceLocation, Button.OnPress pOnPress) {
      this(pX, pY, pWidth, pHeight, pXTexStart, pYTexStart, pHeight, pResourceLocation, 256, 256, null);
   }

   public NCImageButton(int pX, int pY, int pWidth, int pHeight, int pXTexStart, int pYTexStart, int pYDiffTex, ResourceLocation pResourceLocation, Button.OnPress pOnPress) {
      this(pX, pY, pWidth, pHeight, pXTexStart, pYTexStart, pYDiffTex, pResourceLocation, 256, 256, pOnPress);
   }

   public NCImageButton(int pX, int pY, int pWidth, int pHeight, int pXTexStart, int pYTexStart, int pYDiffTex, ResourceLocation pResourceLocation, int pTextureWidth, int pTextureHeight, Button.OnPress pOnPress) {
      this(pX, pY, pWidth, pHeight, pXTexStart, pYTexStart, pYDiffTex, pResourceLocation, pTextureWidth, pTextureHeight, pOnPress, CommonComponents.GUI_NO);
   }

   public NCImageButton(int pX, int pY, int pWidth, int pHeight, int pXTexStart, int pYTexStart, int pYDiffTex, ResourceLocation pResourceLocation, int pTextureWidth, int pTextureHeight, Button.OnPress pOnPress, Component pMessage) {
      this(pX, pY, pWidth, pHeight, pXTexStart, pYTexStart, pYDiffTex, pResourceLocation, pTextureWidth, pTextureHeight, pOnPress, NO_TOOLTIP, pMessage);
   }

   public NCImageButton(int pX, int pY, int pWidth, int pHeight, int pXTexStart, int pYTexStart, int pYDiffTex, ResourceLocation pResourceLocation, int pTextureWidth, int pTextureHeight, Button.OnPress pOnPress, Button.OnTooltip pOnTooltip, Component pMessage) {
      super(pX, pY, pWidth, pHeight, pMessage, pOnPress, pOnTooltip);
      this.textureWidth = pTextureWidth;
      this.textureHeight = pTextureHeight;
      this.xTexStart = pXTexStart;
      this.yTexStart = pYTexStart;
      this.yDiffTex = pYDiffTex;
      this.resourceLocation = pResourceLocation;
   }

   public void setPosition(int pX, int pY) {
      this.x = pX;
      this.y = pY;
   }

   public void renderButton(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
      RenderSystem.setShader(GameRenderer::getPositionTexShader);
      RenderSystem.setShaderTexture(0, this.resourceLocation);
      int i = this.yTexStart;
      if (!this.isActive()) {
         i += this.yDiffTex * 2;
      } else if (this.isHovered()) {
         i += this.yDiffTex;
      }

      RenderSystem.enableDepthTest();
      blit(pPoseStack, this.x, this.y, (float)this.xTexStart, (float)i, this.width, this.height, this.textureWidth, this.textureHeight);
      if (this.isHovered) {
         this.renderToolTip(pPoseStack, pMouseX, pMouseY);
      }

   }
}