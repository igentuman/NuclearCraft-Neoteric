package igentuman.nc.entity.goal;

import igentuman.nc.entity.EntityWastelandBoss;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class SummonGhoulsGoal extends Goal {

    private static final int ANIMATION_TICKS = 40;
    private static final int SUMMON_TICK = 20;

    private final EntityWastelandBoss boss;
    private int ticksRemaining;

    public SummonGhoulsGoal(EntityWastelandBoss boss) {
        this.boss = boss;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = boss.getTarget();
        if (boss.summonCooldownRemaining > 0 || target == null || !target.isAlive()) {
            return false;
        }
        double distance = boss.distanceTo(target);
        return distance > 4.0D
                && distance < EntityWastelandBoss.SUMMON_RANGE * 1.5D
                && boss.getRandom().nextInt(40) == 0;
    }

    @Override
    public void start() {
        boss.getNavigation().stop();
        ticksRemaining = ANIMATION_TICKS;
        boss.level().broadcastEntityEvent(boss, EntityWastelandBoss.ANIMATION_SUMMON);
    }

    @Override
    public void tick() {
        LivingEntity target = boss.getTarget();
        if (target != null) {
            boss.getLookControl().setLookAt(target, 30.0F, 30.0F);
        }
        if (--ticksRemaining == SUMMON_TICK) {
            boss.executeSummonAttack();
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
