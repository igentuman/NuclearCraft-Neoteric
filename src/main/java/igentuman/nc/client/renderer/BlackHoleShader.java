package igentuman.nc.client.renderer;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.shaders.UniformType;
import igentuman.nc.NuclearCraft;
import igentuman.nc.block_entity.kugelblitz.BlackHoleBE;
import igentuman.nc.config.Multiblocks;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4fc;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static igentuman.nc.NuclearCraft.rl;

public final class BlackHoleShader {

    public record Source(Vec3 center, float formationAge, float blackholeScale) {
    }

    static final RenderPipeline PIPELINE = PostEffectSupport.pipeline(rl("pipeline/black_hole"), rl("post/black_hole"))
            .withVertexShader(rl("post/black_hole"))
            .withSampler("InSampler")
            .withSampler("DepthSampler")
            .withUniform("Projection", UniformType.UNIFORM_BUFFER)
            .withUniform("BlackHoleConfig", UniformType.UNIFORM_BUFFER)
            .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, true))
            .build();

    private static final List<Source> SOURCES = new ArrayList<>();
    private static final PostEffectSupport.Uniforms UNIFORMS =
            new PostEffectSupport.Uniforms("nc black hole config", 3 * 4 * Float.BYTES);
    private static TextureTarget scratch;
    private static boolean failed;

    private BlackHoleShader() {}

    public static void clear() {
        SOURCES.clear();
    }

    public static boolean hasWork() {
        return !failed && Multiblocks.kugelblitzBlackholeShader && !SOURCES.isEmpty();
    }

    /** Queues a black hole for this frame; returns {@code false} when the caller must draw the geometry fallback. */
    public static boolean submit(Source source) {
        if (failed || !Multiblocks.kugelblitzBlackholeShader) return false;
        SOURCES.add(source);
        return true;
    }

    /** Convenience for renderers that still hold the block entity at extraction time. */
    public static boolean submit(BlackHoleBE be) {
        Minecraft mc = Minecraft.getInstance();
        if (failed || !Multiblocks.kugelblitzBlackholeShader || be.getLevel() != mc.level) return false;
        float partialTick = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        return submit(new Source(Vec3.atCenterOf(be.getBlockPos()), be.getFormationAge(partialTick), be.getBlackholeScale()));
    }

    static void render(RenderLevelStageEvent event, TextureTarget sceneDepth) {
        // Preserve opaque occlusion, then let chamber glass blend over the completed effect.
        Minecraft mc = Minecraft.getInstance();
        if (failed || mc.level == null || !Multiblocks.kugelblitzBlackholeShader) {
            clear();
            return;
        }
        if (SOURCES.isEmpty()) return;

        RenderTarget main = mc.getMainRenderTarget();
        Vec3 camera = event.getLevelRenderState().cameraRenderState.pos;
        float partialTick = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        Matrix4fc view = event.getModelViewMatrix();
        SOURCES.sort(Comparator.comparingDouble(source -> -camera.distanceToSqr(source.center())));

        try {
            for (Source source : SOURCES) {
                Vec3 relative = source.center().subtract(camera);
                if (relative.lengthSqr() > 128D * 128D) continue;
                Vector3f center = view.transformPosition(relative.toVector3f(), new Vector3f());

                float age = source.formationAge();
                float formation = Math.min(age / 2.5F, 1F);
                float growth = Math.min(age / 0.45F, 1F);
                float radius = 0.7F * (0.85F + source.blackholeScale() * 0.5F) * (0.25F + 0.75F * growth);

                float wobble = (1F - formation) * (1F - formation);
                Vector3f normal = new Vector3f(0.18F + wobble * 0.25F * (float) Math.sin(age * 11F),
                        1F, 0.25F + wobble * 0.2F * (float) Math.cos(age * 9F)).normalize();
                Vector3f axis = new Vector3f(1F, -normal.x / normal.y, 0F).normalize();
                view.transformDirection(normal);
                view.transformDirection(axis);
                float animationTime = ((mc.level.getGameTime() % 240000L) + partialTick) / 20F;
                float formationAge = Math.min(age, 2.5F);

                GpuBuffer ubo = UNIFORMS.write(b -> b
                        .putVec4(center.x, center.y, center.z, radius)
                        .putVec4(normal.x, normal.y, normal.z, formationAge)
                        .putVec4(axis.x, axis.y, axis.z, animationTime));

                // Read a copy of colour and the captured scene depth; write colour and depth of main.
                scratch = PostEffectSupport.ensureTarget(scratch, "nc black hole scratch", main, false);
                PostEffectSupport.copyColor(main, scratch);
                sceneDepth.copyDepthFrom(main);
                new PostEffectSupport.Draw("nc black hole", PIPELINE)
                        .sampler("InSampler", scratch.getColorTextureView())
                        .sampler("DepthSampler", sceneDepth.getDepthTextureView())
                        .uniform("BlackHoleConfig", ubo)
                        .into(main.getColorTextureView(), main.getDepthTextureView());
            }
        } catch (RuntimeException e) {
            failed = true;
            NuclearCraft.LOGGER.error("Black hole shader failed; using geometry fallback", e);
        } finally {
            clear();
        }
    }

    static void close() {
        UNIFORMS.close();
        if (scratch != null) scratch.destroyBuffers();
        scratch = null;
        failed = false;
    }
}
