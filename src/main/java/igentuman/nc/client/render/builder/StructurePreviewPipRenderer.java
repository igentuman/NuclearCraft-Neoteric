package igentuman.nc.client.render.builder;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import igentuman.nc.block.turbine.TurbineBladeBlock;
import igentuman.nc.client.renderer.ModRenderType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.BlockModelSet;
import net.minecraft.client.renderer.block.BlockStateModelSet;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.block.model.BlockStateModelWrapper;
import net.minecraft.client.renderer.block.model.EmptyBlockModel;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ARGB;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Quaternionf;
import org.joml.Matrix4fc;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class StructurePreviewPipRenderer extends PictureInPictureRenderer<StructurePreviewState> {

    private static final int BOX_COLOR = ARGB.colorFromFloat(0.8f, 0.2f, 1.0f, 0.2f);
    private static final BlockDisplayContext DISPLAY = BlockDisplayContext.create();

    private static final int MAX_CACHED_PREVIEWS = 16;
    private static final long MAX_CACHED_VERTEX_BYTES = 32L * 1024 * 1024;
    // NeoForge can assign a different PiP renderer to a preview on each frame. Share only
    // CPU geometry; each renderer still owns its independent PiP textures and projection.
    private static final LinkedHashMap<PreviewKey, PreviewMesh> MESHES = new LinkedHashMap<>(16, 0.75f, true);
    private static long cachedVertexBytes;
    private static BlockStateModelSet terrainModels;
    private static BlockModelSet standaloneModels;
    private static boolean ambientOcclusion;
    private static ClientLevel tintLevel;
    private static int liveRenderers;
    private boolean closed;

    public StructurePreviewPipRenderer(MultiBufferSource.BufferSource bufferSource) {
        super(bufferSource);
        liveRenderers++;
    }

    @Override
    public Class<StructurePreviewState> getRenderStateClass() {
        return StructurePreviewState.class;
    }

    @Override
    protected void renderToTexture(StructurePreviewState state, PoseStack poseStack) {
        Minecraft.getInstance().gameRenderer.getLighting().setupFor(Lighting.Entry.ITEMS_3D);
        poseStack.mulPose(new Quaternionf().rotationX((float) Math.toRadians(state.pitch())));
        poseStack.mulPose(new Quaternionf().rotationY((float) Math.toRadians(state.yaw())));
        poseStack.translate(-state.sw() / 2f, -state.sh() / 2f, -state.sd() / 2f);

        PreviewMesh preview = mesh(state);
        BlockMesh blocks = preview.blocks();
        replay(blocks, ChunkSectionLayer.SOLID, poseStack);
        replay(blocks, ChunkSectionLayer.CUTOUT, poseStack);

        if (preview.specialBlocks().isEmpty() && state.blockEntities().isEmpty()) {
            replay(blocks, ChunkSectionLayer.TRANSLUCENT, poseStack);
        } else {
            // As with vanilla GuiEntityRenderer, drain features while PiP's colour/depth
            // overrides are active. Flush opaque special geometry before any glass.
            FeatureRenderDispatcher features = Minecraft.getInstance().gameRenderer.getFeatureRenderDispatcher();
            try {
                var dispatcher = Minecraft.getInstance().getBlockEntityRenderDispatcher();
                var camera = new net.minecraft.client.renderer.state.level.CameraRenderState();
                for (var entry : state.blockEntities().entrySet()) {
                    poseStack.pushPose();
                    try {
                        BlockPos pos = entry.getKey();
                        poseStack.translate(pos.getX(), pos.getY(), pos.getZ());
                        dispatcher.submit(entry.getValue(), poseStack, features.getSubmitNodeStorage(), camera);
                    } finally {
                        poseStack.popPose();
                    }
                }
                for (SpecialBlock block : preview.specialBlocks()) {
                    if (state.blockEntities().containsKey(block.pos())) continue;
                    poseStack.pushPose();
                    try {
                        poseStack.translate(block.pos().getX(), block.pos().getY(), block.pos().getZ());
                        block.model().submitMultiLayer(poseStack, features.getSubmitNodeStorage(),
                                LightCoordsUtil.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 0);
                    } finally {
                        poseStack.popPose();
                    }
                }
                features.renderSolidFeatures();
                bufferSource.endBatch();
                replay(blocks, ChunkSectionLayer.TRANSLUCENT, poseStack);
                features.renderTranslucentFeatures();
                bufferSource.endBatch();
            } finally {
                features.clearSubmitNodes();
            }
        }
        if (state.boundingBox()) {
            VertexConsumer lines = bufferSource.getBuffer(RenderTypes.lines());
            box(poseStack.last(), lines, state.sw(), state.sh(), state.sd());
            bufferSource.endBatch(RenderTypes.lines());
        }
    }

    private void replay(BlockMesh blocks, ChunkSectionLayer layer, PoseStack poseStack) {
        if (blocks.isEmpty(layer)) return;
        var type = ModRenderType.previewBlock(layer);
        blocks.replay(layer, poseStack.last(), bufferSource.getBuffer(type), 1f);
        bufferSource.endBatch(type);
    }

    private static PreviewMesh mesh(StructurePreviewState state) {
        Minecraft mc = Minecraft.getInstance();
        BlockModelSet models = mc.getModelManager().getBlockModelSet();
        BlockStateModelSet blockStateModels = mc.getModelManager().getBlockStateModelSet();
        boolean ao = mc.options.ambientOcclusion().get();
        if (terrainModels != blockStateModels || standaloneModels != models
                || ambientOcclusion != ao || tintLevel != mc.level) {
            clearCache();
            terrainModels = blockStateModels;
            standaloneModels = models;
            ambientOcclusion = ao;
            tintLevel = mc.level;
        }
        PreviewKey key = new PreviewKey(state.blocks(), state.sliceLevel());
        PreviewMesh cached = MESHES.get(key);
        if (cached != null) return cached;

        FakeStructureLevel level = new FakeStructureLevel();
        Map<BlockPos, BlockState> visible = new HashMap<>();
        Map<BlockState, BlockModelRenderState> resolvedSpecials = new HashMap<>();
        List<SpecialBlock> special = new ArrayList<>();
        state.blocks().forEach((pos, sourceState) -> {
            if (state.sliceLevel() >= 0 && pos.getY() >= state.sliceLevel()) return;
            if (sourceState.isAir()) return;
            // The idle preview shows each turbine blade at its own block position.
            BlockState blockState = sourceState.getBlock() instanceof TurbineBladeBlock
                    ? sourceState.setValue(TurbineBladeBlock.HIDDEN, false) : sourceState;
            level.put(pos, blockState);
            visible.put(pos, blockState);
            // Native special models may be nested in composites/conditionals. Capture
            // just their special component; the terrain mesh already owns normal quads.
            BlockModelRenderState model = resolvedSpecials.computeIfAbsent(blockState, s -> {
                SpecialModelCapture capture = new SpecialModelCapture();
                var standalone = models.get(s);
                if (!(standalone instanceof BlockStateModelWrapper) && !(standalone instanceof EmptyBlockModel)) {
                    mc.getBlockModelResolver().update(capture, s, DISPLAY);
                }
                return capture.special;
            });
            if (!model.isEmpty()) {
                special.add(new SpecialBlock(pos.immutable(), model));
            }
        });
        PreviewMesh built = new PreviewMesh(BlockMesh.build(level, visible), List.copyOf(special));
        long bytes = built.blocks().vertexStorageBytes();
        // Oversized previews still render, but cannot displace the entire bounded cache.
        if (bytes <= MAX_CACHED_VERTEX_BYTES) {
            while (MESHES.size() >= MAX_CACHED_PREVIEWS || cachedVertexBytes + bytes > MAX_CACHED_VERTEX_BYTES) {
                cachedVertexBytes -= MESHES.pollFirstEntry().getValue().blocks().vertexStorageBytes();
            }
            MESHES.put(key, built);
            cachedVertexBytes += bytes;
        }
        return built;
    }

    /** Releases CPU previews on logout, invalidation, or disposal of the last PiP renderer. */
    public static void clearCache() {
        MESHES.clear();
        cachedVertexBytes = 0;
        terrainModels = null;
        standaloneModels = null;
        tintLevel = null;
    }

    @Override
    public void close() {
        if (closed) return;
        closed = true;
        try {
            super.close();
        } finally {
            if (--liveRenderers == 0) clearCache();
        }
    }

    /** Prepared layouts are stable snapshots; identity avoids hashing every block each frame. */
    private record PreviewKey(Map<BlockPos, BlockState> blocks, int slice) {
        @Override
        public boolean equals(Object other) {
            return other instanceof PreviewKey key && blocks == key.blocks && slice == key.slice;
        }

        @Override
        public int hashCode() {
            return 31 * System.identityHashCode(blocks) + slice;
        }
    }

    /** Terrain and standalone special components for one visible slice. */
    private record PreviewMesh(BlockMesh blocks, List<SpecialBlock> specialBlocks) {}

    /** A default standalone special model; no live block entity or level is retained. */
    private record SpecialBlock(BlockPos pos, BlockModelRenderState model) {}

    /** Separates a standalone model's special component from its ordinary terrain geometry. */
    private static final class SpecialModelCapture extends BlockModelRenderState {
        private final BlockModelRenderState special = new BlockModelRenderState();

        @Override
        public void clear() {
            super.clear();
            special.clear();
        }

        @Override
        public <T> void setupSpecialModel(SpecialModelRenderer<T> renderer, Matrix4fc transformation) {
            special.setupSpecialModel(renderer, transformation);
        }
    }

    private static void box(PoseStack.Pose m, VertexConsumer buf, float x1, float y1, float z1) {
        float x0 = 0, y0 = 0, z0 = 0;
        line(buf, m, x0, y0, z0, x1, y0, z0);
        line(buf, m, x1, y0, z0, x1, y0, z1);
        line(buf, m, x1, y0, z1, x0, y0, z1);
        line(buf, m, x0, y0, z1, x0, y0, z0);
        line(buf, m, x0, y1, z0, x1, y1, z0);
        line(buf, m, x1, y1, z0, x1, y1, z1);
        line(buf, m, x1, y1, z1, x0, y1, z1);
        line(buf, m, x0, y1, z1, x0, y1, z0);
        line(buf, m, x0, y0, z0, x0, y1, z0);
        line(buf, m, x1, y0, z0, x1, y1, z0);
        line(buf, m, x1, y0, z1, x1, y1, z1);
        line(buf, m, x0, y0, z1, x0, y1, z1);
    }

    private static void line(VertexConsumer buf, PoseStack.Pose m, float ax, float ay, float az, float bx, float by, float bz) {
        Vector3f normal = new Vector3f(bx - ax, by - ay, bz - az).normalize();
        buf.addVertex(m, ax, ay, az).setColor(BOX_COLOR).setNormal(m, normal).setLineWidth(2f);
        buf.addVertex(m, bx, by, bz).setColor(BOX_COLOR).setNormal(m, normal).setLineWidth(2f);
    }

    @Override
    protected float getTranslateY(int height, int guiScale) {
        return height / 2.0F;
    }

    @Override
    protected String getTextureLabel() {
        return "nuclearcraft structure preview";
    }
}
