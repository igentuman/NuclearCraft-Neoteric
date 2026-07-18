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
                boss.random.nextInt(20) == 0;
    }

    @Override
    public void start() {
        boss.getNavigation().stop();
        attackAnimationTick = 30;
        boss.level.broadcastEntityEvent(boss, (byte) 5);
    }

    @Override
    public void tick() {
        if (attackAnimationTick > 0) {
            attackAnimationTick--;
            if (attackAnimationTick == 15) {
                boss.executeRadiationBurst();
                boss.lookAt(boss.getTarget(), 100.0F, 100.0F);
            }
        }
    }

    @Override
    public boolean canContinueToUse() {
        return attackAnimationTick > 0;
    }
}