package igentuman.nc.client.model.animation;

import igentuman.nc.client.model.ModelWastelandBoss;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

public class SummonAnimation implements BossAnimation {
    public static SummonAnimation instance = new SummonAnimation();

    @Override
    public void playAnimation(ModelWastelandBoss<?> boss, float deltaTime) {
        float normalizedTime = deltaTime / getAnimationDuration();

        ModelPart leftArm = boss.leftArm;
        ModelPart rightArm = boss.rightArm;

        leftArm.resetPose();
        rightArm.resetPose();

        if (normalizedTime <= 0.5f) {
            float raisingProgress = normalizedTime * 2; // Scale to 0-1 range for first half
            float armAngle = Mth.sin(raisingProgress * (float)Math.PI/2) * -1.9f; // Negative for upward motion
            leftArm.xRot = armAngle;
            rightArm.xRot = armAngle;
            leftArm.zRot = -0.4f * raisingProgress;
            rightArm.zRot = 0.4f * raisingProgress;
        } else {
            float pullingProgress = (normalizedTime - 0.5f) * 2; // Scale to 0-1 range for second half
            float downAngle = Mth.sin(pullingProgress * (float)Math.PI/2) * 0.7f; // Positive for downward force
            leftArm.xRot = -1.9f * (1 - pullingProgress) + downAngle;
            rightArm.xRot = -1.9f * (1 - pullingProgress) + downAngle;
            leftArm.zRot = -0.4f * (1 - pullingProgress);
            rightArm.zRot = 0.4f * (1 - pullingProgress);
        }
    }

    @Override
    public float getAnimationDuration() {
        return 1.5f*40;
    }
}
