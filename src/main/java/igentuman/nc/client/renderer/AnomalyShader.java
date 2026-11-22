package igentuman.nc.client.renderer;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.shaders.UniformType;
import igentuman.nc.config.Common;
import igentuman.nc.entity.anomaly.AnomalyEntity;
import igentuman.nc.entity.anomaly.AnomalyType;
import igentuman.nc.entity.anomaly.GravitationalAnomalyEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4fc;
import org.joml.Vector4f;

import static igentuman.nc.NuclearCraft.rl;

public class AnomalyShader {

    private static final double MAX_DISTANCE = 96.0D;
    static final RenderPipeline PIPELINE = PostEffectSupport.pipeline(rl("pipeline/anomaly"), rl("post/anomaly"))
            .withSampler("InSampler")
            .withSampler("DepthSampler")
            .withUniform("AnomalyConfig", UniformType.UNIFORM_BUFFER)
            .build();

    private static final PostEffectSupport.Uniforms UNIFORMS = new PostEffectSupport.Uniforms("nc anomaly config", 48);
    private static TextureTarget scratch;

    public static boolean hasWork() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null
                || !Common.ANOMALY_CONFIG.SHADER.get()) {
            return false;
        }
        for (Entity e : mc.level.entitiesForRendering()) {
            if (e instanceof AnomalyEntity && e.distanceToSqr(mc.player) <= MAX_DISTANCE * MAX_DISTANCE) {
                return true;
            }
        }
        return false;
    }

    public static void clear() {
    }

    private static float baseRadius(AnomalyType type) {
        return switch (type) {
            case GRAVITATIONAL -> 260.0F;
            case ELECTRIC -> 300.0F;
            case RADIOACTIVE -> 300.0F;
            case BURNING -> 200.0F;
            case PSYCHO -> 300.0F;
            case TELEPORTING -> 300.0F;
        };
    }

    private static float magnification(AnomalyType type) {
        return type == AnomalyType.GRAVITATIONAL ? 10.025F : 1.0F;
    }

    static void render(RenderLevelStageEvent event, TextureTarget sceneDepth) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            return;
        }
        if (!Common.ANOMALY_CONFIG.SHADER.get()) {
            return;
        }

        float partialTick = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        RenderTarget main = mc.getMainRenderTarget();
        Matrix4fc view = event.getModelViewMatrix();
        Matrix4fc projection = event.getLevelRenderState().cameraRenderState.projectionMatrix;
        Vec3 camPos = event.getLevelRenderState().cameraRenderState.pos;
        for (Entity e : mc.level.entitiesForRendering()) {
            if (!(e instanceof AnomalyEntity anomaly)) {
                continue;
            }
            GpuBuffer ubo = processAnomaly(mc, view, projection, camPos, anomaly, partialTick);
            if (ubo == null) {
                continue;
            }
            scratch = PostEffectSupport.ensureTarget(scratch, "nc anomaly scratch", main, false);
            PostEffectSupport.copyColor(main, scratch);
            new PostEffectSupport.Draw("nc anomaly", PIPELINE)
                    .sampler("InSampler", scratch.getColorTextureView())
                    .sampler("DepthSampler", sceneDepth.getDepthTextureView())
                    .uniform("AnomalyConfig", ubo)
                    .into(main.getColorTextureView(), null);
        }
    }

    private static GpuBuffer processAnomaly(Minecraft mc, Matrix4fc view, Matrix4fc projection, Vec3 camPos,
                                            AnomalyEntity anomaly, float partialTick) {
        Vec3 pos = anomaly.position().add(0.0D, 1.2D, 0.0D);
        double distance = Math.sqrt(mc.player.position().distanceToSqr(pos));
        if (distance > MAX_DISTANCE) {
            return null;
        }

        Vector4f p = PostEffectSupport.project((float) (pos.x - camPos.x), (float) (pos.y - camPos.y),
                (float) (pos.z - camPos.z), view, projection);
        if (p == null) return null;

        float blurX = p.x * 0.5f + 0.5f;
        float blurY = p.y * 0.5f + 0.5f;
        float margin = 0.25f;
        if (blurX < -margin || blurX > 1.0f + margin || blurY < -margin || blurY > 1.0f + margin) return null;

        float normalizedDepth = p.w;
        float distanceFactor = (float)(10.0f / Math.max(distance, 1.0));

        AnomalyType type = anomaly.getAnomalyType();
        float radius = baseRadius(type) * distanceFactor;
        if (anomaly instanceof GravitationalAnomalyEntity grav) {
            radius *= (float) grav.massFactor();
        }
        radius = Math.min(radius, 700.0f);
        int color = type.color();
        long nanoMod = System.nanoTime() % 1_000_000_000_000L;
        float time = (float) (nanoMod / 1_000_000_000.0);
        float intensity = Math.max(0.2f, anomaly.getIntensity());
        float finalRadius = radius;
        return UNIFORMS.write(b -> b
                .putVec4(((color >> 16) & 0xFF) / 255.0f, ((color >> 8) & 0xFF) / 255.0f, (color & 0xFF) / 255.0f, time)
                .putVec2(blurX, blurY)
                .putVec2(finalRadius, magnification(type))
                .putFloat(normalizedDepth)
                .putFloat(intensity)
                .putInt(type.ordinal())
                .putFloat(1.0f));
    }

    static void close() {
        UNIFORMS.close();
        if (scratch != null) scratch.destroyBuffers();
        scratch = null;
    }
}
