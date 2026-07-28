package igentuman.nc.client.render.bomb;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import igentuman.nc.NuclearCraft;
import igentuman.nc.client.bomb.ActiveBomb;
import igentuman.nc.client.bomb.BombFxManager;
import igentuman.nc.client.renderer.ModShaders;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EffectInstance;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.lang.reflect.Field;
import java.util.List;

import static igentuman.nc.NuclearCraft.rl;

@EventBusSubscriber(modid = NuclearCraft.MODID, value = Dist.CLIENT)
public class NukeRenderer {

    public static final ResourceLocation TEXTURE = rl("textures/particle/nuke.png");
    public static final ResourceLocation NOISE_0 = rl("textures/particle/noise_channel_0.png");
    public static final ResourceLocation NOISE_1 = rl("textures/particle/noise_channel_1.png");
    public static final ResourceLocation NOISE_2 = rl("textures/particle/noise_channel_2.png");

    private static final float[] SPRITE_FIREBALL  = {0.0f, 0.0f, 0.5f, 0.5f};
    private static final float[] SPRITE_SHOCKWAVE = {0.5f, 0.0f, 1.0f, 0.5f};
    private static final float[] SPRITE_SMOKE     = {0.0f, 0.5f, 0.5f, 1.0f};

    private static final int PHASE_FIREBALL    = 0;
    private static final int PHASE_SHOCKWAVE   = 1;
    private static final int PHASE_SMOKE       = 2;
    private static final int PHASE_STEM        = 3;
    private static final int PHASE_GROUND      = 4;
    private static final int PHASE_WHITE_SW    = 5;

    private static final int FIREBALL_GROW_TICKS = 20;
    private static final int FIREBALL_HOLD_TICKS = 60;
    private static final int FIREBALL_FADE_TICKS = 80;
    private static final int FIREBALL_TOTAL_TICKS = FIREBALL_GROW_TICKS + FIREBALL_HOLD_TICKS + FIREBALL_FADE_TICKS;

    private static final int SHOCKWAVE_DURATION_TICKS = 120;
    private static final float SHOCKWAVE_MAX_RADIUS_MULT = 5.0f;
    private static final float FIREBALL_SCALE = 3.5f;
    private static final float MUSHROOM_CAP_SCALE = 3.5f / 1.5f;

    private static final int WHITE_SW_COUNT = 4;
    private static final int WHITE_SW_DELAY_TICKS = 8;
    private static final int WHITE_SW_DURATION_TICKS = 140;

    private static int currentPostSize = 0;
    private static Field passesField;

