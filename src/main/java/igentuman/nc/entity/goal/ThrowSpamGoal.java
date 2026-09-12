package igentuman.nc.entity.goal;

import igentuman.nc.entity.EntityWastelandBoss;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class ThrowSpamGoal extends Goal {

    private static final int WINDUP_TICKS = 40;

    private final EntityWastelandBoss boss;
    private LivingEntity target;
    private int windupRemaining;

    public ThrowSpamGoal(EntityWastelandBoss boss) {
        this.boss = boss;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        target = boss.getTarget();
        return boss.isEnraged()
                && boss.throwSpamCooldownRemaining <= 0
                && boss.throwSpamDurationRemaining <= 0
                && target != null
                && target.isAlive()
                && boss.distanceToSqr(target) <= EntityWastelandBoss.THROW_SPAM_RANGE * EntityWastelandBoss.THROW_SPAM_RANGE;
    }

    @Override
    public void start() {
        boss.getNavigation().stop();
        boss.setAggressive(true);
        windupRemaining = WINDUP_TICKS;
        boss.level().broadcastEntityEvent(boss, EntityWastelandBoss.ANIMATION_THROW_SPAM);
    }

    @Override
    public void tick() {
        if (target == null || !target.isAlive()) {
            return;
        }
        boss.getNavigation().stop();
        boss.getLookControl().setLookAt(target, 30.0F, 30.0F);
        if (windupRemaining > 0 && --windupRemaining == 5) {
            boss.startThrowSpamAttack();
        }
        if (boss.throwSpamDurationRemaining > 0) {
            boss.throwSpamProjectile();
        }
    }

    @Override
    public boolean canContinueToUse() {
        return (windupRemaining > 0 || boss.throwSpamDurationRemaining > 0)
                && target != null
                && target.isAlive();
    }

    @Override
    public void stop() {
        boss.setAggressive(false);
        boss.throwSpamDurationRemaining = 0;
        windupRemaining = 0;
        target = null;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }
}
