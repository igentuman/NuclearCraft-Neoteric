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
        float throwPhase = 0.95f;
        boss.head.resetPose();
        // Body leaning forward and back
        if (animationProgress < leanForwardPhase) {
            float leanProgress = animationProgress / leanForwardPhase;
            float bodyLean = Mth.lerp(leanProgress, 0f, 0.6f);
            boss.body.xRot = bodyLean;
            boss.head.xRot = bodyLean;
            boss.head.z = Mth.lerp(leanProgress, 0f, -12f);
            boss.head.y = Mth.lerp(leanProgress, -8f, -5f);
            boss.rightArm.xRot = Mth.lerp(leanProgress, 0f, 0.7f);
            boss.leftArm.xRot = Mth.lerp(leanProgress, 0f, 0.7f);
            boss.rightLeg.xRot = Mth.lerp(leanProgress, 0f, -0.2f);
            boss.leftLeg.xRot = Mth.lerp(leanProgress, 0f, -0.2f);
            boss.leftArm.z = Mth.lerp(leanProgress, 0f, -3);
            boss.rightArm.z = Mth.lerp(leanProgress, 0f, -3);
            boss.head.yRot = Mth.lerp(leanProgress, 0f, 0.1f);
        } else if (animationProgress < pickUpPhase) {
            // Phase 2: Picking up block from ground
            float pickProgress = (animationProgress - leanForwardPhase) / (pickUpPhase - leanForwardPhase);
            boss.body.xRot = 0.6f;
            boss.head.xRot = 0.6f;
            boss.head.yRot = Mth.lerp(pickProgress, 0.1f, 0f);
            boss.head.z = -12f;
            boss.head.y = -5f;
            boss.rightArm.xRot = Mth.lerp(pickProgress, 0.7f, 1.2f);
            boss.leftArm.xRot = Mth.lerp(pickProgress, 0.7f, 1.2f);
            boss.rightArm.zRot = Mth.lerp(pickProgress, 0.1745f, 0f);
            boss.leftArm.zRot = Mth.lerp(pickProgress, -0.1745f, 0f);
            boss.leftArm.z = -3f;
            boss.rightArm.z = -3f;
            boss.rightLeg.xRot = Mth.lerp(pickProgress, -0.2f, -0.3f);
            boss.leftLeg.xRot = Mth.lerp(pickProgress, -0.2f, -0.3f);
        } else if (animationProgress < standUpPhase) {
            // Phase 3: Standing back up with block
            float standProgress = (animationProgress - pickUpPhase) / (standUpPhase - pickUpPhase);
            boss.body.xRot = Mth.lerp(standProgress, 0.6f, 0.1f);
            boss.head.xRot = Mth.lerp(standProgress, 0.6f, 0.1f);
            boss.head.z = Mth.lerp(standProgress, -12f, 0.0f);
            boss.head.y = Mth.lerp(standProgress, -5f, -8f);
            boss.rightArm.xRot = Mth.lerp(standProgress, 1.2f, 0f);
            boss.leftArm.xRot = Mth.lerp(standProgress, 1.2f, 0f);
            boss.rightArm.zRot = 0f;
            boss.leftArm.zRot = 0f;
            boss.leftArm.z = Mth.lerp(standProgress, -3f, 0);
            boss.rightArm.z = Mth.lerp(standProgress, -3f, 0);
            boss.rightLeg.xRot = Mth.lerp(standProgress, -0.3f, -0.1745f);
            boss.leftLeg.xRot = Mth.lerp(standProgress, -0.3f, -0.1745f);
        } else {
            // Phase 4: Throwing the block
            float throwProgress = (animationProgress - standUpPhase) / (throwPhase - standUpPhase);
            float windupProgress = throwProgress / 0.3f;
            boss.body.xRot = Mth.lerp(windupProgress, 0.1f, -0.2f);
            float twistAmount = Mth.sin(windupProgress * (float)Math.PI) * 0.15f;
            boss.body.yRot = twistAmount;
            boss.rightArm.xRot = Mth.lerp(windupProgress, 0f, -1.8f);
            boss.leftArm.xRot = Mth.lerp(windupProgress, 0f, -1.8f);
            float bodyCompensation = Mth.lerp(windupProgress, 0.1f, -0.2f) * -0.3f;
            float targetFocus = Mth.lerp(windupProgress, 0.1f, 0.3f);
            boss.head.xRot = bodyCompensation + targetFocus;
            boss.head.yRot = Mth.lerp(windupProgress, 0f, -twistAmount * 0.5f);
        }
    }

    //in seconds
    @Override
    public float getAnimationDuration() {
        return 1.5f*15;
    }
}
