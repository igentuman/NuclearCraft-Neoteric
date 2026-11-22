package igentuman.nc.client.renderer;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.shaders.UniformType;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4fc;
import org.joml.Vector4f;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static igentuman.nc.NuclearCraft.rl;

public class DistortShader {

    public static boolean enabled = true;

    static final RenderPipeline PIPELINE = PostEffectSupport.pipeline(rl("pipeline/distort"), rl("post/distort"))
            .withSampler("InSampler")
            .withSampler("DepthSampler")
            .withUniform("DistortConfig", UniformType.UNIFORM_BUFFER)
            .build();

    private static final Map<Object, DistortionSource> SOURCES = new ConcurrentHashMap<>();
    private static final PostEffectSupport.Uniforms UNIFORMS = new PostEffectSupport.Uniforms("nc distort config", 64);
    private static TextureTarget scratch;

    public static boolean hasWork() {
        if (!enabled || SOURCES.isEmpty()) return false;
        SOURCES.entrySet().removeIf(e -> !e.getValue().isActive());
        return !SOURCES.isEmpty();
    }

    public static void add(Object key, DistortionSource source) {
        SOURCES.put(key, source);
    }

    public static void remove(Object key) {
        SOURCES.remove(key);
    }

    public static boolean contains(Object key) {
        return SOURCES.containsKey(key);
    }

    public static void clear() {
        SOURCES.clear();
    }

    public interface DistortionSource {

        /** World-space center of the distortion. Return {@code null} to skip this frame. */
        Vec3 position(float partialTick);

        /** Base lens radius in pixels (scaled down with distance at render time). */
        default float radius() {
            return 150F;
        }

        /** Magnification factor (> 1 magnifies the center). */
        default float magnification() {
            return 5.8F;
        }

        default boolean isActive() {
            return true;
        }

        /** Beyond this distance (blocks) from the player the effect is skipped. */
        default double maxDistance() {
            return 64D;
        }
    }

    static void render(RenderLevelStageEvent event, TextureTarget sceneDepth) {
        if (!hasWork()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        float partialTick = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        RenderTarget main = mc.getMainRenderTarget();
        Matrix4fc view = event.getModelViewMatrix();
        Matrix4fc projection = event.getLevelRenderState().cameraRenderState.projectionMatrix;
        Vec3 camPos = event.getLevelRenderState().cameraRenderState.pos;

        for (DistortionSource source : SOURCES.values()) {
            Vector4f screen = processSource(mc, view, projection, camPos, source, partialTick);
            if (screen == null) continue;
            // Each source reads a copy of the current frame and writes the lens back into main.
            scratch = PostEffectSupport.ensureTarget(scratch, "nc distort scratch", main, false);
            PostEffectSupport.copyColor(main, scratch);
            float distanceFactor = screen.z;
            GpuBuffer ubo = UNIFORMS.write(b -> b
                    .putVec2(screen.x, screen.y)
                    .putVec2(0.2F, 0.0F)
                    .putVec2(source.radius() * distanceFactor, source.magnification())
                    .putFloat(screen.w));
            new PostEffectSupport.Draw("nc distort", PIPELINE)
                    .sampler("InSampler", scratch.getColorTextureView())
                    .sampler("DepthSampler", sceneDepth.getDepthTextureView())
                    .uniform("DistortConfig", ubo)
                    .into(main.getColorTextureView(), null);
        }
    }

    private static Vector4f processSource(Minecraft mc, Matrix4fc view, Matrix4fc projection, Vec3 camPos,
                                          DistortionSource source, float partialTick) {
        if (!source.isActive()) return null;
        Vec3 pos = source.position(partialTick);
        if (pos == null) return null;

        double distance = mc.player.position().distanceTo(pos);
        if (distance > source.maxDistance()) return null;

        Vector4f clip = PostEffectSupport.project((float) (pos.x - camPos.x), (float) (pos.y - camPos.y),
                (float) (pos.z - camPos.z), view, projection);
        if (clip == null) return null;

        float blurX = clip.x * 0.5F + 0.5F;
        float blurY = clip.y * 0.5F + 0.5F;
        float margin = 0.1F;
        if (blurX < -margin || blurX > 1.0F + margin || blurY < -margin || blurY > 1.0F + margin) return null;

        float distanceFactor = (float) (7D / distance);
        return new Vector4f(blurX, blurY, distanceFactor, clip.w);
    }

    static void close() {
        UNIFORMS.close();
        if (scratch != null) scratch.destroyBuffers();
        scratch = null;
    }
}
