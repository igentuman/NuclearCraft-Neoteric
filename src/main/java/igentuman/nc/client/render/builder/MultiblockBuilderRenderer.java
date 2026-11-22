package igentuman.nc.client.render.builder;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import igentuman.nc.NuclearCraft;
import igentuman.nc.block_entity.MultiblockBuilderBE;
import igentuman.nc.client.renderer.ModRenderType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ExtractLevelRenderStateEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4fStack;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

@EventBusSubscriber(modid = NuclearCraft.MODID, value = Dist.CLIENT)
public class MultiblockBuilderRenderer implements BlockEntityRenderer<MultiblockBuilderBE, MultiblockBuilderRenderer.State> {

    private static final int BLOCKED_COLOR = ARGB.colorFromFloat(0.85f, 1f, 0.15f, 0.15f);
    private static final float BLOCKED_LINE_WIDTH = 2f;
    private static final Map<MultiblockBuilderBE, Cache> CACHES = new WeakHashMap<>();
    private static @Nullable Frame pendingFrame;

    public MultiblockBuilderRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(MultiblockBuilderBE be, State state, float partialTicks, Vec3 cameraPosition,
                                   ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(be, state, partialTicks, cameraPosition, breakProgress);
        MultiblockBuilderBE.PlanStatus status = be.getPlanStatus();
        Cache cache = CACHES.computeIfAbsent(be, ignored -> new Cache());
        state.blocked = status.blocked();
        state.mesh = null;

        if (!status.blocked().isEmpty()) {
            clearMesh(cache);
            return;
        }

        Map<BlockPos, BlockState> preview = status.preview();
        if (preview.isEmpty()) {
            clearMesh(cache);
            return;
        }

        if (cache.mesh == null || !cache.mesh.isCurrent() || !preview.equals(cache.lastPreview)) {
            FakeStructureLevel fakeLevel = new FakeStructureLevel();
            for (Map.Entry<BlockPos, BlockState> entry : preview.entrySet()) {
                fakeLevel.put(entry.getKey(), entry.getValue());
            }
            if (cache.mesh == null) {
                cache.mesh = new StructureMesh();
            }
            cache.mesh.rebuild(fakeLevel);
            cache.lastPreview = preview;
        }
        state.mesh = cache.mesh.snapshot();
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector,
                       net.minecraft.client.renderer.state.level.CameraRenderState camera) {
        if (!state.blocked.isEmpty()) {
            List<BlockPos> blocked = state.blocked;
            collector.submitCustomGeometry(poseStack, RenderTypes.lines(), (pose, buffer) -> {
                for (BlockPos pos : blocked) {
                    addWireCube(pose, buffer, pos.getX(), pos.getY(), pos.getZ());
                }
            });
            return;
        }
        // Ghosts draw after the level's transparency composite rather than in the feature batch.
    }

    @SubscribeEvent
    public static void onExtractLevel(ExtractLevelRenderStateEvent event) {
        List<Ghost> ghosts = event.getRenderState().blockEntityRenderStates.stream()
                .filter(renderState -> renderState instanceof State)
                .map(renderState -> (State) renderState)
                .filter(state -> state.mesh != null && !state.mesh.isEmpty())
                .map(state -> new Ghost(state.blockPos, state.mesh))
                .toList();
        // LevelRenderer resets its state before AfterLevel; keep only detached frame values.
        pendingFrame = ghosts.isEmpty() ? null : new Frame(event.getRenderState().cameraRenderState.pos, ghosts);
    }

    // Draw after vanilla's transparency composite, before the NORMAL-priority nuke post effect.
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onAfterLevel(RenderLevelStageEvent.AfterLevel event) {
        Frame frame = pendingFrame;
        pendingFrame = null;
        if (frame == null) return;

        MultiBufferSource.BufferSource buffers = Minecraft.getInstance().renderBuffers().bufferSource();
        PoseStack poseStack = event.getPoseStack();
        Matrix4fStack modelView = RenderSystem.getModelViewStack();
        modelView.pushMatrix();
        modelView.mul(event.getModelViewMatrix());
        try {
            for (ChunkSectionLayer layer : ChunkSectionLayer.values()) {
                var type = ModRenderType.ghostBlock(layer);
                for (Ghost ghost : frame.ghosts) {
                    if (ghost.mesh.isEmpty(layer)) continue;
                    poseStack.pushPose();
                    try {
                        poseStack.translate(ghost.origin.getX() - frame.camera.x,
                                ghost.origin.getY() - frame.camera.y, ghost.origin.getZ() - frame.camera.z);
                        ghost.mesh.replay(layer, poseStack.last(), buffers.getBuffer(type), 1F);
                    } finally {
                        poseStack.popPose();
                    }
                }
                buffers.endBatch(type);
            }
        } finally {
            modelView.popMatrix();
        }
    }

    public static void clear() {
        pendingFrame = null;
        CACHES.clear();
    }

    private static void clearMesh(Cache cache) {
        if (cache.mesh != null) {
            cache.mesh.close();
            cache.mesh = null;
            cache.lastPreview = null;
        }
    }

    private static void addWireCube(PoseStack.Pose m, VertexConsumer buffer, int x, int y, int z) {
        float x0 = x, y0 = y, z0 = z, x1 = x + 1f, y1 = y + 1f, z1 = z + 1f;

        edge(buffer, m, x0, y0, z0, x1, y0, z0);
        edge(buffer, m, x1, y0, z0, x1, y0, z1);
        edge(buffer, m, x1, y0, z1, x0, y0, z1);
        edge(buffer, m, x0, y0, z1, x0, y0, z0);

        edge(buffer, m, x0, y1, z0, x1, y1, z0);
        edge(buffer, m, x1, y1, z0, x1, y1, z1);
        edge(buffer, m, x1, y1, z1, x0, y1, z1);
        edge(buffer, m, x0, y1, z1, x0, y1, z0);

        edge(buffer, m, x0, y0, z0, x0, y1, z0);
        edge(buffer, m, x1, y0, z0, x1, y1, z0);
        edge(buffer, m, x1, y0, z1, x1, y1, z1);
        edge(buffer, m, x0, y0, z1, x0, y1, z1);
    }

    private static void edge(VertexConsumer buffer, PoseStack.Pose m, float x0, float y0, float z0, float x1, float y1, float z1) {
        Vector3f normal = new Vector3f(x1 - x0, y1 - y0, z1 - z0).normalize();
        buffer.addVertex(m, x0, y0, z0).setColor(BLOCKED_COLOR).setNormal(m, normal).setLineWidth(BLOCKED_LINE_WIDTH);
        buffer.addVertex(m, x1, y1, z1).setColor(BLOCKED_COLOR).setNormal(m, normal).setLineWidth(BLOCKED_LINE_WIDTH);
    }

    @Override
    public int getViewDistance() {
        return 64;
    }

    public static class State extends BlockEntityRenderState {
        List<BlockPos> blocked = List.of();
        @Nullable BlockMesh mesh;
    }

    private record Ghost(BlockPos origin, BlockMesh mesh) {}

    private record Frame(Vec3 camera, List<Ghost> ghosts) {}

    private static final class Cache {
        Map<BlockPos, BlockState> lastPreview;
        StructureMesh mesh;
    }
}
