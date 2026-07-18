package igentuman.nc.client.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import igentuman.nc.entity.anomaly.AnomalyEntity;
import igentuman.nc.entity.anomaly.AnomalyType;
import igentuman.nc.entity.anomaly.GravitationalAnomalyEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector4f;

import static igentuman.nc.client.renderer.NCShaders.anomalyPostEffect;
import static igentuman.nc.handler.config.CommonConfig.ANOMALY_CONFIG;

/**
 * Screen-space post-process driver for the anomaly variants. Mirrors {@link Q36FlashShader}: every frame
 * it walks the loaded {@link AnomalyEntity}s, projects each centre to screen space, feeds the per-variant
 * uniforms (type, colour, intensity, lens magnification) into the shared {@code anomaly} post chain and
 * processes one pass per anomaly so multiple effects stack onto the main target. The fragment shader
 * branches on {@code AnomType} to produce the distinct look per variant.
 */
public class AnomalyShader {

    private static final double MAX_DISTANCE = 96.0D;
    private static int currentSize = 0;

    public static void register() {
        MinecraftForge.EVENT_BUS.register(AnomalyShader.class);
    }

    /** Base screen radius (at the reference 2160px height) before distance scaling, per variant. */
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

    /** Lens magnification fed to {@code Radius.y}; < 1 gives the gravitational inverse (sucking) lens. */
    private static float magnification(AnomalyType type) {
        return type == AnomalyType.GRAVITATIONAL ? 10.025F : 1.0F;
    }

    private static boolean processAnomaly(Minecraft mc, RenderLevelStageEvent event, EffectInstance effect, AnomalyEntity anomaly) {
        if (mc.level == null || mc.player == null) {
            return false;
        }
        Vec3 pos = anomaly.position().add(0.0D, 1.2D, 0.0D);
        double distance = Math.sqrt(mc.player.position().distanceToSqr(pos));
        if (distance > MAX_DISTANCE) {
            return false;
        }

        Matrix4f viewMatrix = event.getPoseStack().last().pose();
        Matrix4f projectionMatrix = RenderSystem.getProjectionMatrix();

        Vec3 camPos = mc.gameRenderer.getMainCamera().getPosition();
        float posX = (float) (pos.x - camPos.x());
        float posY = (float) (pos.y - camPos.y());
        float posZ = (float) (pos.z - camPos.z());

        Vector4f p = new Vector4f(posX, posY, posZ, 1.0f);
        p.transform(viewMatrix);
        p.transform(projectionMatrix);
        if (p.w() != 0.0f) {
            p.setX(p.x() / p.w());
            p.setY(p.y() / p.w());
            p.setZ(p.z() / p.w());
        }
        float normalizedDepth = (p.z() + 1.0f) * 0.5f;

        float blurX = 0.5f;
        float blurY = 0.5f;
        boolean visible = false;
        float distanceFactor = 0.0f;

        if (p.z() > -1.0f && p.z() < 1.0f) {
            blurX = (p.x() * 0.5f + 0.5f);
            blurY = (p.y() * 0.5f + 0.5f);
            float margin = 0.25f;
            if (blurX >= -margin && blurX <= 1.0f + margin && blurY >= -margin && blurY <= 1.0f + margin) {
                visible = true;
                distanceFactor = (float) (10.0f / Math.max(distance, 1.0));
            }
        }
        if (!visible) {
            return false;
        }

        AnomalyType type = anomaly.getAnomalyType();
        float radius = baseRadius(type) * distanceFactor;
        if (anomaly instanceof GravitationalAnomalyEntity grav) {
            radius *= (float) grav.massFactor();
        }
        radius = Math.min(radius, 700.0f);
        int color = type.color();

        effect.getUniform("BlurPos").set(blurX, blurY);
        effect.getUniform("Radius").set(radius, magnification(type));
        effect.getUniform("BlackHoleDepth").set(normalizedDepth);
        float timeSeconds = (System.nanoTime() % 1_000_000_000_000L) / 1_000_000_000.0f;
        effect.getUniform("Time").set(timeSeconds);
        effect.getUniform("AnomType").set(type.ordinal());
        effect.getUniform("Intensity").set(Math.max(0.2f, anomaly.getIntensity()));
        effect.getUniform("AnomColor").set(
                ((color >> 16) & 0xFF) / 255.0f,
                ((color >> 8) & 0xFF) / 255.0f,
                (color & 0xFF) / 255.0f);
        return true;
    }

    @SubscribeEvent
    public static void onRenderTick(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_CUTOUT_BLOCKS) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (anomalyPostEffect == null || mc.level == null) {
            return;
        }
        if (!ANOMALY_CONFIG.SHADER.get()) {
            return;
        }

        EffectInstance effect = anomalyPostEffect.passes.get(0).getEffect();

        int dim = mc.getWindow().getWidth() + mc.getWindow().getHeight();
        if (currentSize != dim) {
            currentSize = dim;
            anomalyPostEffect.resize(mc.getWindow().getWidth(), mc.getWindow().getHeight());
            effect.getUniform("BlurDir").set(0.2f, 0.0f);
        }

        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(false);

        boolean any = false;
        for (Entity e : mc.level.entitiesForRendering()) {
            if (!(e instanceof AnomalyEntity anomaly)) {
                continue;
            }
            if (processAnomaly(mc, event, effect, anomaly)) {
                anomalyPostEffect.process(mc.getFrameTime());
                any = true;
            }
        }

        RenderSystem.depthMask(true);
        if (any) {
            mc.getMainRenderTarget().bindWrite(false);
        }
    }
}
