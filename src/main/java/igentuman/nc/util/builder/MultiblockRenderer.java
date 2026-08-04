package igentuman.nc.util.builder;


import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class MultiblockRenderer {

    private static final int MAX_CACHED = 64;
    private static final LinkedHashMap<String, StructureMesh> MESH_CACHE =
            new LinkedHashMap<>(16, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, StructureMesh> eldest) {
                    if (size() > MAX_CACHED) {
                        eldest.getValue().close();
                        return true;
                    }
                    return false;
                }
            };

    public static Vec3i getSize(HashMap<BlockPos, Block> blockMap) {
        if (blockMap.isEmpty()) {
            return new Vec3i(1, 1, 1);
        }

        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        int maxZ = Integer.MIN_VALUE;

        for (BlockPos pos : blockMap.keySet()) {
            maxX = Math.max(maxX, pos.getX());
            maxY = Math.max(maxY, pos.getY());
            maxZ = Math.max(maxZ, pos.getZ());
        }

        return new Vec3i(maxX, maxY, maxZ);
    }

    public static void render(HashMap<BlockPos, Block> structure, PoseStack stack, int x, int y, int w, int h) {
        if (structure == null || structure.isEmpty()) {
            return;
        }

        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
        int hash = 0;
        for (Map.Entry<BlockPos, Block> entry : structure.entrySet()) {
            BlockPos pos = entry.getKey();
            int px = pos.getX(), py = pos.getY(), pz = pos.getZ();
            if (px < minX) minX = px;
            if (py < minY) minY = py;
            if (pz < minZ) minZ = pz;
            if (px > maxX) maxX = px;
            if (py > maxY) maxY = py;
            if (pz > maxZ) maxZ = pz;
            hash = hash * 31 + (pos.hashCode() ^ entry.getValue().hashCode());
        }
        int width = maxX - minX + 1;
        int height = maxY - minY + 1;
        int depth = maxZ - minZ + 1;

        String key = width + "x" + height + "x" + depth + '|' + structure.size() + '|' + hash;
        StructureMesh mesh = getOrBakeMesh(structure, key);
        if (mesh == null || mesh.isEmpty()) {
            return;
        }

        stack.pushPose();

        stack.translate(x + w / 2.0f, y + h / 2.0f, 100.0f);

        float maxDimension = Math.max(Math.max(width, height), depth);
        float baseScale = Math.min(w, h) * 0.9f;
        float scale = baseScale / maxDimension;
        stack.scale(scale, -scale, scale);

        stack.mulPose(new Quaternionf().rotationX((float) Math.toRadians(30)));
        stack.mulPose(new Quaternionf().rotationY((float) Math.toRadians(-45)));

        float fitScale = (float) (1.2f / (Math.log10(Math.max(Math.max(width, height), depth) + 105)));
        stack.scale(fitScale, fitScale, fitScale);
        stack.translate(
                -(minX + width / 2.0f),
                -(minY + height / 2.0f),
                -(minZ + depth / 2.0f)
        );

        Matrix4f modelView = new Matrix4f(RenderSystem.getModelViewMatrix());
        modelView.mul(stack.last().pose());
        mesh.draw(modelView, RenderSystem.getProjectionMatrix(), 1.0f, true);

        stack.popPose();
    }

    private static StructureMesh getOrBakeMesh(HashMap<BlockPos, Block> structure, String key) {
        StructureMesh cached = MESH_CACHE.get(key);
        if (cached != null) {
            return cached;
        }

        FakeStructureLevel level = new FakeStructureLevel();
        for (Map.Entry<BlockPos, Block> entry : structure.entrySet()) {
            BlockState state = entry.getValue().defaultBlockState();
            if (state.isAir()) continue;
            level.put(entry.getKey(), state);
        }

        StructureMesh mesh = new StructureMesh();
        try {
            mesh.rebuild(level);
        } catch (Exception ex) {
            mesh.close();
        }
        MESH_CACHE.put(key, mesh);
        return mesh;
    }

    public static void invalidateCache() {
        for (Iterator<StructureMesh> it = MESH_CACHE.values().iterator(); it.hasNext(); ) {
            it.next().close();
        }
        MESH_CACHE.clear();
    }
}
