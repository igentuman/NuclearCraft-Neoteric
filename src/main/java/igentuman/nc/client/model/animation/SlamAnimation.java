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
        float slamPhase = 0.5f;             // 20% quick slam down
        float groundImpactPhase = 0.9f;     // 20% impact and hold
        float recoveryPhase = 1.0f;         // 20% return to neutral

        // Body adjustments during different phases
        if (normalizedTime < raiseArmsPhase) {
            // Phase 1: Raising arms up
            float raisingProgress = normalizedTime / raiseArmsPhase;

            float armRotation = Mth.lerp(raisingProgress, 0f, -2.0f);
            boss.rightArm.xRot = armRotation;
            boss.leftArm.xRot = armRotation;
            boss.body.xRot = Mth.lerp(raisingProgress, 0f, -0.2f);
            boss.head.xRot = Mth.lerp(raisingProgress, 0f, 0.3f);
        }
        else if (normalizedTime < windupPhase) {
            // Phase 2: Brief pause at top with slight anticipatory motion
            float pauseProgress = (normalizedTime - raiseArmsPhase) / (windupPhase - raiseArmsPhase);
            float armPauseMovement = Mth.sin(pauseProgress * 6.28f) * 0.1f;
            boss.rightArm.xRot = -2.0f + armPauseMovement;
            boss.leftArm.xRot = -2.0f + armPauseMovement;

            float bodyAnticipation = Mth.sin(pauseProgress * 3.14f) * 0.05f;
            boss.body.xRot = -0.2f - bodyAnticipation;
            boss.head.xRot = 0.3f + bodyAnticipation;
        }
        else if (normalizedTime < slamPhase) {
            // Phase 3: Quick slam down
            float slamProgress = (normalizedTime - windupPhase) / (slamPhase - windupPhase);
            float armRotation = Mth.lerp(slamProgress, -2.0f, -1.5f); // From up to past neutral (overextended)
            boss.rightArm.xRot = armRotation;
            boss.leftArm.xRot = armRotation;
            boss.rightArm.y = Mth.lerp(slamProgress, 0.0f, 6.5f); // Move down
            boss.rightArm.z = Mth.lerp(slamProgress, 0.0f, -7.0f); // Move forward
            boss.leftArm.y = Mth.lerp(slamProgress, 0.0f, 6.5f);
            boss.leftArm.z = Mth.lerp(slamProgress, 0.0f, -7.0f);
            boss.body.xRot = Mth.lerp(slamProgress, -0.2f, 1.7f);
            boss.head.xRot = Mth.lerp(slamProgress, 0.3f, 1.7f); // Head looks down
            boss.head.z = Mth.lerp(slamProgress, 0f, -4.5f); // Head moves forward slightly
            boss.head.y = Mth.lerp(slamProgress, 0f, 6.0f); // Head shifts down
        }
        else if (normalizedTime < groundImpactPhase) {
            // Phase 4: Ground impact and hold
            float impactProgress = (normalizedTime - slamPhase) / (groundImpactPhase - slamPhase);

            // Arms stay extended with impact shake
            float impactShake = Mth.sin(impactProgress * 25.0f) * (1.0f - impactProgress) * 0.1f;
            boss.rightArm.xRot = -0.8f + impactShake;
            boss.rightArm.y = 8.5f; // Arms stay low to the ground
            boss.rightArm.z = -8.5f;
            boss.leftArm.xRot = -0.8f + impactShake;
            boss.leftArm.y = 8.5f;
            boss.leftArm.z = -8.5f;

            // Body shakes slightly with impact
            boss.body.xRot = 1.7f + impactShake * 0.5f;
            boss.head.xRot = 1.7f + impactShake * 0.3f;
            boss.head.z = -10.5f; // Head stays forward but shakes slightly
            boss.head.y = 12.5f; // Head stays low to the ground
        }
        else {
            // Phase 5: Recovery to neutral pose
            float recoveryProgress = (normalizedTime - groundImpactPhase) / (recoveryPhase - groundImpactPhase);

            // Arms return to neutral with proper positioning
            float armRotation = Mth.lerp(recoveryProgress, -0.8f, 0f); // Start from impact position
            boss.rightArm.xRot = armRotation;
            boss.leftArm.xRot = armRotation;

            // Gradually reset arm positions
            boss.rightArm.y = Mth.lerp(recoveryProgress, 8.5f, 2.0f); // Return to normal height
            boss.rightArm.z = Mth.lerp(recoveryProgress, -8.5f, 0f); // Return to normal depth
            boss.leftArm.y = Mth.lerp(recoveryProgress, 8.5f, 2.0f);
            boss.leftArm.z = Mth.lerp(recoveryProgress, -8.5f, 0f);

            // Body returns to neutral
            boss.body.xRot = Mth.lerp(recoveryProgress, 1.7f, 0f);

            // Head returns from impact position
            boss.head.xRot = Mth.lerp(recoveryProgress, 1.7f, 0f);
            boss.head.z = Mth.lerp(recoveryProgress, -11.5f, 0f);
            boss.head.y = Mth.lerp(recoveryProgress, 13.5f, 0f);
        }
    }

    // Animation duration in seconds
    @Override
    public float getAnimationDuration() {
        return 1.5f*7;
    }
}
