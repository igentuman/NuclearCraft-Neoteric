package igentuman.nc.entity.goal;

import igentuman.nc.entity.EntityWastelandBoss;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

import static igentuman.nc.entity.EntityWastelandBoss.RADIATION_BURST_RANGE;

public class RadiationBurstGoal extends Goal {
    private final EntityWastelandBoss boss;
    private int attackAnimationTick;

    public RadiationBurstGoal(EntityWastelandBoss boss) {
        this.boss = boss;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (boss.radiationBurstCooldownRemaining > 0 || boss.isExecutingAttack) {
            return false;
        }

        LivingEntity target = boss.getTarget();
        if (target == null || !target.isAlive()) {
            return false;
        }

        // Check if target is within range and boss randomly decides to attack
        return boss.distanceTo(target) < RADIATION_BURST_RANGE &&
                boss.random.nextInt(30) == 0;
    }

    @Override
    public void start() {
        boss.getNavigation().stop();
        boss.executeRadiationBurst();
        attackAnimationTick = 30; // Animation duration
    }

    @Override
    public void tick() {
        if (attackAnimationTick > 0) {
            attackAnimationTick--;
            if (attackAnimationTick == 15) {
                // Do burst halfway through animation
                boss.lookAt(boss.getTarget(), 100.0F, 100.0F);
            }
        }
    }

    @Override
    public boolean canContinueToUse() {
        return attackAnimationTick > 0;
    }
}