    @SubscribeEvent
    public static void onRenderWorld(RenderLevelStageEvent ev) {
        if (ev.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;
        if (BombFxManager.active().isEmpty()) return;

        ShaderInstance shader = ModShaders.NUKE_CORE.get();
        if (shader == null) return;

        Vec3 camPos = ev.getCamera().getPosition();
        Quaternionf camRot = new Quaternionf(ev.getCamera().rotation());
        float partial = ev.getPartialTick().getGameTimeDeltaPartialTick(false);

        PoseStack pose = ev.getPoseStack();
        pose.pushPose();
        pose.translate(-camPos.x, -camPos.y, -camPos.z);

        RenderSystem.setShaderTexture(0, TEXTURE);
        RenderSystem.setShaderTexture(1, NOISE_0);
        RenderSystem.setShaderTexture(2, NOISE_1);
        RenderSystem.setShaderTexture(3, NOISE_2);
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();

        RenderSystem.setShader(ModShaders.NUKE_CORE);
        for (ActiveBomb b : BombFxManager.active()) {
            if (b.distance > BombFxManager.MAX_RANGE) continue;
            renderBombAdditive(b, pose, camRot, partial, shader);
        }

        ShaderInstance smokeShader = ModShaders.NUKE_SMOKE.get();
        if (smokeShader != null) {
            RenderSystem.setShader(ModShaders.NUKE_SMOKE);
            for (ActiveBomb b : BombFxManager.active()) {
                if (b.distance > BombFxManager.MAX_RANGE) continue;
                renderBombSmoke(b, pose, camRot, partial, smokeShader);
            }
        }

        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
        pose.popPose();
    }

    private static void renderBombAdditive(ActiveBomb b, PoseStack pose, Quaternionf camRot, float partial, ShaderInstance shader) {
        float t = b.tickCounter + partial;
        BlockPos e = b.epicenter;
        double cx = e.getX() + 0.5;
        double cy = e.getY() + 0.5;
        double cz = e.getZ() + 0.5;

        float yieldN = Mth.clamp(b.yield / 4f, 0.1f, 4f);
        float maxR = Math.max(18f, b.yield * 32f) * FIREBALL_SCALE;

        if (t < FIREBALL_TOTAL_TICKS) {
            float prog = Mth.clamp(t / (float) FIREBALL_TOTAL_TICKS, 0f, 1f);
            float growT = Mth.clamp(t / (float) FIREBALL_GROW_TICKS, 0f, 1f);
            float radius = maxR * (0.25f + 0.75f * easeOutCubic(growT));
            float fadeIn = (t - (FIREBALL_GROW_TICKS + FIREBALL_HOLD_TICKS)) / (float) FIREBALL_FADE_TICKS;
            float fade = 1f - Mth.clamp(fadeIn, 0f, 1f);

            drawBillboard(cx, cy, cz, radius, camRot, SPRITE_FIREBALL,
                    PHASE_FIREBALL, prog, yieldN, fade, shader, pose);
        }

        if (t < SHOCKWAVE_DURATION_TICKS) {
            float swT = Mth.clamp(t / (float) SHOCKWAVE_DURATION_TICKS, 0f, 1f);
            float ringR = maxR * SHOCKWAVE_MAX_RADIUS_MULT * swT;
            float ringAlpha = 1f - swT * 0.5f;
            drawGroundQuad(cx, cy + 0.1, cz, ringR, SPRITE_SHOCKWAVE,
                    PHASE_SHOCKWAVE, swT, yieldN, ringAlpha, shader, pose);
        }

        float stemHLocal = Math.min(120f, Math.max(50f, b.yield * 70f));
        for (int i = 0; i < WHITE_SW_COUNT; i++) {
            float delay = i * WHITE_SW_DELAY_TICKS;
            float lt = t - delay;
            if (lt < 0f || lt >= WHITE_SW_DURATION_TICKS) continue;
            float swT = Mth.clamp(lt / (float) WHITE_SW_DURATION_TICKS, 0f, 1f);
            float radScale = 1.10f + 0.10f * i;
            float ringR = maxR * swT * radScale;
            float ringAlpha = 1f - swT * 0.6f;
            float heightFrac = 0.18f + 0.26f * i;
            double yOff = stemHLocal * heightFrac;
            drawGroundQuad(cx, cy + yOff, cz, ringR, SPRITE_SHOCKWAVE,
                    PHASE_WHITE_SW, swT, yieldN, ringAlpha, shader, pose);
        }
    }

    private static void renderBombSmoke(ActiveBomb b, PoseStack pose, Quaternionf camRot, float partial, ShaderInstance shader) {
        float t = b.tickCounter + partial;
        BlockPos e = b.epicenter;
        double cx = e.getX() + 0.5;
        double cy = e.getY() + 0.5;
        double cz = e.getZ() + 0.5;

        float yieldN = Mth.clamp(b.yield / 4f, 0.1f, 4f);
        float stemH = Math.min(120f, Math.max(50f, b.yield * 70f));

        if (t >= b.stemStart() && t < b.mushroomEnd()) {
            float local = t - b.stemStart();
            float prog = Mth.clamp(local / (float) ActiveBomb.MUSHROOM_DURATION, 0f, 1f);
            float capR = Math.max(4f, b.yield * 8f) * MUSHROOM_CAP_SCALE;
            float growT = Mth.clamp(local / 60f, 0f, 1f);
            float radius = capR * Math.max(0.3f, growT) * MUSHROOM_CAP_SCALE;
            double capY = cy + stemH * growT;
            float alpha = (1f - prog * 0.4f) * 0.95f;

            drawBillboard(cx, capY, cz, radius, camRot, SPRITE_SMOKE,
                    PHASE_SMOKE, prog, yieldN, alpha, shader, pose);
        }

        if (t >= b.stemStart() && t < b.mushroomEnd()) {
            float local = t - b.stemStart();
            float prog = Mth.clamp(local / (float) ActiveBomb.MUSHROOM_DURATION, 0f, 1f);
            float growT = Mth.clamp(local / 60f, 0f, 1f);
            float stemR = Math.max(10f, b.yield * 16f) * 1.1f;
            float stemHCur = stemH * Math.max(0.05f, growT);
            double stemMidY = cy + stemHCur * 0.5;
            float alpha = (1f - prog * 0.35f) * 0.9f;

            drawVerticalBillboard(cx, stemMidY, cz, stemR, stemHCur * 0.5f, camRot, SPRITE_SMOKE,
                    PHASE_STEM, prog, yieldN, alpha, shader, pose);
        }

        if (t >= b.groundCloudStart() && t < b.groundCloudEnd()) {
            float local = t - b.groundCloudStart();
            float prog = Mth.clamp(local / (float) ActiveBomb.GROUND_CLOUD_DURATION, 0f, 1f);
            float maxGR = Math.max(60f, b.yield * 90f);
            float growT = Mth.clamp(local / 120f, 0f, 1f);
            float gr = maxGR * easeOutCubic(growT);
            float alpha = (1f - prog * 0.85f) * 0.85f;

            drawVerticalBillboard(cx, cy + gr * 0.35, cz, gr, gr * 0.45f, camRot, SPRITE_SMOKE,
                    PHASE_GROUND, prog, yieldN, alpha, shader, pose);
        }
    }

    @SubscribeEvent
    public static void onPostFx(RenderLevelStageEvent ev) {
        if (ev.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;
        if (BombFxManager.active().isEmpty()) return;

        Minecraft mc = Minecraft.getInstance();
        PostChain post = ModShaders.nukePostEffect;
        if (post == null || mc.level == null) return;
        PostPass firstPass = firstPass(post);
        if (firstPass == null) return;

        int dim = mc.getWindow().getWidth() + mc.getWindow().getHeight();
        if (currentPostSize != dim) {
            currentPostSize = dim;
            post.resize(mc.getWindow().getWidth(), mc.getWindow().getHeight());
        }

        EffectInstance effect = firstPass.getEffect();

        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(false);

        boolean any = false;
        for (ActiveBomb b : BombFxManager.active()) {
            if (b.distance > BombFxManager.MAX_RANGE) continue;
            if (!setShockwaveUniforms(mc, ev, effect, b)) continue;
            post.process(ev.getPartialTick().getGameTimeDeltaPartialTick(false));
            any = true;
        }

        if (!any) {
            RenderSystem.depthMask(true);
            return;
        }

        mc.getMainRenderTarget().bindWrite(false);
        RenderTarget swap = post.getTempTarget("swap");
        if (swap != null) {
            swap.blitToScreen(mc.getMainRenderTarget().width, mc.getMainRenderTarget().height, false);
        }

        RenderSystem.depthMask(true);
    }

    private static boolean setShockwaveUniforms(Minecraft mc, RenderLevelStageEvent ev, EffectInstance effect, ActiveBomb b) {
        float now = b.tickCounter + ev.getPartialTick().getGameTimeDeltaPartialTick(false);
        if (now < 0f || now > SHOCKWAVE_DURATION_TICKS) return false;

        float t = Mth.clamp(now / (float) SHOCKWAVE_DURATION_TICKS, 0f, 1f);
        float yieldN = Math.max(1f, b.yield);

        Vec3 cPos = Vec3.atCenterOf(b.epicenter);
        Matrix4f viewMatrix = ev.getModelViewMatrix();
        Matrix4f projMatrix = RenderSystem.getProjectionMatrix();
        Vec3 camPos = mc.gameRenderer.getMainCamera().getPosition();

        Vector4f p = new Vector4f(
                (float) (cPos.x - camPos.x),
                (float) (cPos.y - camPos.y),
                (float) (cPos.z - camPos.z),
                1.0f);
        p.mul(viewMatrix);
        p.mul(projMatrix);
        if (p.w != 0f) {
            p.x /= p.w;
            p.y /= p.w;
            p.z /= p.w;
        }
        if (p.z <= -1f || p.z >= 1f) return false;

        float blurX = p.x * 0.5f + 0.5f;
        float blurY = p.y * 0.5f + 0.5f;
        float depth = (p.z + 1f) * 0.5f;

        float worldRing = Math.max(18f, b.yield * 32f) * SHOCKWAVE_MAX_RADIUS_MULT * t;
        float dist = (float) Math.max(b.distance, 1.0);
        float ringScreen = Mth.clamp(worldRing / dist, 0.05f, 1.8f);
        float thickness = Mth.lerp(t, 0.16f, 0.04f);
        float strength = (1f - t) * 1.35f * Mth.clamp(yieldN * 1.0f, 0.8f, 3.0f);
        float brighten = (1f - t * 0.6f) * 1.1f;

        float margin = ringScreen + 0.1f;
        if (blurX < -margin || blurX > 1f + margin || blurY < -margin || blurY > 1f + margin) {
            return false;
        }

        effect.getUniform("BlurPos").set(blurX, blurY);
        effect.getUniform("Radius").set(ringScreen, thickness);
        effect.getUniform("BlurDir").set(strength, brighten);
        effect.getUniform("BlackHoleDepth").set(depth);
        return true;
    }

    private static float easeOutCubic(float x) {
        float xm = 1f - x;
        return 1f - xm * xm * xm;
    }

    private static void drawBillboard(double wx, double wy, double wz, float radius,
                                      Quaternionf camRot, float[] sprite,
                                      int phase, float progress, float yieldN, float alpha,
                                      ShaderInstance shader, PoseStack pose) {
        if (alpha <= 0f || radius <= 0f) return;
        shader.safeGetUniform("NukeData").set((float) phase, progress, yieldN, alpha);
        shader.safeGetUniform("SpriteRect").set(sprite[0], sprite[1], sprite[2], sprite[3]);

        Vector3f[] v = {
                new Vector3f(-1f,  1f, 0f),
                new Vector3f( 1f,  1f, 0f),
                new Vector3f( 1f, -1f, 0f),
                new Vector3f(-1f, -1f, 0f)
        };
        for (Vector3f vv : v) {
            camRot.transform(vv);
            vv.mul(radius);
            vv.add((float) wx, (float) wy, (float) wz);
        }
        Matrix4f m = pose.last().pose();
        BufferBuilder bb = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        bb.addVertex(m, v[0].x, v[0].y, v[0].z).setColor(255, 255, 255, 255).setUv(sprite[0], sprite[3]);
        bb.addVertex(m, v[1].x, v[1].y, v[1].z).setColor(255, 255, 255, 255).setUv(sprite[2], sprite[3]);
        bb.addVertex(m, v[2].x, v[2].y, v[2].z).setColor(255, 255, 255, 255).setUv(sprite[2], sprite[1]);
        bb.addVertex(m, v[3].x, v[3].y, v[3].z).setColor(255, 255, 255, 255).setUv(sprite[0], sprite[1]);
        BufferUploader.drawWithShader(bb.buildOrThrow());
    }

    private static void drawVerticalBillboard(double wx, double wy, double wz, float halfW, float halfH,
                                              Quaternionf camRot, float[] sprite,
                                              int phase, float progress, float yieldN, float alpha,
                                              ShaderInstance shader, PoseStack pose) {
        if (alpha <= 0f || halfW <= 0f || halfH <= 0f) return;
        shader.safeGetUniform("NukeData").set((float) phase, progress, yieldN, alpha);
        shader.safeGetUniform("SpriteRect").set(sprite[0], sprite[1], sprite[2], sprite[3]);

        Vector3f camRight = new Vector3f(1f, 0f, 0f);
        camRot.transform(camRight);
        camRight.y = 0f;
        if (camRight.lengthSquared() < 1e-6f) camRight.set(1f, 0f, 0f);
        camRight.normalize();

        float rx = camRight.x * halfW;
        float rz = camRight.z * halfW;
        Vector3f[] v = {
                new Vector3f(-rx,  halfH, -rz),
                new Vector3f( rx,  halfH,  rz),
                new Vector3f( rx, -halfH,  rz),
                new Vector3f(-rx, -halfH, -rz)
        };
        for (Vector3f vv : v) vv.add((float) wx, (float) wy, (float) wz);

        Matrix4f m = pose.last().pose();
        BufferBuilder bb = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        bb.addVertex(m, v[0].x, v[0].y, v[0].z).setColor(255, 255, 255, 255).setUv(sprite[0], sprite[3]);
        bb.addVertex(m, v[1].x, v[1].y, v[1].z).setColor(255, 255, 255, 255).setUv(sprite[2], sprite[3]);
        bb.addVertex(m, v[2].x, v[2].y, v[2].z).setColor(255, 255, 255, 255).setUv(sprite[2], sprite[1]);
        bb.addVertex(m, v[3].x, v[3].y, v[3].z).setColor(255, 255, 255, 255).setUv(sprite[0], sprite[1]);
        BufferUploader.drawWithShader(bb.buildOrThrow());
    }

    private static void drawGroundQuad(double cx, double cy, double cz, float radius,
                                       float[] sprite, int phase, float progress, float yieldN, float alpha,
                                       ShaderInstance shader, PoseStack pose) {
        if (alpha <= 0f || radius <= 0f) return;
        shader.safeGetUniform("NukeData").set((float) phase, progress, yieldN, alpha);
        shader.safeGetUniform("SpriteRect").set(sprite[0], sprite[1], sprite[2], sprite[3]);

        Matrix4f m = pose.last().pose();
        BufferBuilder bb = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        float x0 = (float) (cx - radius), x1 = (float) (cx + radius);
        float z0 = (float) (cz - radius), z1 = (float) (cz + radius);
        float y = (float) cy;
        bb.addVertex(m, x0, y, z0).setColor(255, 255, 255, 255).setUv(sprite[0], sprite[1]);
        bb.addVertex(m, x0, y, z1).setColor(255, 255, 255, 255).setUv(sprite[0], sprite[3]);
        bb.addVertex(m, x1, y, z1).setColor(255, 255, 255, 255).setUv(sprite[2], sprite[3]);
        bb.addVertex(m, x1, y, z0).setColor(255, 255, 255, 255).setUv(sprite[2], sprite[1]);
        BufferUploader.drawWithShader(bb.buildOrThrow());
    }

    @SuppressWarnings("unchecked")
    private static PostPass firstPass(PostChain chain) {
        try {
            if (passesField == null) {
                passesField = PostChain.class.getDeclaredField("passes");
                passesField.setAccessible(true);
            }
            List<PostPass> passes = (List<PostPass>) passesField.get(chain);
            return passes.isEmpty() ? null : passes.get(0);
        } catch (ReflectiveOperationException e) {
            NuclearCraft.LOGGER.error("NukeRenderer: unable to access PostChain passes", e);
            return null;
        }
    }
}
