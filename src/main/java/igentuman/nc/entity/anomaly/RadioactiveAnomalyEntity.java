package igentuman.nc.entity.anomaly;

import igentuman.nc.config.Common;
import igentuman.nc.util.NCDamageSources;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;

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
        if (!onInterval(Common.ANOMALY_CONFIG.RAD_INTERVAL.get())) {
            return;
        }
        long dose = Common.ANOMALY_CONFIG.RAD_DOSE.get();
        if (dose <= 0L) {
            return;
        }
        double radius = Common.ANOMALY_CONFIG.RAD_RADIUS.get();
        for (LivingEntity living : livingInRadius(radius)) {
            double dist = living.position().distanceTo(position());
            double falloff = Math.max(0.0D, 1.0D - dist / radius);
            float damage = (float) (2.0D + 18.0D * falloff);
            living.hurt(NCDamageSources.of(level(), NCDamageSources.RADIATION), damage);
        }
    }

    @Override
    protected boolean checkCounterplay() {
        return false;
    }
}
