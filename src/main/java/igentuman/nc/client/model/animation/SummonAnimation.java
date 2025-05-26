package igentuman.nc.client.model.animation;

import igentuman.nc.client.model.ModelWastelandBoss;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

public class SummonAnimation implements BossAnimation {
    public static SummonAnimation instance = new SummonAnimation();

    @Override
    public void playAnimation(ModelWastelandBoss<?> boss, float deltaTime) {
        float normalizedTime = deltaTime / getAnimationDuration();

        // Get the arm parts we need to animate
        ModelPart leftArm = boss.leftArm;
        ModelPart rightArm = boss.rightArm;

        // Reset poses to avoid unwanted accumulation
        leftArm.resetPose();
        rightArm.resetPose();

        // First half of animation: raising arms (0 to 0.5)
        // Second half of animation: pulling arms down (0.5 to 1.0)
        if (normalizedTime <= 0.5f) {
            // Raising arms phase - gradual upward motion
            float raisingProgress = normalizedTime * 2; // Scale to 0-1 range for first half

            // Raise arms by rotating at the shoulder joint
            // Use sine interpolation for smooth movement
            float armAngle = Mth.sin(raisingProgress * (float)Math.PI/2) * -1.3f; // Negative for upward motion

            leftArm.xRot = armAngle;
            rightArm.xRot = armAngle;

            // Add slight outward rotation
            leftArm.zRot = -0.2f * raisingProgress;
            rightArm.zRot = 0.2f * raisingProgress;

        } else {
            // Pulling arms down phase
            float pullingProgress = (normalizedTime - 0.5f) * 2; // Scale to 0-1 range for second half

            // Pull down motion - more rapid than raising
            float downAngle = Mth.sin(pullingProgress * (float)Math.PI/2) * 0.7f; // Positive for downward force

            // Start from raised position and move downward
            leftArm.xRot = -1.3f * (1 - pullingProgress) + downAngle;
            rightArm.xRot = -1.3f * (1 - pullingProgress) + downAngle;

            // Reduce outward rotation
            leftArm.zRot = -0.2f * (1 - pullingProgress);
            rightArm.zRot = 0.2f * (1 - pullingProgress);
        }
    }

    @Override
    public float getAnimationDuration() {
        return 1.5f*2;
    }
}
