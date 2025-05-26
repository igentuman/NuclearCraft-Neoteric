package igentuman.nc.client.model.animation;

import igentuman.nc.client.model.ModelWastelandBoss;
import net.minecraft.util.Mth;

public class RadiationBurstAnimation implements BossAnimation {
    public static RadiationBurstAnimation instance = new RadiationBurstAnimation();

    @Override
    public void playAnimation(ModelWastelandBoss<?> boss, float deltaTime) {
        float animationProgress = deltaTime / getAnimationDuration();

        // Animation phases
        float raisingPhase = 0.3f; // First 30% of animation - raising hands
        float wavingPhase = 0.7f;  // Next 40% - waving hands
        float loweringPhase = 1.0f; // Last 30% - lowering hands

        // Limit animation progress to 1.0
        animationProgress = Math.min(animationProgress, 1.0f);

        if (animationProgress <= raisingPhase) {
            // Phase 1: Raising hands up
            float phaseProgress = animationProgress / raisingPhase;

            // Move arms upward by rotating the shoulders
            float armRotation = phaseProgress * (float) Math.toRadians(-140); // Negative to rotate upward
            boss.rightArm.xRot = armRotation;
            boss.leftArm.xRot = armRotation;

            // Slightly rotate arms outward
            boss.rightArm.zRot = (float) Math.toRadians(15 * phaseProgress);
            boss.leftArm.zRot = (float) Math.toRadians(-15 * phaseProgress);
        }
        else if (animationProgress <= wavingPhase) {
            // Phase 2: Waving hands in the air
            float phaseProgress = (animationProgress - raisingPhase) / (wavingPhase - raisingPhase);

            // Keep arms up
            boss.rightArm.xRot = (float) Math.toRadians(-140);
            boss.leftArm.xRot = (float) Math.toRadians(-140);

            // Wave hands by rotating arms side to side
            float waveRotation = Mth.sin(phaseProgress * Mth.PI * 3) * 0.2f;
            boss.rightArm.zRot = (float) Math.toRadians(15) + waveRotation;
            boss.leftArm.zRot = (float) Math.toRadians(-15) - waveRotation;

            // Add a slight upward bobbing motion
            float bobAmount = Mth.sin(phaseProgress * Mth.PI * 2) * 0.05f;
            boss.rightArm.y -= bobAmount * 5;
            boss.leftArm.y -= bobAmount * 5;
        }
        else {
            // Phase 3: Lowering hands
            float phaseProgress = (animationProgress - wavingPhase) / (loweringPhase - wavingPhase);

            // Gradually lower arms back to normal position
            float armRotation = (float) Math.toRadians(-140) * (1 - phaseProgress);
            boss.rightArm.xRot = armRotation;
            boss.leftArm.xRot = armRotation;

            // Return arm rotation to original state
            boss.rightArm.zRot = (float) Math.toRadians(15 * (1 - phaseProgress));
            boss.leftArm.zRot = (float) Math.toRadians(-15 * (1 - phaseProgress));
        }
    }

    // Animation duration in seconds
    @Override
    public float getAnimationDuration() {
        return 2f*20;
    }
}
