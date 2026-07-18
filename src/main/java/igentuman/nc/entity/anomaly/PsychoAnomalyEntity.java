package igentuman.nc.entity.anomaly;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.List;

import static igentuman.nc.handler.config.CommonConfig.ANOMALY_CONFIG;

/**
 * Inflicts disorienting status effects on players within its (large) radius. Non-player living
 * entities are exempt by design. Counterplay is the Q36 beam (direct hit), handled by the base.
 */
public class PsychoAnomalyEntity extends AnomalyEntity {

    private int vexTimer = -1;

    public PsychoAnomalyEntity(EntityType<? extends Mob> type, Level level) {
        super(type, level);
    }

    @Override
    public AnomalyType getAnomalyType() {
        return AnomalyType.PSYCHO;
    }

    @Override
    protected double ambientParticleRadius() {
        return 2.5D;
    }

    @Override
    protected void applyEffects() {
        tickVexSpawn();

        int interval = ANOMALY_CONFIG.PSYCHO_INTERVAL.get();
        if (!onInterval(interval)) {
            return;
        }
        double radius = ANOMALY_CONFIG.PSYCHO_RADIUS.get();
        double r2 = radius * radius;
        int amplifier = ANOMALY_CONFIG.PSYCHO_AMPLIFIER.get();
        int duration = interval + 40;
        List<Player> players = level.getEntitiesOfClass(Player.class, effectBox(radius),
                p -> p.isAlive() && !p.isCreative() && !p.isSpectator()
                        && p.position().distanceToSqr(position()) <= r2);
        for (Player player : players) {
            player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, duration, amplifier, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, duration, amplifier, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, duration, amplifier, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, amplifier, false, false));
        }
    }

    @Override
    protected boolean checkCounterplay() {
        return false;
    }

    @Override
    protected boolean acceptsQ36Counterplay() {
        return true;
    }

    /** Periodically conjures a vex near the anomaly while a player is in range, up to a nearby cap. */
    private void tickVexSpawn() {
        if (vexTimer < 0) {
            vexTimer = nextVexDelay();
        }
        if (--vexTimer > 0) {
            return;
        }
        vexTimer = nextVexDelay();

        int max = ANOMALY_CONFIG.PSYCHO_VEX_MAX.get();
        if (max <= 0) {
            return;
        }
        double radius = ANOMALY_CONFIG.PSYCHO_RADIUS.get();
        if (!hasPlayerInRadius(radius)) {
            return;
        }
        if (level.getEntitiesOfClass(Vex.class, effectBox(radius)).size() >= max) {
            return;
        }
        spawnVex();
    }

    private int nextVexDelay() {
        int base = Math.max(1, ANOMALY_CONFIG.PSYCHO_VEX_INTERVAL.get());
        int jitter = Math.max(1, base / 4);
        return base - jitter + random.nextInt(jitter * 2 + 1);
    }

    private boolean hasPlayerInRadius(double radius) {
        double r2 = radius * radius;
        return !level.getEntitiesOfClass(Player.class, effectBox(radius),
                p -> p.isAlive() && !p.isSpectator()
                        && p.position().distanceToSqr(position()) <= r2).isEmpty();
    }

    private void spawnVex() {
        if (!(level instanceof ServerLevel server)) {
            return;
        }
        Vex vex = EntityType.VEX.create(server);
        if (vex == null) {
            return;
        }
        double angle = random.nextDouble() * Math.PI * 2.0;
        double dist = 1.0 + random.nextDouble() * 3.0;
        double x = getX() + Math.cos(angle) * dist;
        double z = getZ() + Math.sin(angle) * dist;
        double y = getY() + random.nextDouble() * 2.0;
        vex.moveTo(x, y, z, random.nextFloat() * 360.0F, 0.0F);
        vex.finalizeSpawn(server, server.getCurrentDifficultyAt(vex.blockPosition()),
                MobSpawnType.MOB_SUMMONED, null, null);
        int lifetime = ANOMALY_CONFIG.PSYCHO_VEX_LIFETIME.get();
        if (lifetime > 0) {
            vex.setLimitedLife(lifetime);
        }
        server.addFreshEntity(vex);
    }
}
