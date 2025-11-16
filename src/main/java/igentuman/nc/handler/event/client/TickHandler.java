package igentuman.nc.handler.event.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import igentuman.nc.client.sound.GeigerSound;
import igentuman.nc.client.sound.SoundHandler;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

public class TickHandler {

    public static String currentScreenCode = "";

    public static final Minecraft minecraft = Minecraft.getInstance();

    private static final Map<RenderType, List<TickHandler.LazyRender>> transparentRenderers = new HashMap<>();

    public static void clearQueued() {
        transparentRenderers.clear();
    }

    private void renderStage(RenderLevelStageEvent event, boolean shouldRender, StageRenderer renderer) {
        if (shouldRender) {
            Camera camera = event.getCamera();
            PoseStack matrix = event.getPoseStack();
            matrix.pushPose();
            // here we translate based on the inverse position of the client viewing camera to get back to 0, 0, 0
            Vec3 camVec = camera.getPosition();
            matrix.translate(-camVec.x, -camVec.y, -camVec.z);
            renderer.render(camera, minecraft.renderBuffers().bufferSource(), matrix, event.getRenderTick(), event.getPartialTick());
            matrix.popPose();
        }
    }

    @SubscribeEvent
    public void renderWorld(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            //Only do matrix transforms and mess with buffers if we actually have any renders to render
            renderStage(event, !transparentRenderers.isEmpty(), (camera, renderer, poseStack, renderTick, partialTick) -> {
                ProfilerFiller profiler = minecraft.getProfiler();
                profiler.push("delayedNCTranslucentBERs");
                record TransparentRenderInfo(RenderType renderType, List<LazyRender> renders, double closest) {
                }
                Consumer<TransparentRenderInfo> renderInfoConsumer = info -> {
                    VertexConsumer buffer = renderer.getBuffer(info.renderType);
                    for (LazyRender transparentRender : info.renders) {
                        String profilerSection = transparentRender.getProfilerSection();
                        if (profilerSection != null) {
                            profiler.push(profilerSection);
                        }
                        transparentRender.render(camera, buffer, poseStack, renderTick, partialTick, profiler);
                        if (profilerSection != null) {
                            profiler.pop();
                        }
                    }
                    renderer.endBatch(info.renderType);
                };
                if (transparentRenderers.size() == 1) {
                    for (Map.Entry<RenderType, List<LazyRender>> entry : transparentRenderers.entrySet()) {
                        renderInfoConsumer.accept(new TransparentRenderInfo(entry.getKey(), entry.getValue(), 0));
                    }
                } else {
                    transparentRenderers.entrySet().stream()
                            .map(entry -> {
                                List<LazyRender> renders = entry.getValue();
                                double closest = Double.MAX_VALUE;
                                for (LazyRender render : renders) {
                                    Vec3 renderPos = render.getCenterPos(partialTick);
                                    if (renderPos != null) {
                                        double distanceSqr = camera.getPosition().distanceToSqr(renderPos);
                                        if (distanceSqr < closest) {
                                            closest = distanceSqr;
                                        }
                                    }
                                }
                                return new TransparentRenderInfo(entry.getKey(), renders, closest);
                            })
                            .sorted(Comparator.comparingDouble(info -> -info.closest))
                            .forEachOrdered(renderInfoConsumer);
                }
                transparentRenderers.clear();
                profiler.pop();
            });
        }
    }

    public static void register(FMLClientSetupEvent event) {
        MinecraftForge.EVENT_BUS.addListener(TickHandler::onTick);
        MinecraftForge.EVENT_BUS.register(new TickHandler());
    }
    @SubscribeEvent
    public static void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            tickStart();
        }
    }

    protected static GeigerSound geigerSound;
    public static void tickStart() {
        if (minecraft.player == null || true) {
            return;
        }
        GeigerSound toPlay = GeigerSound.create(minecraft.player);
        if(toPlay != null && (geigerSound == null || geigerSound.radiationLevel != toPlay.radiationLevel)) {
            if(geigerSound != null) {
                SoundHandler.stopSound(geigerSound);
            }
            geigerSound = toPlay;
            SoundHandler.playSound(geigerSound);
        }

        if(toPlay == null && geigerSound != null) {
            SoundHandler.stopSound(geigerSound);
            geigerSound = null;
        }
    }

    public static void addTransparentRenderer(RenderType renderType, LazyRender render) {
        transparentRenderers.computeIfAbsent(renderType, r -> new ArrayList<>()).add(render);
    }

    @FunctionalInterface
    public interface LazyRender {

        void render(Camera camera, VertexConsumer buffer, PoseStack poseStack, int renderTick, float partialTick, ProfilerFiller profiler);

        @Nullable
        default Vec3 getCenterPos(float partialTick) {
            return null;
        }

        @Nullable
        default String getProfilerSection() {
            return null;
        }
    }

    @FunctionalInterface
    private interface StageRenderer {
        void render(Camera camera, MultiBufferSource.BufferSource renderer, PoseStack poseStack, int renderTick, float partialTick);
    }
}
