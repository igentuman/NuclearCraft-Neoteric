package igentuman.nc.client.render.builder;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.BlockQuadOutput;
import net.minecraft.client.renderer.block.BlockStateModelSet;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.EnumMap;
import java.util.Map;

public final class BlockMesh {

    private static final int FLOATS = 6; // x, y, z, u, v, padding (colour/overlay/light/normal live in ints)
    private final Map<ChunkSectionLayer, Vertices> layers = new EnumMap<>(ChunkSectionLayer.class);
    private final BlockStateModelSet models;
    private final boolean ambientOcclusion;

    private BlockMesh(BlockStateModelSet models, boolean ambientOcclusion) {
        this.models = models;
        this.ambientOcclusion = ambientOcclusion;
    }

    public static BlockMesh build(BlockAndTintGetter level, Map<BlockPos, BlockState> blocks) {
        Minecraft mc = Minecraft.getInstance();
        BlockStateModelSet models = mc.getModelManager().getBlockStateModelSet();
        BlockMesh mesh = new BlockMesh(models, mc.options.ambientOcclusion().get());
        ModelBlockRenderer renderer = new ModelBlockRenderer(mesh.ambientOcclusion, true, mc.getBlockColors());
        Map<ChunkSectionLayer, VertexConsumer> recorders = new EnumMap<>(ChunkSectionLayer.class);
        for (ChunkSectionLayer layer : ChunkSectionLayer.values()) {
            Vertices vertices = new Vertices();
            mesh.layers.put(layer, vertices);
            recorders.put(layer, vertices.recorder());
        }
        BlockQuadOutput output = (x, y, z, quad, instance) ->
                recorders.get(quad.materialInfo().layer()).putBlockBakedQuad(x, y, z, quad, instance);
        for (Map.Entry<BlockPos, BlockState> entry : blocks.entrySet()) {
            BlockPos pos = entry.getKey();
            BlockState state = entry.getValue();
            if (state.isAir() || state.getRenderShape() != RenderShape.MODEL) continue;
            renderer.tesselateBlock(output, pos.getX(), pos.getY(), pos.getZ(), level, pos, state,
                    models.get(state), state.getSeed(pos));
        }
        return mesh;
    }

    public boolean isEmpty() {
        return layers.values().stream().allMatch(vertices -> vertices.ints.isEmpty());
    }

    public boolean isEmpty(ChunkSectionLayer layer) {
        return layers.get(layer).ints.isEmpty();
    }

    public long vertexStorageBytes() {
        long bytes = 0;
        for (Vertices vertices : layers.values()) {
            bytes += (long) vertices.positions.elements().length * Float.BYTES;
            bytes += (long) vertices.ints.elements().length * Integer.BYTES;
        }
        return bytes;
    }

    public boolean isCurrent() {
        Minecraft mc = Minecraft.getInstance();
        return models == mc.getModelManager().getBlockStateModelSet()
                && ambientOcclusion == mc.options.ambientOcclusion().get();
    }

    public void replay(PoseStack.Pose pose, VertexConsumer buffer, float alpha) {
        for (Vertices vertices : layers.values()) vertices.replay(pose, buffer, alpha);
    }

    public void replay(ChunkSectionLayer layer, PoseStack.Pose pose, VertexConsumer buffer, float alpha) {
        layers.get(layer).replay(pose, buffer, alpha);
    }

    private static final class Vertices {
        private final FloatArrayList positions = new FloatArrayList();
        private final IntArrayList ints = new IntArrayList();

        public void replay(PoseStack.Pose pose, VertexConsumer buffer, float alpha) {
            Matrix4f matrix = pose.pose();
            Vector3f p = new Vector3f();
            Vector3f n = new Vector3f();
            int vertices = ints.size() / 4;
            for (int i = 0; i < vertices; i++) {
                int f = i * FLOATS;
                int k = i * 4;
                matrix.transformPosition(positions.getFloat(f), positions.getFloat(f + 1), positions.getFloat(f + 2), p);
                int packedNormal = ints.getInt(k + 3);
                pose.transformNormal((byte) packedNormal / 127F, (byte) (packedNormal >> 8) / 127F,
                        (byte) (packedNormal >> 16) / 127F, n);
                int color = ints.getInt(k);
                int a = Math.round(ARGB.alpha(color) * alpha);
                buffer.addVertex(p.x, p.y, p.z, ARGB.color(a, color), positions.getFloat(f + 3), positions.getFloat(f + 4),
                        ints.getInt(k + 1), ints.getInt(k + 2), n.x, n.y, n.z);
            }
        }

        private VertexConsumer recorder() {
            return new VertexConsumer() {
                @Override
                public void addVertex(float x, float y, float z, int color, float u, float v, int overlayCoords,
                                      int lightCoords, float nx, float ny, float nz) {
                    positions.add(x);
                    positions.add(y);
                    positions.add(z);
                    positions.add(u);
                    positions.add(v);
                    positions.add(0F);
                    ints.add(color);
                    ints.add(overlayCoords);
                    ints.add(lightCoords);
                    ints.add((Math.round(nx * 127F) & 0xFF) | (Math.round(ny * 127F) & 0xFF) << 8 | (Math.round(nz * 127F) & 0xFF) << 16);
                }

                @Override
                public VertexConsumer addVertex(float x, float y, float z) {
                    throw new UnsupportedOperationException();
                }

                @Override
                public VertexConsumer setColor(int r, int g, int b, int a) {
                    return this;
                }

                @Override
                public VertexConsumer setColor(int color) {
                    return this;
                }

                @Override
                public VertexConsumer setUv(float u, float v) {
                    return this;
                }

                @Override
                public VertexConsumer setUv1(int u, int v) {
                    return this;
                }

                @Override
                public VertexConsumer setUv2(int u, int v) {
                    return this;
                }

                @Override
                public VertexConsumer setNormal(float x, float y, float z) {
                    return this;
                }

                @Override
                public VertexConsumer setLineWidth(float width) {
                    return this;
                }
            };
        }
    }
}
