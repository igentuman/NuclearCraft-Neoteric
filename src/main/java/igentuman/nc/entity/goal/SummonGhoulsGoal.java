package igentuman.nc.entity.goal;

import igentuman.nc.entity.EntityWastelandBoss;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

import static igentuman.nc.entity.EntityWastelandBoss.SUMMON_RANGE;

/**
     * Goal that handles summoning feral ghouls near the target
     */
public class SummonGhoulsGoal extends Goal {
    private final EntityWastelandBoss boss;
    private int summonAnimationTick;

    public SummonGhoulsGoal(EntityWastelandBoss boss) {
        this.boss = boss;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (boss.summonCooldownRemaining > 0 || boss.isExecutingAttack) {
            return false;
        }

        LivingEntity target = boss.getTarget();
        if (target == null || !target.isAlive()) {
            return false;
        }

        // The boss should be at a medium distance to summon
        // Not too close (would prefer melee attack) and not too far (out of range)
        double distance = boss.distanceTo(target);
        return distance > 4.0 && distance < SUMMON_RANGE * 1.5 &&
                boss.random.nextInt(40) == 0;
    }

    @Override
    public void start() {
        boss.getNavigation().stop();
        boss.executeSummonAttack();
        summonAnimationTick = 40; // Animation duration
    }

    @Override
    public void tick() {
        if (summonAnimationTick > 0) {
            summonAnimationTick--;

            // Boss should look at players during the summoning animation
            if (boss.getTarget() != null && summonAnimationTick % 10 == 0) {
                boss.lookAt(boss.getTarget(), 100.0F, 100.0F);
            }
        }
    }

    @Override
    public boolean canContinueToUse() {
        return summonAnimationTick > 0;
    }
}