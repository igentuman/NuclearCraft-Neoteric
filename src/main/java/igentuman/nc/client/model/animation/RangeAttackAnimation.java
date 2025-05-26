package igentuman.nc.client.model.animation;

import igentuman.nc.client.model.ModelWastelandBoss;
import net.minecraft.util.Mth;

public class RangeAttackAnimation implements BossAnimation {
    public static RangeAttackAnimation instance = new RangeAttackAnimation();

    @Override
    public void playAnimation(ModelWastelandBoss<?> boss, float deltaTime) {
        float animationProgress = deltaTime / getAnimationDuration();

        // Phases of animation (in normalized time)
        float leanForwardPhase = 0.3f;
        float pickUpPhase = 0.5f;
        float standUpPhase = 0.7f;
        float throwPhase = 1.0f;

        // Body leaning forward and back
        if (animationProgress < leanForwardPhase) {
            float leanProgress = animationProgress / leanForwardPhase;
            float bodyLean = Mth.lerp(leanProgress, 0f, 0.6f); // 0 to 0.6 radians forward lean

            boss.body.xRot = bodyLean;
            // Head compensates for forward lean - looks more natural
            boss.head.xRot = bodyLean;
            boss.head.z = Mth.lerp(leanProgress, 0f, -6.6f);
            // Arms move forward as boss leans
            boss.rightArm.xRot = Mth.lerp(leanProgress, 0f, 0.7f);
            boss.leftArm.xRot = Mth.lerp(leanProgress, 0f, 0.7f);

            // Slight squat in legs
            boss.rightLeg.xRot = Mth.lerp(leanProgress, 0f, -0.2f);
            boss.leftLeg.xRot = Mth.lerp(leanProgress, 0f, -0.2f);

            // Add slight head looking down at target spot
            boss.head.yRot = Mth.lerp(leanProgress, 0f, 0.1f);
        }
        else if (animationProgress < pickUpPhase) {
            // Phase 2: Picking up block from ground
            float pickProgress = (animationProgress - leanForwardPhase) / (pickUpPhase - leanForwardPhase);

            // Body stays leaned
            boss.body.xRot = 0.6f;
            // Head looks down at hands/object
            boss.head.xRot = 0.6f;
            boss.head.yRot = Mth.lerp(pickProgress, 0.1f, 0f);
            boss.head.z = -6.6f;

            // Arms reach down to ground
            boss.rightArm.xRot = Mth.lerp(pickProgress, 0.7f, 1.2f);
            boss.leftArm.xRot = Mth.lerp(pickProgress, 0.7f, 1.2f);

            // Hands come together as if grabbing something
            boss.rightArm.zRot = Mth.lerp(pickProgress, 0.1745f, 0f);
            boss.leftArm.zRot = Mth.lerp(pickProgress, -0.1745f, 0f);

            // Deeper squat
            boss.rightLeg.xRot = Mth.lerp(pickProgress, -0.2f, -0.3f);
            boss.leftLeg.xRot = Mth.lerp(pickProgress, -0.2f, -0.3f);
        }
        else if (animationProgress < standUpPhase) {
            // Phase 3: Standing back up with block
            float standProgress = (animationProgress - pickUpPhase) / (standUpPhase - pickUpPhase);

            // Body returns to upright
            boss.body.xRot = Mth.lerp(standProgress, 0.6f, 0.1f);

            // Head follows the body's movement while looking at the block
            boss.head.xRot = Mth.lerp(standProgress, 0.6f, 0.1f);
            boss.head.z = Mth.lerp(standProgress, -6.6f, 0.0f);
            // Arms raise up with the block
            boss.rightArm.xRot = Mth.lerp(standProgress, 1.2f, 0f);
            boss.leftArm.xRot = Mth.lerp(standProgress, 1.2f, 0f);

            // Arms positioned to hold block
            boss.rightArm.zRot = 0f;
            boss.leftArm.zRot = 0f;

            // Return to normal standing posture
            boss.rightLeg.xRot = Mth.lerp(standProgress, -0.3f, -0.1745f);
            boss.leftLeg.xRot = Mth.lerp(standProgress, -0.3f, -0.1745f);
        }
        else {
            // Phase 4: Throwing the block
            float throwProgress = (animationProgress - standUpPhase) / (throwPhase - standUpPhase);

            // Wind up for throw
            if (throwProgress < 0.3f) {
                float windupProgress = throwProgress / 0.3f;
                boss.body.xRot = Mth.lerp(windupProgress, 0.1f, -0.2f);

                // Only small twisting motion for power, but always returning to face forward
                float twistAmount = Mth.sin(windupProgress * (float)Math.PI) * 0.15f;
                boss.body.yRot = twistAmount;

                // Arms pull back
                boss.rightArm.xRot = Mth.lerp(windupProgress, 0f, -0.8f);
                boss.leftArm.xRot = Mth.lerp(windupProgress, 0f, -0.8f);

                // Head follows body rotation with slight target focus
                float bodyCompensation = Mth.lerp(windupProgress, 0.1f, -0.2f) * -0.3f;
                float targetFocus = Mth.lerp(windupProgress, 0.1f, 0.3f);
                boss.head.xRot = bodyCompensation + targetFocus;
                boss.head.yRot = Mth.lerp(windupProgress, 0f, -twistAmount * 0.5f); // Counteract body twist to keep looking at target
            }
            else {
                float releaseProgress = (throwProgress - 0.3f) / 0.7f;

                // Forward throw motion
                boss.body.xRot = Mth.lerp(releaseProgress, -0.2f, 0f);

                // Return to facing directly at target during throw
                boss.body.yRot = Mth.lerp(releaseProgress, 0.15f, 0f);

                // Arms extend forward rapidly
                boss.rightArm.xRot = Mth.lerp(releaseProgress, -0.8f, 1.2f);
                boss.leftArm.xRot = Mth.lerp(releaseProgress, -0.8f, 1.2f);

                // Small twist for power
                boss.rightArm.zRot = Mth.lerp(releaseProgress, 0f, 0.2f);
                boss.leftArm.zRot = Mth.lerp(releaseProgress, 0f, -0.2f);

                // Head follows throw and tracks the projectile
                float bodyCompensation = Mth.lerp(releaseProgress, -0.2f, 0f) * -0.2f;
                float throwTracking = Mth.lerp(releaseProgress, 0.3f, -0.15f); // Starts looking up, then follows projectile down
                boss.head.xRot = bodyCompensation + throwTracking;
                boss.head.yRot = Mth.lerp(releaseProgress, -0.075f, 0f); // Align head with body during throw
            }
        }
    }

    //in seconds
    @Override
    public float getAnimationDuration() {
        return 2.5f*2;
    }
}
