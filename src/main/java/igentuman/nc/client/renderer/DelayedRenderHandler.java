package igentuman.nc.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Collects deferred translucent renders and flushes them once per frame at
 * {@link RenderLevelStageEvent.Stage#AFTER_TRANSLUCENT_BLOCKS}, with the pose stack translated back
 * to world origin (0,0,0) so callers can render at absolute world coordinates.
 *
 * <p>This is an immediate-mode queue: it is cleared every frame, so callers must re-submit their
 * renderers each frame (see {@link BillboardingEffectRenderer#submit}).
 */
public class DelayedRenderHandler {

    private static final Minecraft MC = Minecraft.getInstance();

    private static final Map<RenderType, List<LazyRender>> TRANSPARENT_RENDERERS = new HashMap<>();

    public static void register() {
        NeoForge.EVENT_BUS.register(DelayedRenderHandler.class);
    }

    /** Queue a renderer to be drawn this frame under the given render type. */
    public static void addTransparentRenderer(RenderType renderType, LazyRender render) {
        TRANSPARENT_RENDERERS.computeIfAbsent(renderType, r -> new ArrayList<>()).add(render);
    }

    @SubscribeEvent
    public static void onRenderStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;
        if (TRANSPARENT_RENDERERS.isEmpty()) return;

        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        int renderTick = event.getRenderTick();
        Camera camera = event.getCamera();
        Vec3 camPos = camera.getPosition();
        PoseStack poseStack = event.getPoseStack();

        poseStack.pushPose();
        // Translate by the inverse camera position to get back to world origin (0,0,0).
        poseStack.translate(-camPos.x, -camPos.y, -camPos.z);

        MultiBufferSource.BufferSource buffers = MC.renderBuffers().bufferSource();

        // Draw farthest groups first so translucency blends in the right order.
        TRANSPARENT_RENDERERS.entrySet().stream()
                .map(entry -> new RenderGroup(entry.getKey(), entry.getValue(), closestDistanceSqr(entry.getValue(), camPos, partialTick)))
                .sorted(Comparator.comparingDouble(group -> -group.closestSqr))
                .forEachOrdered(group -> {
                    VertexConsumer buffer = buffers.getBuffer(group.renderType);
                    for (LazyRender render : group.renders) {
                        render.render(camera, buffer, poseStack, renderTick, partialTick);
                    }
                    buffers.endBatch(group.renderType);
                });

        poseStack.popPose();
        TRANSPARENT_RENDERERS.clear();
    }

    private static double closestDistanceSqr(List<LazyRender> renders, Vec3 camPos, float partialTick) {
        double closest = Double.MAX_VALUE;
        for (LazyRender render : renders) {
            Vec3 pos = render.getCenterPos(partialTick);
            if (pos != null) {
                closest = Math.min(closest, camPos.distanceToSqr(pos));
            }
        }
        return closest;
    }

    private record RenderGroup(RenderType renderType, List<LazyRender> renders, double closestSqr) {
    }

    @FunctionalInterface
    public interface LazyRender {

        void render(Camera camera, VertexConsumer buffer, PoseStack poseStack, int renderTick, float partialTick);

        /** Optional world-space center, used to depth-sort groups. */
        @Nullable
        default Vec3 getCenterPos(float partialTick) {
            return null;
        }
    }
}
