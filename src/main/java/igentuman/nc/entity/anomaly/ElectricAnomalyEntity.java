package igentuman.nc.entity.anomaly;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;

import java.util.List;

import static igentuman.nc.handler.config.CommonConfig.ANOMALY_CONFIG;

/**
 * Strikes living entities in range with (visual-only) lightning plus direct electric damage.
 * Counterplay: place a battery with a large free buffer adjacent to it — the anomaly discharges into
 * the battery and dissipates. Uses the Forge energy capability only (the battery's GregTech cap and
 * per-side config are ignored).
 */
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
        if (!onInterval(ANOMALY_CONFIG.ELECTRIC_INTERVAL.get())) {
            return;
        }
        List<LivingEntity> targets = livingInRadius(ANOMALY_CONFIG.ELECTRIC_RADIUS.get());
        if (targets.isEmpty()) {
            return;
        }
        LivingEntity target = targets.get(random.nextInt(targets.size()));
        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
        if (bolt != null) {
            bolt.moveTo(target.getX(), target.getY(), target.getZ());
            bolt.setVisualOnly(true);
            level.addFreshEntity(bolt);
        }
        target.hurt(DamageSource.LIGHTNING_BOLT, (float) (double) ANOMALY_CONFIG.ELECTRIC_DAMAGE.get());
    }

    @Override
    protected boolean checkCounterplay() {
        int threshold = ANOMALY_CONFIG.ELECTRIC_DISCHARGE_FE.get();
        BlockPos center = blockPosition();
        for (int dx = -13; dx <= 13; dx++) {
            for (int dy = -8; dy <= 8; dy++) {
                for (int dz = -13; dz <= 13; dz++) {
                    BlockPos pos = center.offset(dx, dy, dz);
                    if (!level.isLoaded(pos)) {
                        continue;
                    }
                    BlockEntity be = level.getBlockEntity(pos);
                    if (be == null) {
                        continue;
                    }
                    if (tryDischargeInto(be, threshold)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean tryDischargeInto(BlockEntity be, int threshold) {
        for (Direction side : Direction.values()) {
            IEnergyStorage storage = be.getCapability(ForgeCapabilities.ENERGY, side).orElse(null);
            if (storage == null || !storage.canReceive()) {
                continue;
            }
            int free = storage.getMaxEnergyStored() - storage.getEnergyStored();
            if (free >= threshold) {
                storage.receiveEnergy(threshold, false);
                return true;
            }
        }
        return false;
    }
}
