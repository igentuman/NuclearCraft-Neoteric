package igentuman.nc.client.renderer;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.shaders.UniformType;
import igentuman.nc.NuclearCraft;
import igentuman.nc.client.bomb.ActiveBomb;
import igentuman.nc.client.bomb.BombFxManager;
import igentuman.nc.client.bomb.NukeTimeline;
import igentuman.nc.client.bomb.NukeVisualState;
import igentuman.nc.config.Client;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.client.renderer.state.level.SkyRenderState;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.ExtractLevelRenderStateEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

import java.util.ArrayList;
import java.util.List;

import static igentuman.nc.NuclearCraft.rl;

public final class NukeVolumeRenderer {

    public static final int MAX_DETAILED = 4;
    public static final double MAX_RENDER_DISTANCE = 4096.0;
    public static final Identifier NOISE_ATLAS = rl("textures/effect/nuke_noise.png");
    private static final float SELECTION_HYSTERESIS = 1.25f;

    static final RenderPipeline THERMAL_PIPELINE = PostEffectSupport.pipeline(rl("pipeline/nuke_thermal"), rl("post/nuke_thermal"))
            .withSampler("InSampler")
            .withSampler("DepthSampler")
            .withUniform("NukeThermal", UniformType.UNIFORM_BUFFER)
            .build();
    static final RenderPipeline VOLUME_PIPELINE = PostEffectSupport.pipeline(rl("pipeline/nuke_volume"), rl("post/nuke_volume"))
            .withSampler("DepthSampler")
            .withSampler("NoiseSampler")
            .withUniform("NukeVolume", UniformType.UNIFORM_BUFFER)
            .build();
    static final RenderPipeline COMPOSITE_PIPELINE = PostEffectSupport.pipeline(rl("pipeline/nuke_composite"), rl("post/nuke_composite"))
            .withSampler("InSampler")
            .withSampler("VolumeSampler")
            .withSampler("DepthSampler")
            .withUniform("NukeComposite", UniformType.UNIFORM_BUFFER)
            .build();

    private static final PostEffectSupport.Uniforms THERMAL_UNIFORMS = new PostEffectSupport.Uniforms("nc nuke thermal", 512);
    private static final PostEffectSupport.Uniforms VOLUME_UNIFORMS = new PostEffectSupport.Uniforms("nc nuke volume", 768);
    private static final PostEffectSupport.Uniforms COMPOSITE_UNIFORMS = new PostEffectSupport.Uniforms("nc nuke composite", 64);

    private static TextureTarget thermalOut;
    /** RGBA8 stores logarithmic radiance and linear transmittance. */
    private static TextureTarget volumeOut;
    private static Lighting pendingLighting;
    private static boolean failed;

    private record Lighting(float x, float y, float intensity, int skyColor, float brightness) {}

    private static final List<ActiveBomb> CANDIDATES = new ArrayList<>();
    private static final List<ActiveBomb> SELECTED = new ArrayList<>(MAX_DETAILED);
    private static final List<ActiveBomb> PREVIOUS = new ArrayList<>(MAX_DETAILED);
    private static final float[] PACKED = new float[NukeVisualState.PACKED_SIZE];
    private static final float[] THERMAL_CENTERS = new float[16];
    private static final float[] THERMAL_PARAMS = new float[16];
    private static final float[] THERMAL_EXTRA = new float[16];

    private NukeVolumeRenderer() {}

    public static boolean available() {
        return !failed;
    }

    private static boolean hasWork() {
        if (!available()) return false;
        for (ActiveBomb bomb : BombFxManager.active()) {
            if (!bomb.expired()) return true;
        }
        return false;
    }

    static boolean hasFrame() {
        return pendingLighting != null;
    }

    static void extractLighting(ExtractLevelRenderStateEvent event) {
        // The level renderer resets its sky state before the final render stage.
        pendingLighting = null;
        if (!hasWork()) return;

        SkyRenderState sky = event.getRenderState().skyRenderState;
        float partialTick = event.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        var attributes = event.getCamera().attributeProbe();
        int skyColor = attributes.getValue(EnvironmentAttributes.SKY_COLOR, partialTick);
        float brightness = Math.max(0.2f, attributes.getValue(EnvironmentAttributes.SKY_LIGHT_FACTOR, partialTick));
        if (sky.skybox != DimensionType.Skybox.OVERWORLD) {
            pendingLighting = new Lighting(0f, 1f, 0f, skyColor, brightness);
            return;
        }

        float sunHeight = Mth.cos(sky.sunAngle);
        float moonHeight = Mth.cos(sky.moonAngle);
        float sun = Mth.clamp(sunHeight * 4f + 0.2f, 0f, 1f);
        float moon = Mth.clamp(moonHeight * 4f + 0.2f, 0f, 1f) * 0.15f;
        boolean useSun = sun >= moon;
        // SkyRenderer turns the celestial path onto the X/Y plane.
        float x = -Mth.sin(useSun ? sky.sunAngle : sky.moonAngle);
        float y = Math.max(useSun ? sunHeight : moonHeight, 0.15f);
        float length = Mth.sqrt(x * x + y * y);
        float intensity = Math.max(sun, moon) * (0.4f + 0.6f * sky.rainBrightness);
        pendingLighting = new Lighting(x / length, y / length, intensity, skyColor, brightness);
    }

