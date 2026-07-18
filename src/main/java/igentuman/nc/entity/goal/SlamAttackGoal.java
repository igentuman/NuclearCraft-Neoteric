package igentuman.nc.entity.goal;

import igentuman.nc.entity.EntityWastelandBoss;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

import static igentuman.nc.entity.EntityWastelandBoss.SLAM_ATTACK_RANGE;

public class SlamAttackGoal extends Goal {
    private final EntityWastelandBoss boss;
    private int attackAnimationTick;

    public SlamAttackGoal(EntityWastelandBoss boss) {
        this.boss = boss;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.JUMP, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (boss.slamAttackCooldownRemaining > 0 || boss.isExecutingAttack) {
            return false;
        }

        LivingEntity target = boss.getTarget();
        if (target == null || !target.isAlive()) {
            return false;
        }

        // Check if target is within range and boss has line of sight
        return boss.distanceTo(target) < SLAM_ATTACK_RANGE + 1.0F &&
                boss.random.nextInt(20) == 0;
    }

    @Override
    public void start() {
        boss.getNavigation().stop();
        attackAnimationTick = 15;
        boss.level.broadcastEntityEvent(boss, (byte) 4);
    }

    @Override
    public void tick() {
        if (attackAnimationTick > 0) {
            attackAnimationTick--;
        }
        if(attackAnimationTick == 10) {
            boss.executeSlamAttack();
        }
    }

    @Override
    public boolean canContinueToUse() {
        return attackAnimationTick > 0;
    }
}