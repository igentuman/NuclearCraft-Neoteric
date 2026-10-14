package igentuman.nc.client.renderer;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import igentuman.nc.NuclearCraft;
import igentuman.nc.client.bomb.ActiveBomb;
import igentuman.nc.client.bomb.BombFxManager;
import igentuman.nc.client.bomb.NukeTimeline;
import igentuman.nc.client.bomb.NukeVisualState;
import igentuman.nc.config.Client;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.EffectInstance;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static igentuman.nc.NuclearCraft.rl;

public final class NukeVolumeRenderer {

    public static final int MAX_DETAILED = 4;
    public static final double MAX_RENDER_DISTANCE = 4096.0;
    public static final ResourceLocation NOISE_ATLAS = rl("textures/effect/nuke_noise.png");
    private static final String[] SHAPE_A = {"ShapeA0", "ShapeA1", "ShapeA2", "ShapeA3"};
    private static final String[] SHAPE_B = {"ShapeB0", "ShapeB1", "ShapeB2", "ShapeB3"};
    private static final float SELECTION_HYSTERESIS = 1.25f;

    private static TextureTarget thermalOut;
    private static HdrTextureTarget volumeOut;
    private static TextureTarget compositeOut;
    private static PostPass thermalPass;
    private static PostPass volumePass;
    private static PostPass compositePass;
    private static int depthTexture;

    private static final List<ActiveBomb> CANDIDATES = new ArrayList<>();
    private static final List<ActiveBomb> SELECTED = new ArrayList<>(MAX_DETAILED);
    private static final List<ActiveBomb> PREVIOUS = new ArrayList<>(MAX_DETAILED);
    private static final float[] PACKED = new float[NukeVisualState.PACKED_SIZE];
    private static final float[] THERMAL_CENTERS = new float[16];
    private static final float[] THERMAL_PARAMS = new float[16];
    private static final float[] THERMAL_EXTRA = new float[16];
    private static final Matrix4f SCRATCH = new Matrix4f();

    private NukeVolumeRenderer() {}

    public static boolean available() {
        return thermalPass != null && volumePass != null && compositePass != null;
    }

    public static boolean hasWork() {
        if (!available()) return false;
        for (ActiveBomb bomb : BombFxManager.active()) {
            if (!bomb.expired()) return true;
        }
        return false;
    }

    public static void reload(ResourceProvider resources) {
        close();
        Minecraft mc = Minecraft.getInstance();
        RenderTarget main = mc.getMainRenderTarget();
        Client.NukeQuality quality = Client.effectiveNukeQuality();
        try {
            thermalOut = new TextureTarget(main.width, main.height, false, Minecraft.ON_OSX);
            volumeOut = new HdrTextureTarget(scaled(main.width, quality), scaled(main.height, quality), false, Minecraft.ON_OSX);
            compositeOut = new TextureTarget(main.width, main.height, false, Minecraft.ON_OSX);

            thermalPass = new PostPass(resources, "nuclearcraft:nuke_thermal", main, thermalOut, false);
            thermalPass.addAuxAsset("DepthSampler", NukeVolumeRenderer::depthTexture, main.width, main.height);

            volumePass = new PostPass(resources, "nuclearcraft:nuke_volume", main, volumeOut, false);
            volumePass.addAuxAsset("DepthSampler", NukeVolumeRenderer::depthTexture, main.width, main.height);
            volumePass.addAuxAsset("NoiseSampler", NukeVolumeRenderer::noiseTexture, 0, 0);

            compositePass = new PostPass(resources, "nuclearcraft:nuke_composite", thermalOut, compositeOut, false);
            compositePass.addAuxAsset("VolumeSampler", () -> volumeOut.getColorTextureId(), volumeOut.width, volumeOut.height);
            compositePass.addAuxAsset("DepthSampler", NukeVolumeRenderer::depthTexture, main.width, main.height);

            resize(main, quality);
        } catch (IOException | RuntimeException e) {
            close();
            NuclearCraft.LOGGER.error("Unable to load nuke volume shaders; volumetric explosions disabled", e);
        } finally {
            main.bindWrite(false);
        }
    }

    private static int depthTexture() {
        return depthTexture;
    }

    private static int noiseTexture() {
        return Minecraft.getInstance().getTextureManager().getTexture(NOISE_ATLAS).getId();
    }

    private static int scaled(int size, Client.NukeQuality quality) {
        return Math.max(1, Math.round(size * quality.volumeScale));
    }

    private static void resize(RenderTarget main, Client.NukeQuality quality) {
        if (thermalOut.width != main.width || thermalOut.height != main.height) {
            thermalOut.resize(main.width, main.height, Minecraft.ON_OSX);
        }
        if (compositeOut.width != main.width || compositeOut.height != main.height) {
            compositeOut.resize(main.width, main.height, Minecraft.ON_OSX);
        }
        int volumeWidth = scaled(main.width, quality);
        int volumeHeight = scaled(main.height, quality);
        if (volumeOut.width != volumeWidth || volumeOut.height != volumeHeight) {
            volumeOut.resize(volumeWidth, volumeHeight, Minecraft.ON_OSX);
        }
        thermalPass.setOrthoMatrix(ortho(thermalOut));
        volumePass.setOrthoMatrix(ortho(volumeOut));
        compositePass.setOrthoMatrix(ortho(compositeOut));
    }

