package igentuman.nc.util.builder;

import igentuman.nc.util.StructurePreviewRenderer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

/** Auto-rotating 3D preview of a saved design, drawn through the shared structure preview. */
public final class DesignPreviewRenderer {

    private static final float ROTATION_SPEED_DEG_PER_SEC = 15f;
    private static final float TILT_DEG = 30f;
    private static final long START_NANOS = System.nanoTime();

    private static final Map<HashMap<BlockPos, Block>, Prepared> CACHE = new WeakHashMap<>();

    private record Prepared(Map<BlockPos, BlockState> blocks, int sw, int sh, int sd, int contentHash) {
    }

    private DesignPreviewRenderer() {}

    public static void render(GuiGraphicsExtractor graphics, HashMap<BlockPos, Block> blockMap,
                              int x, int y, int width, int height) {
        if (blockMap == null || blockMap.isEmpty()) return;

        Prepared prepared = CACHE.get(blockMap);
        if (prepared == null || prepared.contentHash() != blockMap.hashCode()) {
            prepared = prepare(blockMap);
            CACHE.put(blockMap, prepared);
        }
        int maxDim = Math.max(prepared.sw(), Math.max(prepared.sh(), prepared.sd()));
        if (maxDim <= 0) return;

        float scale = Math.min(width, height) / (float) (maxDim * 1.8f);
        float yaw = ((System.nanoTime() - START_NANOS) / 1_000_000_000f) * ROTATION_SPEED_DEG_PER_SEC;
        StructurePreviewRenderer.submit(graphics, prepared.blocks(), prepared.sw(), prepared.sh(), prepared.sd(),
                yaw, TILT_DEG, -1, false, x, y, width, height, scale);
    }

    private static Prepared prepare(HashMap<BlockPos, Block> blockMap) {
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
        for (BlockPos pos : blockMap.keySet()) {
            minX = Math.min(minX, pos.getX());
            minY = Math.min(minY, pos.getY());
            minZ = Math.min(minZ, pos.getZ());
            maxX = Math.max(maxX, pos.getX());
            maxY = Math.max(maxY, pos.getY());
            maxZ = Math.max(maxZ, pos.getZ());
        }
        Map<BlockPos, BlockState> blocks = new HashMap<>();
        for (Map.Entry<BlockPos, Block> entry : blockMap.entrySet()) {
            BlockState state = entry.getValue().defaultBlockState();
            if (state.isAir()) continue;
            BlockPos pos = entry.getKey();
            blocks.put(new BlockPos(pos.getX() - minX, pos.getY() - minY, pos.getZ() - minZ), state);
        }
        return new Prepared(Map.copyOf(blocks), maxX - minX + 1, maxY - minY + 1, maxZ - minZ + 1, blockMap.hashCode());
    }
}
