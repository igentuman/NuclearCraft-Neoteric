package igentuman.nc.entity.goal;

import igentuman.nc.entity.EntityWastelandBoss;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class ThrowSpamGoal extends Goal {
    private final EntityWastelandBoss boss;
    private LivingEntity target;
    private int animationTimeout = 0;
    private boolean executed = false;

    public ThrowSpamGoal(EntityWastelandBoss boss) {
        this.boss = boss;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        target = boss.getTarget();
        if (target == null || !target.isAlive()) {
            return false;
        }

        // Only activate when boss is enraged (health below 50%)
        if (!boss.isEnraged()) {
            return false;
        }

        double distanceToTarget = boss.distanceToSqr(target);
        return boss.throwSpamCooldownRemaining <= 0 &&
               !boss.isExecutingAttack &&
               boss.throwSpamDurationRemaining <= 0 &&
               distanceToTarget <= EntityWastelandBoss.THROW_SPAM_RANGE * EntityWastelandBoss.THROW_SPAM_RANGE;
    }

    @Override
    public boolean canContinueToUse() {
        return (animationTimeout > 0 || boss.throwSpamDurationRemaining > 0) && 
               target != null && target.isAlive();
    }

    @Override
    public void start() {
        super.start();
        boss.setAggressive(true);
        animationTimeout = 40; // 2 second wind-up
        boss.level.broadcastEntityEvent(boss, (byte) 9); // Custom event for throw spam
        executed = false;
    }

    @Override
    public void stop() {
        super.stop();
        target = null;
        boss.setAggressive(false);
        executed = false;
        boss.throwSpamDurationRemaining = 0; // Stop the spam attack if goal is interrupted
    }

    @Override
    public void tick() {
        if (target == null || !target.isAlive()) {
            return;
        }

        // Look at target during the attack
        boss.getLookControl().setLookAt(target, 30.0F, 30.0F);
        
        // Wind-up phase
        if (animationTimeout > 0) {
            animationTimeout--;
            // Stop moving during wind-up
            boss.getNavigation().stop();
            
            // Execute the attack start when wind-up is almost done
            if (animationTimeout <= 5 && !executed) {
                executed = true;
                boss.executeThrowSpamAttack();
            }
        }
        
        // Spam throwing phase
        if (boss.throwSpamDurationRemaining > 0) {
            // Stop moving during spam attack
            boss.getNavigation().stop();
            
            // Continuously throw projectiles
            boss.throwSpamProjectile();
        }
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }
}