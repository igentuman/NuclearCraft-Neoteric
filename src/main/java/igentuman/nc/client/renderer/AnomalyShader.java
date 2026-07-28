package igentuman.nc.client.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import igentuman.nc.NuclearCraft;
import igentuman.nc.config.Common;
import igentuman.nc.entity.anomaly.AnomalyEntity;
import igentuman.nc.entity.anomaly.AnomalyType;
import igentuman.nc.entity.anomaly.GravitationalAnomalyEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import java.lang.reflect.Field;
import java.util.List;

public class AnomalyShader {

    private static final double MAX_DISTANCE = 96.0D;
    private static int currentSize = 0;
    private static Field passesField;

    public static void register() {
        NeoForge.EVENT_BUS.register(AnomalyShader.class);
    }

    public static void clear() {
        currentSize = 0;
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

    @net.neoforged.bus.api.SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_CUTOUT_BLOCKS) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        PostChain chain = ModShaders.anomalyPostEffect;
        if (chain == null || mc.level == null) {
            return;
        }
        if (!Common.ANOMALY_CONFIG.SHADER.get()) {
            return;
        }

        PostPass pass = firstPass(chain);
        if (pass == null) {
            return;
        }

        int dim = mc.getWindow().getWidth() + mc.getWindow().getHeight();
        if (currentSize != dim) {
            currentSize = dim;
            chain.resize(mc.getWindow().getWidth(), mc.getWindow().getHeight());
            pass.getEffect().safeGetUniform("BlurDir").set(0.2f, 0.0f);
        }

        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(false);

        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        for (Entity e : mc.level.entitiesForRendering()) {
            if (!(e instanceof AnomalyEntity anomaly)) {
                continue;
            }
            if (processAnomaly(mc, event, pass, anomaly, partialTick)) {
                chain.process(partialTick);
            }
        }

        RenderSystem.depthMask(true);
        mc.getMainRenderTarget().bindWrite(false);
        RenderSystem.depthFunc(515);
    }

    private static boolean processAnomaly(Minecraft mc, RenderLevelStageEvent event, PostPass pass, AnomalyEntity anomaly, float partialTick) {
        if (mc.player == null) {
            return false;
        }
        Vec3 pos = anomaly.position().add(0.0D, 1.2D, 0.0D);
        double distance = Math.sqrt(mc.player.position().distanceToSqr(pos));
        if (distance > MAX_DISTANCE) {
            return false;
        }

        Matrix4f viewMatrix = event.getModelViewMatrix();
        Matrix4f projMatrix = RenderSystem.getProjectionMatrix();
        Vec3 camPos = mc.gameRenderer.getMainCamera().getPosition();

        Vector4f p = new Vector4f((float)(pos.x - camPos.x), (float)(pos.y - camPos.y), (float)(pos.z - camPos.z), 1.0f);
        p.mul(viewMatrix);
        p.mul(projMatrix);
        if (p.w == 0.0f) return false;
        p.x /= p.w;
        p.y /= p.w;
        p.z /= p.w;

        if (!(p.z > -1.0f && p.z < 1.0f)) return false;

        float blurX = p.x * 0.5f + 0.5f;
        float blurY = p.y * 0.5f + 0.5f;
        float margin = 0.25f;
        if (blurX < -margin || blurX > 1.0f + margin || blurY < -margin || blurY > 1.0f + margin) return false;

        float normalizedDepth = (p.z + 1.0f) * 0.5f;
        float distanceFactor = (float)(10.0f / Math.max(distance, 1.0));

        AnomalyType type = anomaly.getAnomalyType();
        float radius = baseRadius(type) * distanceFactor;
        if (anomaly instanceof GravitationalAnomalyEntity grav) {
            radius *= (float) grav.massFactor();
        }
        radius = Math.min(radius, 700.0f);
        int color = type.color();

        pass.getEffect().safeGetUniform("BlurPos").set(blurX, blurY);
        pass.getEffect().safeGetUniform("Radius").set(radius, magnification(type));
        pass.getEffect().safeGetUniform("BlackHoleDepth").set(normalizedDepth);
        long nanoMod = System.nanoTime() % 1_000_000_000_000L;
        pass.getEffect().safeGetUniform("Time").set((float)(nanoMod / 1_000_000_000.0));
        pass.getEffect().safeGetUniform("AnomType").set(type.ordinal());
        pass.getEffect().safeGetUniform("Intensity").set(Math.max(0.2f, anomaly.getIntensity()));
        pass.getEffect().safeGetUniform("AnomColor").set(
                ((color >> 16) & 0xFF) / 255.0f,
                ((color >> 8) & 0xFF) / 255.0f,
                (color & 0xFF) / 255.0f);
        return true;
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
            NuclearCraft.LOGGER.error("AnomalyShader: unable to access PostChain passes; disabling", e);
            return null;
        }
    }
}
