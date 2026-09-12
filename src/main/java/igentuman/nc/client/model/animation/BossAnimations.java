package igentuman.nc.client.model.animation;

import igentuman.nc.entity.EntityWastelandBoss;

public final class BossAnimations {

    private BossAnimations() {
    }

    public static BossAnimation forEvent(byte event) {
        return switch (event) {
            case EntityWastelandBoss.ANIMATION_SLAM -> SlamAnimation.INSTANCE;
            case EntityWastelandBoss.ANIMATION_RADIATION_BURST -> RadiationBurstAnimation.INSTANCE;
            case EntityWastelandBoss.ANIMATION_SUMMON -> SummonAnimation.INSTANCE;
            case EntityWastelandBoss.ANIMATION_RANGED -> RangedAttackAnimation.INSTANCE;
            case EntityWastelandBoss.ANIMATION_MELEE -> AttackAnimation.INSTANCE;
            case EntityWastelandBoss.ANIMATION_THROW_SPAM -> ThrowSpamAnimation.INSTANCE;
            default -> null;
        };
    }
}
