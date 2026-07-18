package igentuman.nc.entity.anomaly;

import igentuman.nc.content.NCRadiationDamageSource;
import igentuman.nc.radiation.data.PlayerRadiationProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import static igentuman.nc.handler.config.CommonConfig.ANOMALY_CONFIG;

/**
 * Irradiates living entities within its radius, applying a dose directly to each entity's radiation
 * capability (scaled by distance falloff) rather than emitting into world chunks. No counterplay — a
 * radioactive anomaly is permanent.
 */
public class RadioactiveAnomalyEntity extends AnomalyEntity {

    public RadioactiveAnomalyEntity(EntityType<? extends Mob> type, Level level) {
        super(type, level);
    }

    @Override
    public AnomalyType getAnomalyType() {
        return AnomalyType.RADIOACTIVE;
    }

    @Override
    protected double ambientParticleRadius() {
        return 3.0D;
    }

    @Override
    protected void applyEffects() {
        if (!onInterval(ANOMALY_CONFIG.RAD_INTERVAL.get())) {
            return;
        }
        long dose = ANOMALY_CONFIG.RAD_DOSE.get();
        if (dose <= 0L) {
            return;
        }
        double radius = ANOMALY_CONFIG.RAD_RADIUS.get();
        for (LivingEntity living : livingInRadius(radius)) {
            double dist = living.position().distanceTo(position());
            double falloff = Math.max(0.0D, 1.0D - dist / radius);
            if (!(living instanceof Player)) {
                float damage = (float) (2.0D + 18.0D * falloff);
                living.hurt(NCRadiationDamageSource.RADIATION(level), damage);
                continue;
            }
            long applied = (long) (dose * falloff);
            if (applied <= 0L) {
                continue;
            }
            living.getCapability(PlayerRadiationProvider.PLAYER_RADIATION).ifPresent(rad ->
                    rad.setRadiation(rad.getRadiation() + applied));
        }
    }

    @Override
    protected boolean checkCounterplay() {
        return false;
    }
}
