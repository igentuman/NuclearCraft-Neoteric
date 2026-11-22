package igentuman.nc.client.renderer;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import igentuman.nc.compat.iris.IrisCompat;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ExtractLevelRenderStateEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.lifecycle.ClientStoppingEvent;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Schedules the mod's full-screen effects inside the level's main pass.
 *
 * <ul>
 *   <li>{@link RenderLevelStageEvent.AfterOpaqueBlocks}: distortion lenses and anomaly glows.</li>
 *   <li>{@link RenderLevelStageEvent.AfterOpaqueFeatures}: black holes (after entities and block
 *       entities, before translucent terrain), then the scene depth is captured for the nuke.</li>
 *   <li>{@link RenderLevelStageEvent.AfterLevel}: the volumetric nuke cloud.</li>
 * </ul>
 *
 * <p>With an Iris shader pack active, the opaque feature stage captures scene depth and all effects
 * draw after Iris has composited the world. Effects sample {@link #sceneDepth} separately from
 * the main depth attachment they draw into.
 */
public final class ScenePostEffects {

    private static TextureTarget sceneDepth;
    private static LatePass latePass = LatePass.NONE;

    private enum LatePass {
        NONE, NUKE, SHADER_PACK
    }

    private ScenePostEffects() {
    }

    public static void register() {
        NeoForge.EVENT_BUS.register(ScenePostEffects.class);
    }

    public static void clear() {
        latePass = LatePass.NONE;
        DistortShader.clear();
        AnomalyShader.clear();
        BlackHoleShader.clear();
        NukeVolumeRenderer.clear();
    }

    /** Frees every GPU resource owned by the effects (resource reload / shutdown). */
    public static void close() {
        latePass = LatePass.NONE;
        DistortShader.close();
        AnomalyShader.close();
        BlackHoleShader.close();
        NukeVolumeRenderer.close();
        if (sceneDepth != null) sceneDepth.destroyBuffers();
        sceneDepth = null;
    }

    @SubscribeEvent
    public static void onClientStopping(ClientStoppingEvent event) {
        close();
    }

    @SubscribeEvent
    public static void onExtractLevel(ExtractLevelRenderStateEvent event) {
        NukeVolumeRenderer.extractLighting(event);
    }

    @SubscribeEvent
    public static void onAfterOpaqueBlocks(RenderLevelStageEvent.AfterOpaqueBlocks event) {
        latePass = LatePass.NONE;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || IrisCompat.shaderPackInUse()) return;
        if (DistortShader.hasWork() || AnomalyShader.hasWork()) {
            TextureTarget depth = captureDepth(mc.getMainRenderTarget());
            DistortShader.render(event, depth);
            AnomalyShader.render(event, depth);
        }
    }

    @SubscribeEvent
    public static void onAfterOpaqueFeatures(RenderLevelStageEvent.AfterOpaqueFeatures event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        if (IrisCompat.shaderPackInUse()) {
            if (DistortShader.hasWork() || AnomalyShader.hasWork() || BlackHoleShader.hasWork()
                    || NukeVolumeRenderer.hasFrame()) {
                captureDepth(mc.getMainRenderTarget());
                latePass = LatePass.SHADER_PACK;
            }
            return;
        }
        if (BlackHoleShader.hasWork()) {
            BlackHoleShader.render(event, captureDepth(mc.getMainRenderTarget()));
        }
        if (NukeVolumeRenderer.hasFrame()) {
            captureDepth(mc.getMainRenderTarget());
            latePass = LatePass.NUKE;
        }
    }

    @SubscribeEvent
    public static void onAfterLevel(RenderLevelStageEvent.AfterLevel event) {
        LatePass pass = latePass;
        latePass = LatePass.NONE;
        if (pass == LatePass.SHADER_PACK) {
            DistortShader.render(event, sceneDepth);
            AnomalyShader.render(event, sceneDepth);
            if (BlackHoleShader.hasWork()) {
                RenderTarget main = Minecraft.getInstance().getMainRenderTarget();
                // Start from opaque depth and carry the black hole's depth writes into the volume pass.
                main.copyDepthFrom(sceneDepth);
                BlackHoleShader.render(event, sceneDepth);
                if (NukeVolumeRenderer.hasFrame()) sceneDepth.copyDepthFrom(main);
            }
        }
        if (pass != LatePass.NONE) {
            NukeVolumeRenderer.render(event, sceneDepth);
        }
        BlackHoleShader.clear();
    }

    private static TextureTarget captureDepth(RenderTarget main) {
        if (sceneDepth == null || sceneDepth.useStencil != main.useStencil) {
            if (sceneDepth != null) sceneDepth.destroyBuffers();
            sceneDepth = new TextureTarget("nc scene depth", main.width, main.height, true, main.useStencil);
        } else if (sceneDepth.width != main.width || sceneDepth.height != main.height) {
            sceneDepth.resize(main.width, main.height);
        }
        sceneDepth.copyDepthFrom(main);
        return sceneDepth;
    }
}
