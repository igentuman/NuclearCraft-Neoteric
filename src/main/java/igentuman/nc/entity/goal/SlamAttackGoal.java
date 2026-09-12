package igentuman.nc.entity.goal;

import igentuman.nc.entity.EntityWastelandBoss;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class SlamAttackGoal extends Goal {

    private static final int ANIMATION_TICKS = 20;
    private static final int IMPACT_TICK = 10;

    private final EntityWastelandBoss boss;
    private int ticksRemaining;

    public SlamAttackGoal(EntityWastelandBoss boss) {
        this.boss = boss;
        setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = boss.getTarget();
        return boss.slamAttackCooldownRemaining <= 0
                && target != null
                && target.isAlive()
                && boss.distanceTo(target) < EntityWastelandBoss.SLAM_ATTACK_RANGE + 1.0F
                && boss.getRandom().nextInt(20) == 0;
    }

    @Override
    public void start() {
        boss.getNavigation().stop();
        ticksRemaining = ANIMATION_TICKS;
        boss.level().broadcastEntityEvent(boss, EntityWastelandBoss.ANIMATION_SLAM);
    }

    @Override
    public void tick() {
        LivingEntity target = boss.getTarget();
        if (target != null) {
            boss.getLookControl().setLookAt(target, 30.0F, 30.0F);
        }
        if (--ticksRemaining == IMPACT_TICK) {
            boss.executeSlamAttack();
        }
    }

    @Override
    public boolean canContinueToUse() {
        return ticksRemaining > 0;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }
}
