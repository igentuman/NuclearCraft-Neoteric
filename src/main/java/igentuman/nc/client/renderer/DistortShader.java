package igentuman.nc.client.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import igentuman.nc.NuclearCraft;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Applies a screen-space lens distortion ({@code shaders/post/distort.json}) around one or more
 * world positions. Generic: register any {@link DistortionSource} keyed by an arbitrary object
 * (a {@link net.minecraft.core.BlockPos}, entity UUID, etc.) and remove it when no longer needed.
 *
 * <pre>{@code
 * // when the effect should appear:
 * DistortShader.add(blockPos, partialTick -> Vec3.atCenterOf(blockPos));
 * // when it should stop:
 * DistortShader.remove(blockPos);
 * }</pre>
 */
public class DistortShader {

    /** Master toggle - bind to your own config if desired. */
    public static boolean enabled = true;

    private static final Map<Object, DistortionSource> SOURCES = new ConcurrentHashMap<>();
    private static int currentSize = 0;
    private static Field passesField;

    public static void register() {
        NeoForge.EVENT_BUS.register(DistortShader.class);
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
        currentSize = 0;
    }

    /**
     * A point in the world that distorts the screen around it. Provide a position; override the
     * defaults to tune the look or gate the effect.
     */
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

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_CUTOUT_BLOCKS) return;
        if (!enabled || SOURCES.isEmpty()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        PostChain chain = ModShaders.distortPostEffect;
        if (chain == null) return;
        PostPass pass = firstPass(chain);
        if (pass == null) return;

        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(false);

        int size = mc.getWindow().getWidth() + mc.getWindow().getHeight();
        if (currentSize != size) {
            currentSize = size;
            chain.resize(mc.getWindow().getWidth(), mc.getWindow().getHeight());
            pass.getEffect().safeGetUniform("BlurDir").set(0.2F, 0.0F);
        }

        // Sample depth so geometry in front of the source occludes the effect, but don't write depth.
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(false);

        SOURCES.entrySet().removeIf(e -> !e.getValue().isActive());
        for (DistortionSource source : SOURCES.values()) {
            if (processSource(mc, event, pass, source, partialTick)) {
                chain.process(partialTick);
            }
        }

        // The chain's last pass outputs to minecraft:main; rebind it for the rest of level rendering.
        mc.getMainRenderTarget().bindWrite(false);
        RenderSystem.depthMask(true);
        RenderSystem.depthFunc(515); // GL_LEQUAL
    }

    private static boolean processSource(Minecraft mc, RenderLevelStageEvent event, PostPass pass, DistortionSource source, float partialTick) {
        if (!source.isActive()) return false;
        Vec3 pos = source.position(partialTick);
        if (pos == null) return false;

        double distance = mc.player.position().distanceTo(pos);
        if (distance > source.maxDistance()) return false;

        Matrix4f viewMatrix = event.getModelViewMatrix();
        Matrix4f projectionMatrix = RenderSystem.getProjectionMatrix();
        Camera camera = mc.gameRenderer.getMainCamera();
        Vec3 camPos = camera.getPosition();

        // Project the camera-relative world position to clip space.
        Vector4f clip = new Vector4f((float) (pos.x - camPos.x), (float) (pos.y - camPos.y), (float) (pos.z - camPos.z), 1.0F);
        clip.mul(viewMatrix);
        clip.mul(projectionMatrix);
        if (clip.w == 0.0F) return false;
        clip.x /= clip.w;
        clip.y /= clip.w;
        clip.z /= clip.w;

        // Behind the camera or outside the depth range -> skip.
        if (!(clip.z > -1.0F && clip.z < 1.0F)) return false;

        float blurX = clip.x * 0.5F + 0.5F;
        float blurY = clip.y * 0.5F + 0.5F;
        float margin = 0.1F;
        if (blurX < -margin || blurX > 1.0F + margin || blurY < -margin || blurY > 1.0F + margin) return false;

        float normalizedDepth = (clip.z + 1.0F) * 0.5F;
        float distanceFactor = (float) (7D / distance);
        float radius = source.radius() * distanceFactor;

        pass.getEffect().safeGetUniform("BlurPos").set(blurX, blurY);
        pass.getEffect().safeGetUniform("Radius").set(radius, source.magnification());
        pass.getEffect().safeGetUniform("EffectDepth").set(normalizedDepth);
        return true;
    }

    /**
     * {@link PostChain#passes} is private in 1.21 with no public accessor, so reach it reflectively
     * (the field name is stable at runtime under Mojang mappings). Disables the effect on failure
     * rather than spamming exceptions from the render loop.
     */
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
            NuclearCraft.LOGGER.error("DistortShader: unable to access PostChain passes; disabling effect", e);
            enabled = false;
            return null;
        }
    }
}
