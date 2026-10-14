package igentuman.nc.client.bomb;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Comparator;

public final class SurfaceHeightCache {

    public static final int GRID = 48;
    public static final int QUERIES_PER_TICK = 64;
    private static final float SMOOTHING = 0.2f;
    private static final int RING_SAMPLES = 8;
    private static final int[] ORDER = buildOrder();

    private final BlockPos origin;
    private final float extent;
    private final float cellSize;
    private final float[] target = new float[GRID * GRID];
    private final float[] current = new float[GRID * GRID];
    private final BitSet known = new BitSet(GRID * GRID);
    private final BitSet settled = new BitSet(GRID * GRID);
    private int cursor;

    public SurfaceHeightCache(BlockPos origin, float extent) {
        this.origin = origin;
        this.extent = Math.max(extent, GRID);
        this.cellSize = this.extent * 2f / GRID;
        Arrays.fill(current, Float.NaN);
    }

    public void tick(ClientLevel level) {
        int total = GRID * GRID;
        int cells = Math.min(QUERIES_PER_TICK, total);
        for (int i = 0; i < cells; i++) {
            refresh(level, ORDER[cursor]);
            cursor = (cursor + 1) % total;
        }
        for (int i = known.nextSetBit(0); i >= 0; i = known.nextSetBit(i + 1)) {
            if (!settled.get(i)) {
                current[i] = target[i];
                settled.set(i);
            } else {
                current[i] += (target[i] - current[i]) * SMOOTHING;
            }
        }
    }

    private static int[] buildOrder() {
        Integer[] cells = new Integer[GRID * GRID];
        for (int i = 0; i < cells.length; i++) cells[i] = i;
        float c = (GRID - 1) * 0.5f;
        Arrays.sort(cells, Comparator.comparingDouble(i -> {
            float x = i % GRID - c;
            float z = i / GRID - c;
            return x * x + z * z;
        }));
        int[] order = new int[cells.length];
        for (int i = 0; i < cells.length; i++) order[i] = cells[i];
        return order;
    }

    private void refresh(ClientLevel level, int index) {
        int x = Mth.floor(origin.getX() + 0.5f - extent + (index % GRID + 0.5f) * cellSize);
        int z = Mth.floor(origin.getZ() + 0.5f - extent + (index / GRID + 0.5f) * cellSize);
        if (!level.hasChunk(SectionPos.blockToSectionCoord(x), SectionPos.blockToSectionCoord(z))) {
            known.clear(index);
            settled.clear(index);
            return;
        }
        int surface = level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);
        if (surface <= level.getMinBuildHeight()) {
            known.clear(index);
            settled.clear(index);
            return;
        }
        target[index] = surface - (origin.getY() + 0.5f);
        known.set(index);
    }

    public float heightOffset(float dx, float dz) {
        float gx = (dx + extent) / cellSize - 0.5f;
        float gz = (dz + extent) / cellSize - 0.5f;
        int x0 = Mth.floor(gx);
        int z0 = Mth.floor(gz);
        if (x0 < 0 || z0 < 0 || x0 + 1 >= GRID || z0 + 1 >= GRID) return Float.NaN;
        float fx = gx - x0;
        float fz = gz - z0;
        float a = sample(x0, z0);
        float b = sample(x0 + 1, z0);
        float c = sample(x0, z0 + 1);
        float d = sample(x0 + 1, z0 + 1);
        if (Float.isNaN(a) || Float.isNaN(b) || Float.isNaN(c) || Float.isNaN(d)) return Float.NaN;
        return Mth.lerp(fz, Mth.lerp(fx, a, b), Mth.lerp(fx, c, d));
    }

    public float ringAverage(float radius, float fallback) {
        float sum = 0f;
        int count = 0;
        for (int i = 0; i < RING_SAMPLES; i++) {
            float angle = i * Mth.TWO_PI / RING_SAMPLES;
            float h = heightOffset(Mth.cos(angle) * radius, Mth.sin(angle) * radius);
            if (Float.isNaN(h)) continue;
            sum += h;
            count++;
        }
        return count == 0 ? fallback : sum / count;
    }

    private float sample(int x, int z) {
        int index = z * GRID + x;
        return known.get(index) && settled.get(index) ? current[index] : Float.NaN;
    }

    public float extent() {
        return extent;
    }

    public void clear() {
        known.clear();
        settled.clear();
        Arrays.fill(current, Float.NaN);
        cursor = 0;
    }
}
