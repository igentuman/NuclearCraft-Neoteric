package igentuman.nc.client.renderer;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import igentuman.nc.compat.iris.IrisCompat;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

public final class ScenePostEffects {

    private static TextureTarget sceneDepth;
    private static boolean captured;

    private ScenePostEffects() {
    }

    public static void register() {
        NeoForge.EVENT_BUS.register(ScenePostEffects.class);
    }

    public static void clear() {
        captured = false;
        DistortShader.clear();
        AnomalyShader.clear();
        BlackHoleShader.clear();
        NukeVolumeRenderer.clear();
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        RenderLevelStageEvent.Stage stage = event.getStage();
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        if (!IrisCompat.shaderPackInUse()) {
            if (stage == RenderLevelStageEvent.Stage.AFTER_CUTOUT_BLOCKS) {
                captured = false;
                DistortShader.render(event);
                AnomalyShader.render(event);
            } else if (stage == RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES) {
                BlackHoleShader.render(event);
                captured = NukeVolumeRenderer.hasWork() && captureDepth(mc.getMainRenderTarget());
            } else if (stage == RenderLevelStageEvent.Stage.AFTER_LEVEL && captured) {
                captured = false;
                NukeVolumeRenderer.render(event, sceneDepth.getDepthTextureId());
            }
            return;
        }

        if (stage == RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES) {
            captured = false;
            if (DistortShader.hasWork() || AnomalyShader.hasWork() || BlackHoleShader.hasWork()
                    || NukeVolumeRenderer.hasWork()) {
                captured = captureDepth(mc.getMainRenderTarget());
            }
        } else if (stage == RenderLevelStageEvent.Stage.AFTER_LEVEL) {
            if (captured) {
                captured = false;
                RenderTarget main = mc.getMainRenderTarget();
                main.copyDepthFrom(sceneDepth);
                main.bindWrite(false);
                DistortShader.render(event);
                AnomalyShader.render(event);
                BlackHoleShader.render(event);
                main.bindWrite(true);
                NukeVolumeRenderer.render(event, main.getDepthTextureId());
            }
            BlackHoleShader.clear();
        }
    }

    private static boolean captureDepth(RenderTarget main) {
        if (sceneDepth == null) {
            sceneDepth = new TextureTarget(main.width, main.height, true, Minecraft.ON_OSX);
        } else if (sceneDepth.width != main.width || sceneDepth.height != main.height) {
            sceneDepth.resize(main.width, main.height, Minecraft.ON_OSX);
        }
        matchDepthFormat(sceneDepth, main);
        sceneDepth.copyDepthFrom(main);
        main.bindWrite(false);
        return true;
    }

    static void matchDepthFormat(RenderTarget target, RenderTarget main) {
        int mainFormat = depthInternalFormat(main);
        if (mainFormat == 0 || mainFormat == depthInternalFormat(target)) return;
        boolean stencil = mainFormat == GL30.GL_DEPTH24_STENCIL8 || mainFormat == GL30.GL_DEPTH32F_STENCIL8;
        int pixelFormat = stencil ? GL30.GL_DEPTH_STENCIL : GL11.GL_DEPTH_COMPONENT;
        int pixelType = mainFormat == GL30.GL_DEPTH24_STENCIL8 ? GL30.GL_UNSIGNED_INT_24_8
                : mainFormat == GL30.GL_DEPTH32F_STENCIL8 ? GL30.GL_FLOAT_32_UNSIGNED_INT_24_8_REV
                : mainFormat == GL30.GL_DEPTH_COMPONENT32F ? GL11.GL_FLOAT
                : GL11.GL_UNSIGNED_INT;
        GlStateManager._bindTexture(target.getDepthTextureId());
        GlStateManager._texImage2D(GL11.GL_TEXTURE_2D, 0, mainFormat, target.width, target.height, 0, pixelFormat, pixelType, null);
        GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, target.frameBufferId);
        GlStateManager._glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_STENCIL_ATTACHMENT, GL11.GL_TEXTURE_2D, 0, 0);
        GlStateManager._glFramebufferTexture2D(GL30.GL_FRAMEBUFFER,
                stencil ? GL30.GL_DEPTH_STENCIL_ATTACHMENT : GL30.GL_DEPTH_ATTACHMENT,
                GL11.GL_TEXTURE_2D, target.getDepthTextureId(), 0);
        GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
    }

    private static int depthInternalFormat(RenderTarget target) {
        int id = target.getDepthTextureId();
        if (id <= 0) return 0;
        GlStateManager._bindTexture(id);
        return GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_INTERNAL_FORMAT);
    }
}
