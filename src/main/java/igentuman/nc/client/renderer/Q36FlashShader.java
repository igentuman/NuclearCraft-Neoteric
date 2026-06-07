package igentuman.nc.client.renderer;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import igentuman.nc.entity.Q36EnergyFlash;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EffectInstance;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.client.renderer.NCShaders.q36FlashPostEffect;

@Mod.EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
public class Q36FlashShader {

    private static int currentSize = 0;

    public static void register() {
        MinecraftForge.EVENT_BUS.register(Q36FlashShader.class);
    }

    private static boolean processFlash(Minecraft mc, RenderLevelStageEvent event, EffectInstance effect, Q36EnergyFlash flash) {
        if (mc.level == null || mc.player == null) return false;

        Vec3 flashPos = flash.position();
        double distance = Math.sqrt(mc.player.position().distanceToSqr(flashPos));
        if (distance > 64) return false;

        int age = flash.tickCount;
        int life = Q36EnergyFlash.LIFETIME_TICKS;
        if (age >= life) return false;

        int phase1End = 6;
        float radiusFactor;
        float magnification;
        if (age < phase1End) {
            float t1 = (float) age / (float) phase1End;
            float ease = 1.0F - (1.0F - t1) * (1.0F - t1);
            radiusFactor = ease;
            magnification = 1.5F + ease * 14.0F;
        } else {
            float t2 = (float) (age - phase1End) / (float) (life - phase1End);
            float ease = t2 * t2;
            radiusFactor = 1.0F - ease * 0.5F;
            magnification = 1.0F - ease * 0.95F;
        }

        Matrix4f viewMatrix = event.getPoseStack().last().pose();
        Matrix4f projectionMatrix = RenderSystem.getProjectionMatrix();

        Vec3 camPos = mc.gameRenderer.getMainCamera().getPosition();
        float posX = (float) (flashPos.x - camPos.x());
        float posY = (float) (flashPos.y - camPos.y());
        float posZ = (float) (flashPos.z - camPos.z());

        Vector4f p = new Vector4f(posX, posY, posZ, 1.0f);
        p.mul(viewMatrix);
        p.mul(projectionMatrix);
        if (p.w != 0.0f) {
            p.x /= p.w;
            p.y /= p.w;
            p.z /= p.w;
        }
        float normalizedDepth = (p.z + 1.0f) * 0.5f;

        float blurX = 0.5f;
        float blurY = 0.5f;
        boolean visible = false;
        float distanceFactor = 0.0f;

        if (p.z > -1.0f && p.z < 1.0f) {
            blurX = (p.x * 0.5f + 0.5f);
            blurY = (p.y * 0.5f + 0.5f);
            float margin = 0.1f;
            if (blurX >= -margin && blurX <= 1.0f + margin && blurY >= -margin && blurY <= 1.0f + margin) {
                visible = true;
                distanceFactor = (float) (7f / Math.max(distance, 1.0));
            }
        }

        float baseRadius = visible ? 300.0f : 0.0f;
        float radius = baseRadius * distanceFactor * radiusFactor;
        if (!visible) magnification = 0.1f;

        effect.getUniform("BlurPos").set(blurX, blurY);
        effect.getUniform("Radius").set(radius, magnification);
        effect.getUniform("BlackHoleDepth").set(normalizedDepth);
        float timeSeconds = (System.nanoTime() % 1_000_000_000_000L) / 1_000_000_000.0f;
        effect.getUniform("Time").set(timeSeconds);
        return true;
    }

    @SubscribeEvent
    public static void onRenderTick(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_CUTOUT_BLOCKS) return;
        Minecraft mc = Minecraft.getInstance();
        if (q36FlashPostEffect == null || mc.level == null) return;

        EffectInstance effect = q36FlashPostEffect.passes.get(0).getEffect();

        int dim = mc.getWindow().getWidth() + mc.getWindow().getHeight();
        if (currentSize != dim) {
            currentSize = dim;
            q36FlashPostEffect.resize(mc.getWindow().getWidth(), mc.getWindow().getHeight());
            effect.getUniform("BlurDir").set(0.2f, 0.0f);
        }

        boolean any = false;
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(false);

        for (net.minecraft.world.entity.Entity e : mc.level.entitiesForRendering()) {
            if (!(e instanceof Q36EnergyFlash flash)) continue;
            if (processFlash(mc, event, effect, flash)) {
                q36FlashPostEffect.process(mc.getFrameTime());
                any = true;
            }
        }

        if (!any) {
            RenderSystem.depthMask(true);
            return;
        }

        mc.getMainRenderTarget().bindWrite(false);
        q36FlashPostEffect.passes.get(q36FlashPostEffect.passes.size() - 1).outTarget.bindRead();

        RenderSystem.depthFunc(515);
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO
        );

        Tesselator tess = Tesselator.getInstance();
        BufferBuilder bb = tess.getBuilder();
        bb.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bb.vertex(0, mc.getMainRenderTarget().height, 0).uv(0, 0).endVertex();
        bb.vertex(mc.getMainRenderTarget().width, mc.getMainRenderTarget().height, 0).uv(1, 0).endVertex();
        bb.vertex(mc.getMainRenderTarget().width, 0, 0).uv(1, 1).endVertex();
        bb.vertex(0, 0, 0).uv(0, 1).endVertex();
        tess.end();

        RenderSystem.depthFunc(515);
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
    }
}