    private static int scaled(int size, Client.NukeQuality quality) {
        return Math.max(1, Math.round(size * quality.volumeScale));
    }

    private static void resize(RenderTarget main, Client.NukeQuality quality) {
        thermalOut = PostEffectSupport.ensureTarget(thermalOut, "nc nuke thermal", main, false);
        int volumeWidth = scaled(main.width, quality);
        int volumeHeight = scaled(main.height, quality);
        if (volumeOut == null) {
            volumeOut = new TextureTarget("nc nuke volume", volumeWidth, volumeHeight, false);
        } else if (volumeOut.width != volumeWidth || volumeOut.height != volumeHeight) {
            volumeOut.resize(volumeWidth, volumeHeight);
        }
    }

    private static void select(Vec3 camera, float partialTick) {
        PREVIOUS.clear();
        PREVIOUS.addAll(SELECTED);
        SELECTED.clear();
        CANDIDATES.clear();
        for (ActiveBomb bomb : BombFxManager.active()) {
            bomb.visual.evaluate(bomb, partialTick);
            if (!bomb.visual.visible()) continue;
            if (distance(bomb, camera) - bomb.visual.boundsRadius > MAX_RENDER_DISTANCE) continue;
            CANDIDATES.add(bomb);
        }
        CANDIDATES.sort((a, b) -> Float.compare(score(b, camera), score(a, camera)));
        for (int i = 0; i < CANDIDATES.size() && i < MAX_DETAILED; i++) {
            SELECTED.add(CANDIDATES.get(i));
        }
        CANDIDATES.clear();
    }

    private static float score(ActiveBomb bomb, Vec3 camera) {
        NukeVisualState v = bomb.visual;
        float coverage = (float) (Math.max(v.boundsRadius, v.boundsTop) / Math.max(1.0, distance(bomb, camera)));
        return PREVIOUS.contains(bomb) ? coverage * SELECTION_HYSTERESIS : coverage;
    }

    private static double distance(ActiveBomb bomb, Vec3 camera) {
        return Math.sqrt(camera.distanceToSqr(Vec3.atCenterOf(bomb.epicenter)));
    }

