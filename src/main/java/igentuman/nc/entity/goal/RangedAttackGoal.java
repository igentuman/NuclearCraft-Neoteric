package igentuman.nc.entity.goal;

import igentuman.nc.entity.EntityWastelandBoss;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class RangedAttackGoal extends Goal {

    private static final int ANIMATION_TICKS = 30;
    private static final int THROW_TICK = 18;

    private final EntityWastelandBoss boss;
    private LivingEntity target;
    private int ticksRemaining;

    public RangedAttackGoal(EntityWastelandBoss boss) {
        this.boss = boss;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        target = boss.getTarget();
        if (boss.rangedAttackCooldownRemaining > 0 || target == null || !target.isAlive()) {
            return false;
        }
        double distanceSquared = boss.distanceToSqr(target);
        return distanceSquared >= EntityWastelandBoss.MIN_RANGED_ATTACK_DISTANCE * EntityWastelandBoss.MIN_RANGED_ATTACK_DISTANCE
                && distanceSquared <= EntityWastelandBoss.MAX_RANGED_ATTACK_DISTANCE * EntityWastelandBoss.MAX_RANGED_ATTACK_DISTANCE;
    }

    @Override
    public void start() {
        boss.getNavigation().stop();
        boss.setAggressive(true);
        ticksRemaining = ANIMATION_TICKS;
        boss.level().broadcastEntityEvent(boss, EntityWastelandBoss.ANIMATION_RANGED);
    }

    @Override
    public void tick() {
        if (target != null) {
            boss.getLookControl().setLookAt(target, 30.0F, 30.0F);
        }
        if (--ticksRemaining == THROW_TICK) {
            boss.executeRangedAttack();
        }
    }

    @Override
    public boolean canContinueToUse() {
        return ticksRemaining > 0 && target != null && target.isAlive();
    }

    @Override
    public void stop() {
        boss.setAggressive(false);
        target = null;
        ticksRemaining = 0;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }
}
