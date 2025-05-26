package igentuman.nc.entity.goal;

import igentuman.nc.entity.EntityWastelandBoss;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class RangedAttackGoal extends Goal {
    private final EntityWastelandBoss boss;
    private LivingEntity target;
    private int timeUntilNextAttack = 0;
    private final float minAttackDistance;
    private final float maxAttackDistance;
    private boolean strafingClockwise = false;
    private boolean strafingBackwards = false;
    private int strafingTime = -1;

    public RangedAttackGoal(EntityWastelandBoss boss) {
        this.boss = boss;
        this.minAttackDistance = EntityWastelandBoss.MIN_RANGED_ATTACK_DISTANCE;
        this.maxAttackDistance = EntityWastelandBoss.MAX_RANGED_ATTACK_DISTANCE;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        target = boss.getTarget();
        if (target == null || !target.isAlive()) {
            return false;
        }

        double distanceToTarget = boss.distanceToSqr(target);
        // Use ranged attack only when target is between min and max attack distance
        return boss.rangedAttackCooldownRemaining <= 0 &&
               !boss.isExecutingAttack &&
               distanceToTarget >= minAttackDistance * minAttackDistance &&
               distanceToTarget <= maxAttackDistance * maxAttackDistance;
    }

    @Override
    public boolean canContinueToUse() {
        return this.canUse() || !boss.getNavigation().isDone();
    }

    @Override
    public void start() {
        super.start();
        boss.setAggressive(true);
    }

    @Override
    public void stop() {
        super.stop();
        target = null;
        timeUntilNextAttack = 0;
        boss.setAggressive(false);
    }

    @Override
    public void tick() {
        // Make sure we still have a valid target
        if (target == null || !target.isAlive()) {
            return;
        }

        double distanceToTarget = boss.distanceToSqr(target);
        boolean canSeeTarget = boss.getSensing().hasLineOfSight(target);

        // Update strafing behavior
        if (canSeeTarget) {
            // Increment strafing counter
            ++this.strafingTime;
        } else {
            // Reset strafing counter if target not visible
            this.strafingTime = -1;
        }

        // Change strafing direction occasionally
        if (this.strafingTime >= 20) {
            if (boss.getRandom().nextFloat() < 0.3) {
                this.strafingClockwise = !this.strafingClockwise;
            }
            if (boss.getRandom().nextFloat() < 0.3) {
                this.strafingBackwards = !this.strafingBackwards;
            }
            this.strafingTime = 0;
        }

        // Determine if we should move backwards based on distance
        if (distanceToTarget < (minAttackDistance * 0.75) * (minAttackDistance * 0.75)) {
            this.strafingBackwards = true;
        } else if (distanceToTarget > minAttackDistance * minAttackDistance) {
            this.strafingBackwards = false;
        }

        // Apply strafing movement when in proper attack range
        if (distanceToTarget <= maxAttackDistance * maxAttackDistance &&
            distanceToTarget >= minAttackDistance * minAttackDistance * 0.75) {
            boss.getMoveControl().strafe(strafingBackwards ? -0.5F : 0.5F,
                                        strafingClockwise ? 0.5F : -0.5F);
            boss.lookAt(target, 30.0F, 30.0F);
        } else {
            // If we're out of range, move toward the target normally
            boss.getNavigation().moveTo(target, 1.0);
        }

        // Always keep looking at the target
        boss.getLookControl().setLookAt(target, 30.0F, 30.0F);

        // Perform the attack if we can see the target and cooldown has expired
        if (canSeeTarget && boss.rangedAttackCooldownRemaining <= 0 && !boss.isExecutingAttack) {
            // Execute the ranged attack
            boss.executeRangedAttack();
        }
    }
}
