package igentuman.nc.entity.goal;

import igentuman.nc.entity.EntityWastelandBoss;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class RangedAttackGoal extends Goal {
    private final EntityWastelandBoss boss;
    private LivingEntity target;
    private final float minAttackDistance;
    private final float maxAttackDistance;
    private int animationTimeout = 0;
    private boolean executed = false;
    public RangedAttackGoal(EntityWastelandBoss boss) {
        this.boss = boss;
        this.minAttackDistance = EntityWastelandBoss.MIN_RANGED_ATTACK_DISTANCE;
        this.maxAttackDistance = EntityWastelandBoss.MAX_RANGED_ATTACK_DISTANCE;
        this.setFlags(EnumSet.of(Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        target = boss.getTarget();
        if (target == null || !target.isAlive()) {
            return false;
        }

        double distanceToTarget = boss.distanceToSqr(target);
        return boss.rangedAttackCooldownRemaining <= 0 &&
               !boss.isExecutingAttack &&
               distanceToTarget >= minAttackDistance * minAttackDistance &&
               distanceToTarget <= maxAttackDistance * maxAttackDistance;
    }

    @Override
    public boolean canContinueToUse() {
        return animationTimeout > 0 && target != null && target.isAlive();
    }

    @Override
    public void start() {
        super.start();
        boss.setAggressive(true);
        animationTimeout = 70;
        boss.level.broadcastEntityEvent(boss, (byte) 7);
        executed = false;
    }

    @Override
    public void stop() {
        super.stop();
        target = null;
        boss.setAggressive(false);
        executed = false;
    }

    @Override
    public void tick() {
        if(animationTimeout > 0) {
            animationTimeout--;
        }
        if (target == null || !target.isAlive()) {
            return;
        }
        boss.getLookControl().setLookAt(target, 30.0F, 30.0F);
        if (animationTimeout < 60 && !executed) {
            executed = true;
            boss.executeRangedAttack();
        }
    }
}
