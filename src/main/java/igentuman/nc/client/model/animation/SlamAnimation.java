package igentuman.nc.client.model.animation;

import igentuman.nc.client.model.ModelWastelandBoss;
import net.minecraft.util.Mth;

public class SlamAnimation implements BossAnimation {
    public static SlamAnimation instance = new SlamAnimation();

    @Override
    public void playAnimation(ModelWastelandBoss<?> boss, float deltaTime) {
        float normalizedTime = deltaTime / getAnimationDuration();

        // Define animation phases
        float raiseArmsPhase = 0.3f;        // First 30% - raising arms up
        float windupPhase = 0.4f;           // 10% pause at the top
        float slamPhase = 0.6f;             // 20% quick slam down
        float groundImpactPhase = 0.8f;     // 20% impact and hold
        float recoveryPhase = 1.0f;         // 20% return to neutral

        // Body adjustments during different phases
        if (normalizedTime < raiseArmsPhase) {
            // Phase 1: Raising arms up
            float raisingProgress = normalizedTime / raiseArmsPhase;

            // Arms go up
            float armRotation = Mth.lerp(raisingProgress, 0f, -2.0f); // Negative value to rotate upward
            boss.rightArm.xRot = armRotation;
            boss.leftArm.xRot = armRotation;

            // Body leans back slightly to counterbalance
            boss.body.xRot = Mth.lerp(raisingProgress, 0f, -0.2f);
            boss.head.xRot = Mth.lerp(raisingProgress, 0f, 0.3f); // Head looks up
        }
        else if (normalizedTime < windupPhase) {
            // Phase 2: Brief pause at top with slight anticipatory motion
            float pauseProgress = (normalizedTime - raiseArmsPhase) / (windupPhase - raiseArmsPhase);

            // Arms at top position with slight movement
            float armPauseMovement = Mth.sin(pauseProgress * 6.28f) * 0.1f; // Small oscillation
            boss.rightArm.xRot = -2.0f + armPauseMovement;
            boss.leftArm.xRot = -2.0f + armPauseMovement;

            // Body positioning
            boss.body.xRot = -0.2f;
            boss.head.xRot = 0.3f;
        }
        else if (normalizedTime < slamPhase) {
            // Phase 3: Quick slam down
            float slamProgress = (normalizedTime - windupPhase) / (slamPhase - windupPhase);

            // Arms quickly slam down
            float armRotation = Mth.lerp(slamProgress, -2.0f, 1.5f); // From up to past neutral (overextended)
            boss.rightArm.xRot = armRotation;
            boss.leftArm.xRot = armRotation;

            // Body lunges forward with the slam
            boss.body.xRot = Mth.lerp(slamProgress, -0.2f, 0.4f);
            boss.head.xRot = Mth.lerp(slamProgress, 0.3f, -0.3f); // Head looks down
        }
        else if (normalizedTime < groundImpactPhase) {
            // Phase 4: Ground impact and hold
            float impactProgress = (normalizedTime - slamPhase) / (groundImpactPhase - slamPhase);

            // Arms stay extended with impact shake
            float impactShake = Mth.sin(impactProgress * 25.0f) * (1.0f - impactProgress) * 0.1f;
            boss.rightArm.xRot = 1.5f + impactShake;
            boss.leftArm.xRot = 1.5f + impactShake;

            // Body shakes slightly with impact
            boss.body.xRot = 0.4f + impactShake * 0.5f;
            boss.head.xRot = -0.3f + impactShake * 0.3f;
        }
        else {
            // Phase 5: Recovery to neutral pose
            float recoveryProgress = (normalizedTime - groundImpactPhase) / (recoveryPhase - groundImpactPhase);

            // Arms return to neutral
            float armRotation = Mth.lerp(recoveryProgress, 1.5f, 0f);
            boss.rightArm.xRot = armRotation;
            boss.leftArm.xRot = armRotation;

            // Body returns to neutral
            boss.body.xRot = Mth.lerp(recoveryProgress, 0.4f, 0f);
            boss.head.xRot = Mth.lerp(recoveryProgress, -0.3f, 0f);
        }
    }

    // Animation duration in seconds
    @Override
    public float getAnimationDuration() {
        return 1.5f*2;
    }
}
