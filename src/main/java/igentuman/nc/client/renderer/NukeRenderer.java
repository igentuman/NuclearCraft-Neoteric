package igentuman.nc.client.renderer;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import igentuman.nc.client.bomb.ActiveBomb;
import igentuman.nc.client.bomb.BombFxManager;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EffectInstance;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.NuclearCraft.rl;

@Mod.EventBusSubscriber(modid = MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class NukeRenderer {

    public static final ResourceLocation TEXTURE = rl("textures/particle/nuke.png");
    public static final ResourceLocation NOISE_0 = rl("textures/particle/noise_channel_0.png");
    public static final ResourceLocation NOISE_1 = rl("textures/particle/noise_channel_1.png");
    public static final ResourceLocation NOISE_2 = rl("textures/particle/noise_channel_2.png");

    // nuke.png is a 2x2 sprite atlas (256x256, 128x128 per cell).
    // Layout: TL = fireball core, TR = shockwave ring, BL = smoke veil, BR = reserved.
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

    @SubscribeEvent
    public static void onRenderWorld(RenderLevelStageEvent ev) {
        if (ev.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;
        if (BombFxManager.active().isEmpty()) return;

        ShaderInstance shader = NCShaders.NUKE_CORE.get();
        if (shader == null) return;

        Camera cam = ev.getCamera();
        Vec3 camPos = cam.getPosition();
        Quaternion camRot = cam.rotation();
        float partial = ev.getPartialTick();

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

        // Additive pass: fireball + shockwave ring (uses NUKE_CORE shader with additive blend)
        RenderSystem.setShader(NCShaders.NUKE_CORE);
        for (ActiveBomb b : BombFxManager.active()) {
            if (b.distance > BombFxManager.MAX_RANGE) continue;
            renderBombAdditive(b, pose, camRot, partial, shader);
        }

        // Alpha-blend pass: mushroom smoke (uses NUKE_SMOKE shader with alpha blend)
        ShaderInstance smokeShader = NCShaders.NUKE_SMOKE.get();
        if (smokeShader != null) {
            RenderSystem.setShader(NCShaders.NUKE_SMOKE);
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

    private static void renderBombAdditive(ActiveBomb b, PoseStack pose, Quaternion camRot, float partial, ShaderInstance shader) {
        float t = b.tickCounter + partial;
        BlockPos e = b.epicenter;
        double cx = e.getX() + 0.5;
        double cy = e.getY() + 0.5;
        double cz = e.getZ() + 0.5;

        float yieldN = Mth.clamp(b.yield / 4f, 0.1f, 4f);
        float maxR = Math.max(18f, b.yield * 32f) * FIREBALL_SCALE;

        // Fireball: kicks in immediately at detonation; grows fast, holds, fades.
        if (t < FIREBALL_TOTAL_TICKS) {
            float prog = Mth.clamp(t / (float) FIREBALL_TOTAL_TICKS, 0f, 1f);
            float growT = Mth.clamp(t / (float) FIREBALL_GROW_TICKS, 0f, 1f);
            float radius = maxR * (0.25f + 0.75f * easeOutCubic(growT));
            float fadeIn = (t - (FIREBALL_GROW_TICKS + FIREBALL_HOLD_TICKS)) / (float) FIREBALL_FADE_TICKS;
            float fade = 1f - Mth.clamp(fadeIn, 0f, 1f);

            drawBillboard(cx, cy, cz, radius, camRot, SPRITE_FIREBALL,
                    PHASE_FIREBALL, prog, yieldN, fade, shader, pose);
        }

        // Horizontal ground shockwave ring: starts simultaneously with fireball.
        if (t < SHOCKWAVE_DURATION_TICKS) {
            float swT = Mth.clamp(t / (float) SHOCKWAVE_DURATION_TICKS, 0f, 1f);
            float ringR = maxR * SHOCKWAVE_MAX_RADIUS_MULT * swT;
            float ringAlpha = 1f - swT * 0.5f;
            drawGroundQuad(cx, cy + 0.1, cz, ringR, SPRITE_SHOCKWAVE,
                    PHASE_SHOCKWAVE, swT, yieldN, ringAlpha, shader, pose);
        }

        // Secondary white shockwaves: smaller, staggered, spread up the mushroom column.
        float stemHLocal = Math.min(120f, Math.max(50f, b.yield * 70f));
        for (int i = 0; i < WHITE_SW_COUNT; i++) {
            float delay = i * WHITE_SW_DELAY_TICKS;
            float lt = t - delay;
            if (lt < 0f || lt >= WHITE_SW_DURATION_TICKS) continue;
            float swT = Mth.clamp(lt / (float) WHITE_SW_DURATION_TICKS, 0f, 1f);
            float radScale = 1.10f + 0.10f * i;  // 1.10..1.40 of fireball radius
            float ringR = maxR * swT * radScale;
            float ringAlpha = 1f - swT * 0.6f;
            // distribute across 0.18..0.95 of stem height
            float heightFrac = 0.18f + 0.26f * i;
            double yOff = stemHLocal * heightFrac;
            drawGroundQuad(cx, cy + yOff, cz, ringR, SPRITE_SHOCKWAVE,
                    PHASE_WHITE_SW, swT, yieldN, ringAlpha, shader, pose);
        }
    }

    private static void renderBombSmoke(ActiveBomb b, PoseStack pose, Quaternion camRot, float partial, ShaderInstance shader) {
        float t = b.tickCounter + partial;
        BlockPos e = b.epicenter;
        double cx = e.getX() + 0.5;
        double cy = e.getY() + 0.5;
        double cz = e.getZ() + 0.5;

        float yieldN = Mth.clamp(b.yield / 4f, 0.1f, 4f);

        float stemH = Math.min(120f, Math.max(50f, b.yield * 70f));

        // Mushroom cap (top billowing cloud)
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

        // Mushroom stem (vertical column, 2x wider)
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

        // Ground dust cloud (volumetric, 2x wider — vertical billboard)
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
        PostChain post = NCShaders.nukePostEffect;
        if (post == null || mc.level == null) return;

        int dim = mc.getWindow().getWidth() + mc.getWindow().getHeight();
        if (currentPostSize != dim) {
            currentPostSize = dim;
            post.resize(mc.getWindow().getWidth(), mc.getWindow().getHeight());
        }

        EffectInstance effect = post.passes.get(0).getEffect();

        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(false);

        boolean any = false;
        for (ActiveBomb b : BombFxManager.active()) {
            if (b.distance > BombFxManager.MAX_RANGE) continue;
            if (!setShockwaveUniforms(mc, ev, effect, b)) continue;
            post.process(mc.getFrameTime());
            any = true;
        }

        if (!any) {
            RenderSystem.depthMask(true);
            return;
        }

        // composite final post-chain output back onto the main framebuffer
        mc.getMainRenderTarget().bindWrite(false);
        post.passes.get(post.passes.size() - 1).outTarget.bindRead();

        RenderSystem.depthFunc(515);
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO);

        Tesselator tess = Tesselator.getInstance();
        BufferBuilder bb = tess.getBuilder();
        bb.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        int fbw = mc.getMainRenderTarget().width;
        int fbh = mc.getMainRenderTarget().height;
        bb.vertex(0, fbh, 0).uv(0, 0).endVertex();
        bb.vertex(fbw, fbh, 0).uv(1, 0).endVertex();
        bb.vertex(fbw, 0, 0).uv(1, 1).endVertex();
        bb.vertex(0, 0, 0).uv(0, 1).endVertex();
        tess.end();

        RenderSystem.depthFunc(515);
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
    }

    private static boolean setShockwaveUniforms(Minecraft mc, RenderLevelStageEvent ev, EffectInstance effect, ActiveBomb b) {
        float now = b.tickCounter + ev.getPartialTick();
        if (now < 0f || now > SHOCKWAVE_DURATION_TICKS) return false;

        float t = Mth.clamp(now / (float) SHOCKWAVE_DURATION_TICKS, 0f, 1f);
        float yieldN = Math.max(1f, b.yield);

        Vec3 cPos = Vec3.atCenterOf(b.epicenter);
        Matrix4f viewMatrix = ev.getPoseStack().last().pose();
        Matrix4f projMatrix = RenderSystem.getProjectionMatrix();
        Vec3 camPos = mc.gameRenderer.getMainCamera().getPosition();

        Vector4f p = new Vector4f(
                (float) (cPos.x - camPos.x),
                (float) (cPos.y - camPos.y),
                (float) (cPos.z - camPos.z),
                1.0f);
        p.transform(viewMatrix);
        p.transform(projMatrix);
        if (p.w() != 0f) {
            p.setX(p.x() / p.w());
            p.setY(p.y() / p.w());
            p.setZ(p.z() / p.w());
        }
        if (p.z() <= -1f || p.z() >= 1f) return false;

        float blurX = p.x() * 0.5f + 0.5f;
        float blurY = p.y() * 0.5f + 0.5f;
        float depth = (p.z() + 1f) * 0.5f;

        // ring radius in screen UV: physical radius / distance, ramped over t
        float worldRing = Math.max(18f, b.yield * 32f) * SHOCKWAVE_MAX_RADIUS_MULT * t;
        float dist = (float) Math.max(b.distance, 1.0);
        float ringScreen = Mth.clamp(worldRing / dist, 0.05f, 1.8f);
        float thickness = Mth.lerp(t, 0.16f, 0.04f);
        float strength = (1f - t) * 1.35f * Mth.clamp(yieldN * 1.0f, 0.8f, 3.0f);
        float brighten = (1f - t * 0.6f) * 1.1f;

        // off-screen margin tolerance
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
                                      Quaternion camRot, float[] sprite,
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
            vv.transform(camRot);
            vv.mul(radius);
            vv.add((float) wx, (float) wy, (float) wz);
        }
        Matrix4f m = pose.last().pose();
        Tesselator tess = Tesselator.getInstance();
        BufferBuilder bb = tess.getBuilder();
        bb.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);
        bb.vertex(m, v[0].x(), v[0].y(), v[0].z()).color(255, 255, 255, 255).uv(sprite[0], sprite[3]).endVertex();
        bb.vertex(m, v[1].x(), v[1].y(), v[1].z()).color(255, 255, 255, 255).uv(sprite[2], sprite[3]).endVertex();
        bb.vertex(m, v[2].x(), v[2].y(), v[2].z()).color(255, 255, 255, 255).uv(sprite[2], sprite[1]).endVertex();
        bb.vertex(m, v[3].x(), v[3].y(), v[3].z()).color(255, 255, 255, 255).uv(sprite[0], sprite[1]).endVertex();
        BufferUploader.drawWithShader(bb.end());
    }

    private static void drawVerticalBillboard(double wx, double wy, double wz, float halfW, float halfH,
                                              Quaternion camRot, float[] sprite,
                                              int phase, float progress, float yieldN, float alpha,
                                              ShaderInstance shader, PoseStack pose) {
        if (alpha <= 0f || halfW <= 0f || halfH <= 0f) return;
        shader.safeGetUniform("NukeData").set((float) phase, progress, yieldN, alpha);
        shader.safeGetUniform("SpriteRect").set(sprite[0], sprite[1], sprite[2], sprite[3]);

        // Yaw-locked billboard: horizontal axis follows camera right projected to ground plane,
        // vertical axis stays world Y so the stem reads as a column from any angle.
        Vector3f camRight = new Vector3f(1f, 0f, 0f);
        camRight.transform(camRot);
        camRight.set(camRight.x(), 0f, camRight.z());
        float lenSq = camRight.x() * camRight.x() + camRight.z() * camRight.z();
        if (lenSq < 1e-6f) camRight.set(1f, 0f, 0f);
        camRight.normalize();

        float rx = camRight.x() * halfW;
        float rz = camRight.z() * halfW;
        Vector3f[] v = {
                new Vector3f(-rx,  halfH, -rz),
                new Vector3f( rx,  halfH,  rz),
                new Vector3f( rx, -halfH,  rz),
                new Vector3f(-rx, -halfH, -rz)
        };
        for (Vector3f vv : v) vv.add((float) wx, (float) wy, (float) wz);

        Matrix4f m = pose.last().pose();
        Tesselator tess = Tesselator.getInstance();
        BufferBuilder bb = tess.getBuilder();
        bb.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);
        bb.vertex(m, v[0].x(), v[0].y(), v[0].z()).color(255, 255, 255, 255).uv(sprite[0], sprite[3]).endVertex();
        bb.vertex(m, v[1].x(), v[1].y(), v[1].z()).color(255, 255, 255, 255).uv(sprite[2], sprite[3]).endVertex();
        bb.vertex(m, v[2].x(), v[2].y(), v[2].z()).color(255, 255, 255, 255).uv(sprite[2], sprite[1]).endVertex();
        bb.vertex(m, v[3].x(), v[3].y(), v[3].z()).color(255, 255, 255, 255).uv(sprite[0], sprite[1]).endVertex();
        BufferUploader.drawWithShader(bb.end());
    }

    private static void drawGroundQuad(double cx, double cy, double cz, float radius,
                                       float[] sprite, int phase, float progress, float yieldN, float alpha,
                                       ShaderInstance shader, PoseStack pose) {
        if (alpha <= 0f || radius <= 0f) return;
        shader.safeGetUniform("NukeData").set((float) phase, progress, yieldN, alpha);
        shader.safeGetUniform("SpriteRect").set(sprite[0], sprite[1], sprite[2], sprite[3]);

        Matrix4f m = pose.last().pose();
        Tesselator tess = Tesselator.getInstance();
        BufferBuilder bb = tess.getBuilder();
        bb.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);
        float x0 = (float) (cx - radius), x1 = (float) (cx + radius);
        float z0 = (float) (cz - radius), z1 = (float) (cz + radius);
        float y = (float) cy;
        bb.vertex(m, x0, y, z0).color(255, 255, 255, 255).uv(sprite[0], sprite[1]).endVertex();
        bb.vertex(m, x0, y, z1).color(255, 255, 255, 255).uv(sprite[0], sprite[3]).endVertex();
        bb.vertex(m, x1, y, z1).color(255, 255, 255, 255).uv(sprite[2], sprite[3]).endVertex();
        bb.vertex(m, x1, y, z0).color(255, 255, 255, 255).uv(sprite[2], sprite[1]).endVertex();
        BufferUploader.drawWithShader(bb.end());
    }
}
