package igentuman.nc.client.gui.element.button;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
@OnlyIn(Dist.CLIENT)
public class NCImageButton extends Button {
   private final ResourceLocation resourceLocation;
   public int xTexStart;
   public int yTexStart;
   public int yDiffTex;
   private final int textureWidth;
   private final int textureHeight;

   public NCImageButton(int pX, int pY, int pWidth, int pHeight, int pXTexStart, int pYTexStart, ResourceLocation pResourceLocation) {
      this(pX, pY, pWidth, pHeight, pXTexStart, pYTexStart, pHeight, pResourceLocation, 256, 256, null);
   }


   public NCImageButton(int pX, int pY, int pWidth, int pHeight, int pXTexStart, int pYTexStart, int pYDiffTex, ResourceLocation pResourceLocation, int pTextureWidth, int pTextureHeight, ITextComponent pMessage) {
      super(pX, pY, null, pHeight);
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

   public void renderButton(MatrixStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTick) {
      //RenderSystem.setShader(GameRenderer::getPositionTexShader);
      Minecraft.getInstance().getTextureManager().bind(this.resourceLocation);
      int i = this.yTexStart;
/*      if (!this.isActive()) {
         i += this.yDiffTex * 2;
      } else if (this.isHovered()) {
         i += this.yDiffTex;
      }*/

      RenderSystem.enableDepthTest();
      blit(pMatrixStack, this.x, this.y, (float)this.xTexStart, (float)i, this.width, this.height, this.textureWidth, this.textureHeight);
      if (this.isHovered) {
         this.renderToolTip(pMatrixStack, pMouseX, pMouseY);
      }

   }
}