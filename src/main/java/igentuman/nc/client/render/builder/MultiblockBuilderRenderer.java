package igentuman.nc.client.render.builder;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import igentuman.nc.block_entity.MultiblockBuilderBE;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix4f;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public class MultiblockBuilderRenderer implements BlockEntityRenderer<MultiblockBuilderBE> {

    private static final float ALPHA = 0.5f;
    private static final float BLOCKED_R = 1f, BLOCKED_G = 0.15f, BLOCKED_B = 0.15f, BLOCKED_A = 0.85f;
    private static final Map<MultiblockBuilderBE, Cache> CACHES = new WeakHashMap<>();

    public MultiblockBuilderRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(MultiblockBuilderBE be, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        MultiblockBuilderBE.PlanStatus status = be.getPlanStatus();
        Cache cache = CACHES.computeIfAbsent(be, ignored -> new Cache());

        if (!status.blocked().isEmpty()) {
            clearMesh(cache);
            renderBlocked(poseStack, status.blocked());
            return;
        }

        Map<BlockPos, BlockState> preview = status.preview();
        if (preview.isEmpty()) {
            clearMesh(cache);
            return;
        }

        if (!preview.equals(cache.lastPreview)) {
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

        Matrix4f mv = new Matrix4f(RenderSystem.getModelViewMatrix()).mul(poseStack.last().pose());
        cache.mesh.draw(mv, RenderSystem.getProjectionMatrix(), ALPHA, false);
    }

    private static void clearMesh(Cache cache) {
        if (cache.mesh != null) {
            cache.mesh.close();
            cache.mesh = null;
            cache.lastPreview = null;
        }
    }

    private static void renderBlocked(PoseStack poseStack, List<BlockPos> blocked) {
        Matrix4f matrix = poseStack.last().pose();
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);

        for (BlockPos pos : blocked) {
            addWireCube(buffer, matrix, pos.getX(), pos.getY(), pos.getZ());
        }

        MeshData meshData = buffer.build();
        if (meshData == null) return;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.lineWidth(2f);
        BufferUploader.drawWithShader(meshData);
        RenderSystem.lineWidth(1f);
        RenderSystem.disableBlend();
    }

    private static void addWireCube(BufferBuilder buffer, Matrix4f m, int x, int y, int z) {
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

    private static void edge(BufferBuilder buffer, Matrix4f m, float x0, float y0, float z0, float x1, float y1, float z1) {
        buffer.addVertex(m, x0, y0, z0).setColor(BLOCKED_R, BLOCKED_G, BLOCKED_B, BLOCKED_A);
        buffer.addVertex(m, x1, y1, z1).setColor(BLOCKED_R, BLOCKED_G, BLOCKED_B, BLOCKED_A);
    }

    @Override
    public int getViewDistance() {
        return 64;
    }

    private static final class Cache {
        Map<BlockPos, BlockState> lastPreview;
        StructureMesh mesh;
    }
}
