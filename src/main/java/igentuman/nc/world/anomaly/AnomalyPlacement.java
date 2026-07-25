package igentuman.nc.world.anomaly;

import igentuman.nc.entity.anomaly.AnomalyType;
import net.minecraft.world.level.ChunkPos;

import java.util.Optional;
import java.util.Random;

public final class AnomalyPlacement {

    private AnomalyPlacement() {
    }

    public static int cellIndex(int worldCoord, int cellSize) {
        return Math.floorDiv(worldCoord, cellSize);
    }

    public static long cellId(int cx, int cz) {
        return ChunkPos.asLong(cx, cz);
    }

    public static long mix(long worldSeed, int cx, int cz) {
        long h = worldSeed;
        h ^= (long) cx * 0x9E3779B97F4A7C15L;
        h = (h ^ (h >>> 30)) * 0xBF58476D1CE4E5B9L;
        h ^= (long) cz * 0xC2B2AE3D27D4EB4FL;
        h = (h ^ (h >>> 27)) * 0x94D049BB133111EBL;
        h ^= h >>> 31;
        return h;
    }

    public static Optional<PlannedAnomaly> forCell(long worldSeed, int cx, int cz,
                                                   double spawnChance, int cellSize, int[] weights) {
        if (spawnChance <= 0.0D || cellSize <= 0) {
            return Optional.empty();
        }
        Random rng = new Random(mix(worldSeed, cx, cz));
        if (rng.nextDouble() >= spawnChance) {
            return Optional.empty();
        }
        AnomalyType type = weightedPick(rng, weights);
        if (type == null) {
            return Optional.empty();
        }
        int ox = rng.nextInt(cellSize);
        int oz = rng.nextInt(cellSize);
        int worldX = Math.multiplyExact(cx, cellSize) + ox;
        int worldZ = Math.multiplyExact(cz, cellSize) + oz;
        return Optional.of(new PlannedAnomaly(cellId(cx, cz), worldX, worldZ, type));
    }

    public static AnomalyType weightedPick(Random rng, int[] weights) {
        AnomalyType[] types = AnomalyType.values();
        int total = 0;
        for (int i = 0; i < types.length; i++) {
            int w = (weights != null && i < weights.length) ? Math.max(0, weights[i]) : 0;
            total += w;
        }
        if (total <= 0) {
            return null;
        }
        int roll = rng.nextInt(total);
        for (int i = 0; i < types.length; i++) {
            int w = (weights != null && i < weights.length) ? Math.max(0, weights[i]) : 0;
            if (w <= 0) {
                continue;
            }
            roll -= w;
            if (roll < 0) {
                return types[i];
            }
        }
        return null;
    }

    public record PlannedAnomaly(long cellId, int x, int z, AnomalyType type) {
    }
}
