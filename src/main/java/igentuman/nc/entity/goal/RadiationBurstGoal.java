package igentuman.nc.entity.goal;

import igentuman.nc.entity.EntityWastelandBoss;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class RadiationBurstGoal extends Goal {

    private static final int ANIMATION_TICKS = 30;
    private static final int BURST_TICK = 15;

    private final EntityWastelandBoss boss;
    private int ticksRemaining;

    public RadiationBurstGoal(EntityWastelandBoss boss) {
        this.boss = boss;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = boss.getTarget();
        return boss.radiationBurstCooldownRemaining <= 0
                && target != null
                && target.isAlive()
                && boss.distanceTo(target) < EntityWastelandBoss.RADIATION_BURST_RANGE
                && boss.getRandom().nextInt(20) == 0;
    }

    @Override
    public void start() {
        boss.getNavigation().stop();
        ticksRemaining = ANIMATION_TICKS;
        boss.level().broadcastEntityEvent(boss, EntityWastelandBoss.ANIMATION_RADIATION_BURST);
    }

    @Override
    public void tick() {
        LivingEntity target = boss.getTarget();
        if (target != null) {
            boss.getLookControl().setLookAt(target, 30.0F, 30.0F);
        }
        if (--ticksRemaining == BURST_TICK) {
            boss.executeRadiationBurst();
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
