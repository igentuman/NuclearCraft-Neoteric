package igentuman.nc.client.renderer;

import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

public class HdrTextureTarget extends TextureTarget {

    public HdrTextureTarget(int width, int height, boolean useDepth, boolean clearError) {
        super(width, height, useDepth, clearError);
    }

    @Override
    public void createBuffers(int width, int height, boolean clearError) {
        super.createBuffers(width, height, clearError);
        GlStateManager._bindTexture(getColorTextureId());
        GlStateManager._texImage2D(GL11.GL_TEXTURE_2D, 0, GL30.GL_RGBA16F, width, height, 0,
                GL11.GL_RGBA, GL11.GL_FLOAT, null);
        GlStateManager._bindTexture(0);
        GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, frameBufferId);
        checkStatus();
        GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
    }
}
