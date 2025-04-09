package igentuman.nc.client.renderer;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.shaders.Effect;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import com.mojang.blaze3d.platform.GlStateManager;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

public class DistortionEffectRenderer {
    private static final Minecraft minecraft = Minecraft.getInstance();
    private static float intensity = 0.5f;
    private static float radius = 5.0f;
    private static boolean enabled = false;

    public static void register() {
        MinecraftForge.EVENT_BUS.register(DistortionEffectRenderer.class);
    }

    public static void setDistortionEffect(float newIntensity, float newRadius) {
        intensity = newIntensity;
        radius = newRadius;
        enabled = intensity > 0;
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (!enabled || event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }

        ShaderInstance distortionShader = NCShaders.DISTORTION.get();
        if (distortionShader == null) return;
        ProfilerFiller profiler = minecraft.getProfiler();
        profiler.push("distortion " + distortionShader.getId());
        // Save original framebuffer state
        int originalFramebuffer = GlStateManager._getInteger(GL30.GL_FRAMEBUFFER_BINDING);

        // Bind main framebuffer texture
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();

        // Bind the main color texture to Sampler0
        RenderSystem.activeTexture(GL20.GL_TEXTURE0);
        RenderTarget mainTarget = minecraft.getMainRenderTarget();
        RenderSystem.bindTexture(mainTarget.getColorTextureId());

        // Set shader uniforms
        distortionShader.safeGetUniform("Intensity").set(intensity);
        distortionShader.safeGetUniform("Radius").set(radius);
        // GameTime is set automatically by Minecraft

        // Apply shader
        RenderSystem.setShader(() -> distortionShader);

        // Render fullscreen quad
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferbuilder.vertex(-1.0, -1.0, 0.0).uv(0.0F, 0.0F).endVertex();
        bufferbuilder.vertex(1.0, -1.0, 0.0).uv(1.0F, 0.0F).endVertex();
        bufferbuilder.vertex(1.0, 1.0, 0.0).uv(1.0F, 1.0F).endVertex();
        bufferbuilder.vertex(-1.0, 1.0, 0.0).uv(0.0F, 1.0F).endVertex();
        BufferUploader.drawWithShader(bufferbuilder.end());

        // Restore original state
        GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, originalFramebuffer);
        RenderSystem.disableBlend();
        RenderSystem.enableCull();
        profiler.pop();
    }
}