    static void render(RenderLevelStageEvent event, TextureTarget sceneDepth) {
        Lighting light = pendingLighting;
        pendingLighting = null;
        Minecraft mc = Minecraft.getInstance();
        if (light == null || !available() || mc.level == null || sceneDepth == null) return;
        LevelRenderState state = event.getLevelRenderState();
        Vec3 camera = state.cameraRenderState.pos;
        float partialTick = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        select(camera, partialTick);
        if (SELECTED.isEmpty()) return;

        RenderTarget main = mc.getMainRenderTarget();
        Client.NukeQuality quality = Client.effectiveNukeQuality();
        Matrix4fc projection = state.cameraRenderState.projectionMatrix;
        Matrix4fc view = event.getModelViewMatrix();
        Matrix4f inverseProjection = new Matrix4f(projection).invert();
        Matrix4f inverseView = new Matrix4f(view).invert();

        try {
            resize(main, quality);
            int thermalCount = packThermal(camera);
            GpuBuffer thermal = THERMAL_UNIFORMS.write(b -> b
                    .putMat4f(inverseProjection)
                    .putMat4f(inverseView)
                    .putMat4f(projection)
                    .putMat4f(view)
                    .putMat4f(new Matrix4f().set(THERMAL_CENTERS))
                    .putMat4f(new Matrix4f().set(THERMAL_PARAMS))
                    .putMat4f(new Matrix4f().set(THERMAL_EXTRA))
                    .putInt(thermalCount));
            float detail = quality.detail;
            GpuBuffer volume = VOLUME_UNIFORMS.write(b -> {
                b.putMat4f(inverseProjection).putMat4f(inverseView);
                Matrix4f[] shapeB = new Matrix4f[MAX_DETAILED];
                for (int i = 0; i < MAX_DETAILED; i++) {
                    if (i < SELECTED.size()) {
                        ActiveBomb bomb = SELECTED.get(i);
                        bomb.visual.pack(PACKED, bomb, camera, detail);
                        b.putMat4f(new Matrix4f().set(PACKED, 0));
                        shapeB[i] = new Matrix4f().set(PACKED, 16);
                    } else {
                        b.putMat4f(new Matrix4f().zero());
                        shapeB[i] = new Matrix4f().zero();
                    }
                }
                for (Matrix4f m : shapeB) b.putMat4f(m);
                b.putVec4(light.x(), light.y(), 0f, light.intensity());
                b.putVec4(ARGB.redFloat(light.skyColor()), ARGB.greenFloat(light.skyColor()),
                        ARGB.blueFloat(light.skyColor()), light.brightness());
                b.putInt(SELECTED.size()).putInt(quality.maxSamples).putInt(quality.lightProbes);
            });
            GpuBuffer composite = COMPOSITE_UNIFORMS.write(b -> b.putMat4f(inverseProjection));

            // Thermal refraction reads the finished frame; the volume is ray-marched at reduced
            // resolution; the composite blends both back into the main target.
            new PostEffectSupport.Draw("nc nuke thermal", THERMAL_PIPELINE)
                    .sampler("InSampler", main.getColorTextureView())
                    .sampler("DepthSampler", sceneDepth.getDepthTextureView())
                    .uniform("NukeThermal", thermal)
                    .into(thermalOut.getColorTextureView(), null);
            AbstractTexture noise = mc.getTextureManager().getTexture(NOISE_ATLAS);
            new PostEffectSupport.Draw("nc nuke volume", VOLUME_PIPELINE)
                    .sampler("DepthSampler", sceneDepth.getDepthTextureView())
                    .sampler("NoiseSampler", noise.getTextureView(), noise.getSampler())
                    .uniform("NukeVolume", volume)
                    .into(volumeOut.getColorTextureView(), null);
            new PostEffectSupport.Draw("nc nuke composite", COMPOSITE_PIPELINE)
                    .sampler("InSampler", thermalOut.getColorTextureView())
                    .sampler("VolumeSampler", volumeOut.getColorTextureView())
                    .sampler("DepthSampler", sceneDepth.getDepthTextureView())
                    .uniform("NukeComposite", composite)
                    .into(main.getColorTextureView(), null);
        } catch (RuntimeException e) {
            failed = true;
            NuclearCraft.LOGGER.error("Nuke volume shaders failed; volumetric explosions disabled", e);
        }
    }

    private static int packThermal(Vec3 camera) {
        java.util.Arrays.fill(THERMAL_CENTERS, 0f);
        java.util.Arrays.fill(THERMAL_PARAMS, 0f);
        java.util.Arrays.fill(THERMAL_EXTRA, 0f);
        int count = 0;
        if (Client.nukeThermalRefraction) {
            for (ActiveBomb bomb : SELECTED) {
                NukeVisualState v = bomb.visual;
                if ((v.thermalStrength <= 0f && v.hazeStrength <= 0f) || v.thermalRadius <= 0f) continue;
                int o = count * 4;
                THERMAL_CENTERS[o] = (float) (bomb.epicenter.getX() + 0.5 - camera.x);
                THERMAL_CENTERS[o + 1] = (float) (bomb.epicenter.getY() + 0.5 - camera.y);
                THERMAL_CENTERS[o + 2] = (float) (bomb.epicenter.getZ() + 0.5 - camera.z);
                THERMAL_CENTERS[o + 3] = v.thermalRadius;
                THERMAL_PARAMS[o] = v.thermalStrength;
                THERMAL_PARAMS[o + 1] = NukeTimeline.THERMAL_THICKNESS * v.r;
                THERMAL_PARAMS[o + 2] = v.hazeStrength;
                THERMAL_PARAMS[o + 3] = v.hazeRadius;
                THERMAL_EXTRA[o] = v.age;
                THERMAL_EXTRA[o + 1] = v.r;
                count++;
            }
        }
        return count;
    }

    public static void clear() {
        pendingLighting = null;
        SELECTED.clear();
        PREVIOUS.clear();
        CANDIDATES.clear();
    }

    public static void close() {
        clear();
        THERMAL_UNIFORMS.close();
        VOLUME_UNIFORMS.close();
        COMPOSITE_UNIFORMS.close();
        if (thermalOut != null) thermalOut.destroyBuffers();
        if (volumeOut != null) volumeOut.destroyBuffers();
        thermalOut = null;
        volumeOut = null;
        failed = false;
    }
}