    private static Matrix4f ortho(RenderTarget target) {
        return new Matrix4f().setOrtho(0F, target.width, 0F, target.height, 0.1F, 1000F);
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

    static void render(RenderLevelStageEvent event, int sceneDepthTexture) {
        Minecraft mc = Minecraft.getInstance();
        if (!available() || mc.level == null || sceneDepthTexture <= 0) return;
        Vec3 camera = event.getCamera().getPosition();
        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        select(camera, partialTick);
        if (SELECTED.isEmpty()) return;

        depthTexture = sceneDepthTexture;
        RenderTarget main = mc.getMainRenderTarget();
        Client.NukeQuality quality = Client.effectiveNukeQuality();
        resize(main, quality);
        Matrix4f projection = event.getProjectionMatrix();
        Matrix4f view = event.getModelViewMatrix();
        Matrix4f inverseProjection = new Matrix4f(projection).invert();
        Matrix4f inverseView = new Matrix4f(view).invert();
        EffectInstance thermal = thermalPass.getEffect();
        EffectInstance volume = volumePass.getEffect();
        EffectInstance composite = compositePass.getEffect();
        uploadCamera(thermal, inverseProjection, inverseView);
        uploadCamera(volume, inverseProjection, inverseView);
        uploadCamera(composite, inverseProjection, inverseView);
        thermal.safeGetUniform("Projection").set(projection);
        thermal.safeGetUniform("ViewMat").set(view);
        uploadThermal(thermal, camera);
        uploadVolumes(volume, camera, quality);
        uploadLighting(volume, mc.level, camera, partialTick);

        try {
            RenderSystem.disableBlend();
            thermalPass.process(partialTick);
            volumePass.process(partialTick);
            compositePass.process(partialTick);
            main.bindWrite(false);
            compositeOut.blitToScreen(main.width, main.height);
        } finally {
            depthTexture = 0;
            main.bindWrite(true);
            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
            RenderSystem.depthFunc(GL11.GL_LEQUAL);
            RenderSystem.defaultBlendFunc();
        }
    }

    private static void uploadCamera(EffectInstance effect, Matrix4f inverseProjection, Matrix4f inverseView) {
        effect.safeGetUniform("InverseProjection").set(inverseProjection);
        effect.safeGetUniform("InverseView").set(inverseView);
    }

    private static void uploadThermal(EffectInstance effect, Vec3 camera) {
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
                THERMAL_EXTRA[o + 2] = 0f;
                THERMAL_EXTRA[o + 3] = 0f;
                count++;
            }
        }
        effect.safeGetUniform("ThermalCenters").set(SCRATCH.set(THERMAL_CENTERS));
        effect.safeGetUniform("ThermalParams").set(SCRATCH.set(THERMAL_PARAMS));
        effect.safeGetUniform("ThermalExtra").set(SCRATCH.set(THERMAL_EXTRA));
        effect.safeGetUniform("ThermalCount").set(count);
    }

    private static void uploadVolumes(EffectInstance effect, Vec3 camera, Client.NukeQuality quality) {
        float detail = quality.detail;
        for (int i = 0; i < MAX_DETAILED; i++) {
            if (i < SELECTED.size()) {
                ActiveBomb bomb = SELECTED.get(i);
                bomb.visual.pack(PACKED, bomb, camera, detail);
                effect.safeGetUniform(SHAPE_A[i]).set(SCRATCH.set(PACKED, 0));
                effect.safeGetUniform(SHAPE_B[i]).set(SCRATCH.set(PACKED, 16));
            } else {
                effect.safeGetUniform(SHAPE_A[i]).set(SCRATCH.zero());
                effect.safeGetUniform(SHAPE_B[i]).set(SCRATCH.zero());
            }
        }
        effect.safeGetUniform("VolumeCount").set(SELECTED.size());
        effect.safeGetUniform("MaxSamples").set(quality.maxSamples);
        effect.safeGetUniform("LightProbes").set(quality.lightProbes);
    }

    private static void uploadLighting(EffectInstance effect, ClientLevel level, Vec3 camera, float partialTick) {
        float angle = level.getTimeOfDay(partialTick) * Mth.TWO_PI;
        float sx = -Mth.sin(angle);
        float sy = Mth.cos(angle);
        float sun = Mth.clamp(sy * 4f + 0.2f, 0f, 1f);
        float moon = Mth.clamp(-sy * 4f + 0.2f, 0f, 1f) * 0.15f;
        float intensity = Math.max(sun, moon) * (1f - level.getRainLevel(partialTick) * 0.6f);
        float lx = sy >= 0f ? sx : -sx;
        float ly = Math.max(Math.abs(sy), 0.15f);
        float lz = 0.15f;
        float length = Mth.sqrt(lx * lx + ly * ly + lz * lz);
        effect.safeGetUniform("Light").set(lx / length, ly / length, lz / length, intensity);
        Vec3 sky = level.getSkyColor(camera, partialTick);
        float brightness = level.getSkyDarken(partialTick);
        effect.safeGetUniform("Ambient").set((float) sky.x, (float) sky.y, (float) sky.z, brightness);
    }

    public static void clear() {
        SELECTED.clear();
        PREVIOUS.clear();
        CANDIDATES.clear();
    }

    public static void close() {
        clear();
        if (thermalPass != null) thermalPass.close();
        if (volumePass != null) volumePass.close();
        if (compositePass != null) compositePass.close();
        if (thermalOut != null) thermalOut.destroyBuffers();
        if (volumeOut != null) volumeOut.destroyBuffers();
        if (compositeOut != null) compositeOut.destroyBuffers();
        thermalPass = null;
        volumePass = null;
        compositePass = null;
        thermalOut = null;
        volumeOut = null;
        compositeOut = null;
    }
}
