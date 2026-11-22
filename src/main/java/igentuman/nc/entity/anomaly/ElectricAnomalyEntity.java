package igentuman.nc.entity.anomaly;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import igentuman.nc.config.Common;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;

import java.util.List;

public class ElectricAnomalyEntity extends AnomalyEntity {

    public ElectricAnomalyEntity(EntityType<? extends Mob> type, Level level) {
        super(type, level);
    }

    @Override
    public AnomalyType getAnomalyType() {
        return AnomalyType.ELECTRIC;
    }

    @Override
    protected double ambientParticleRadius() {
        return 6.0D;
    }

    @Override
    protected void applyEffects() {
        if (!onInterval(Common.ANOMALY_CONFIG.ELECTRIC_INTERVAL.get())) {
            return;
        }
        List<LivingEntity> targets = livingInRadius(Common.ANOMALY_CONFIG.ELECTRIC_RADIUS.get());
        if (targets.isEmpty()) {
            return;
        }
        LivingEntity target = targets.get(random.nextInt(targets.size()));
        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level(), EntitySpawnReason.TRIGGERED);
        if (bolt != null) {
            bolt.snapTo(target.getX(), target.getY(), target.getZ());
            bolt.setVisualOnly(true);
            level().addFreshEntity(bolt);
        }
        target.hurtServer((ServerLevel) level(), damageSources().lightningBolt(), (float) (double) Common.ANOMALY_CONFIG.ELECTRIC_DAMAGE.get());
    }

    @Override
    protected boolean checkCounterplay() {
        int threshold = Common.ANOMALY_CONFIG.ELECTRIC_DISCHARGE_FE.get();
        BlockPos center = blockPosition();
        for (int dx = -13; dx <= 13; dx++) {
            for (int dy = -8; dy <= 8; dy++) {
                for (int dz = -13; dz <= 13; dz++) {
                    BlockPos pos = center.offset(dx, dy, dz);
                    if (!level().isLoaded(pos)) {
                        continue;
                    }
                    BlockEntity be = level().getBlockEntity(pos);
                    if (be == null) {
                        continue;
                    }
                    if (tryDischargeInto(pos, threshold)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean tryDischargeInto(BlockPos pos, int threshold) {
        for (Direction side : Direction.values()) {
            EnergyHandler storage = level().getCapability(Capabilities.Energy.BLOCK, pos, side);
            if (storage == null) continue;
            if (storage.getCapacityAsLong() - storage.getAmountAsLong() >= threshold) {
                try (var transaction = Transaction.openRoot()) {
                    if (storage.insert(threshold, transaction) == threshold) {
                        transaction.commit();
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
