package igentuman.nc.world.anomaly;

import igentuman.nc.entity.anomaly.AnomalyEntity;
import igentuman.nc.entity.anomaly.AnomalyType;
import igentuman.nc.entity.anomaly.TeleportingAnomalyEntity;
import igentuman.nc.setup.registration.Entities;
import igentuman.nc.world.dimension.Dimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.Optional;

import static igentuman.nc.NuclearCraft.currentTick;
import static igentuman.nc.handler.config.CommonConfig.ANOMALY_CONFIG;

public final class AnomalySpawnManager {

    /** Cadence of the (already throttled) caller; keeps scan cost low. */
    private static final int RUN_INTERVAL_TICKS = 40;

    private AnomalySpawnManager() {
    }

    public static void tick(ServerLevel level) {
        if (!ANOMALY_CONFIG.ENABLED.get()) {
            return;
        }
        if (level.dimension() != Dimensions.WASTELAND) {
            return;
        }
        if (currentTick % RUN_INTERVAL_TICKS != 0) {
            return;
        }
        List<ServerPlayer> players = level.players();
        if (players.isEmpty()) {
            return;
        }

        long seed = level.getSeed();
        double spawnChance = ANOMALY_CONFIG.SPAWN_CHANCE_PER_CELL.get();
        int cellSize = ANOMALY_CONFIG.CELL_SIZE.get();
        int cellRadius = ANOMALY_CONFIG.ACTIVATION_CELL_RADIUS.get();
        int[] weights = weights();
        AnomalySavedData saved = AnomalySavedData.get(level);

        for (ServerPlayer player : players) {
            int pcx = AnomalyPlacement.cellIndex((int) Math.floor(player.getX()), cellSize);
            int pcz = AnomalyPlacement.cellIndex((int) Math.floor(player.getZ()), cellSize);
            for (int cx = pcx - cellRadius; cx <= pcx + cellRadius; cx++) {
                for (int cz = pcz - cellRadius; cz <= pcz + cellRadius; cz++) {
                    Optional<AnomalyPlacement.PlannedAnomaly> planned =
                            AnomalyPlacement.forCell(seed, cx, cz, spawnChance, cellSize, weights);
                    planned.ifPresent(p -> trySpawn(level, saved, p));
                }
            }
        }
    }

    private static void trySpawn(ServerLevel level, AnomalySavedData saved, AnomalyPlacement.PlannedAnomaly planned) {
        if (saved.isDestroyed(planned.cellId())) {
            return;
        }
        BlockPos column = new BlockPos(planned.x(), 0, planned.z());
        if (!level.hasChunkAt(column)) {
            return;
        }
        int surface = level.getHeight(Heightmap.Types.WORLD_SURFACE, planned.x(), planned.z());
        if (isAnomalyAlreadyPresent(level, planned)) {
            return;
        }
        EntityType<? extends AnomalyEntity> type = entityTypeFor(planned.type());
        if (type == null) {
            return;
        }
        AnomalyEntity anomaly = type.create(level);
        if (anomaly == null) {
            return;
        }
        int hover = planned.type() == AnomalyType.TELEPORTING ? ANOMALY_CONFIG.TP_HOVER_OFFSET.get() : 0;
        anomaly.moveTo(planned.x() + 0.5D, surface + 1 + hover, planned.z() + 0.5D, 0.0F, 0.0F);
        anomaly.setCellId(planned.cellId());
        if (anomaly instanceof TeleportingAnomalyEntity tp) {
            tp.setAnchor(planned.x() + 0.5D, planned.z() + 0.5D);
        }
        level.addFreshEntity(anomaly);
    }

    private static boolean isAnomalyAlreadyPresent(ServerLevel level, AnomalyPlacement.PlannedAnomaly planned) {
        int pad = Math.max(3, (int) Math.ceil(ANOMALY_CONFIG.TP_SELF_RADIUS.get()) + 2);
        AABB box = new AABB(planned.x() - pad, level.getMinBuildHeight(), planned.z() - pad,
                planned.x() + pad + 1, level.getMaxBuildHeight(), planned.z() + pad + 1);
        for (AnomalyEntity existing : level.getEntitiesOfClass(AnomalyEntity.class, box)) {
            if (existing.getCellId() == planned.cellId()) {
                return true;
            }
        }
        return false;
    }

    private static int[] weights() {
        return new int[]{
                ANOMALY_CONFIG.ENABLE_GRAVITATIONAL.get() ? ANOMALY_CONFIG.WEIGHT_GRAVITATIONAL.get() : 0,
                ANOMALY_CONFIG.ENABLE_ELECTRIC.get() ? ANOMALY_CONFIG.WEIGHT_ELECTRIC.get() : 0,
                ANOMALY_CONFIG.ENABLE_RADIOACTIVE.get() ? ANOMALY_CONFIG.WEIGHT_RADIOACTIVE.get() : 0,
                ANOMALY_CONFIG.ENABLE_BURNING.get() ? ANOMALY_CONFIG.WEIGHT_BURNING.get() : 0,
                ANOMALY_CONFIG.ENABLE_PSYCHO.get() ? ANOMALY_CONFIG.WEIGHT_PSYCHO.get() : 0,
                ANOMALY_CONFIG.ENABLE_TELEPORTING.get() ? ANOMALY_CONFIG.WEIGHT_TELEPORTING.get() : 0
        };
    }

    private static EntityType<? extends AnomalyEntity> entityTypeFor(AnomalyType type) {
        return switch (type) {
            case GRAVITATIONAL -> Entities.GRAVITATIONAL_ANOMALY.get();
            case ELECTRIC -> Entities.ELECTRIC_ANOMALY.get();
            case RADIOACTIVE -> Entities.RADIOACTIVE_ANOMALY.get();
            case BURNING -> Entities.BURNING_ANOMALY.get();
            case PSYCHO -> Entities.PSYCHO_ANOMALY.get();
            case TELEPORTING -> Entities.TELEPORTING_ANOMALY.get();
        };
    }
}
