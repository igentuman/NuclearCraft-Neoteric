package igentuman.nc.client.model.animation;

import igentuman.nc.client.model.ModelWastelandBoss;
import net.minecraft.util.Mth;

public class AttackAnimation implements BossAnimation {
    public static AttackAnimation instance = new AttackAnimation();

    // Animation control parameters
    private final float clappingSpeed = 3.0f; // Controls the speed of the clap
    private final float maxArmRotation = 0.8f; // Maximum arm rotation (in radians)

    @Override
    public void playAnimation(ModelWastelandBoss<?> boss, float deltaTime) {
        // Calculate animation progress (0 to 1)
        float progress = deltaTime / getAnimationDuration();

        // Calculate arm movement - create a sinusoidal motion
        // Use Mth.sin for smooth movement, multiply by clappingSpeed to control frequency
        float armPosition = Mth.sin(progress * clappingSpeed * (float)Math.PI) * maxArmRotation;


        // Left arm movement - rotate inward on Z axis and slightly forward on X axis
        boss.leftArm.setRotation(
            -0.2f + progress * 0.4f, // Slight forward movement (X axis)
            0.0f,                    // No rotation around Y axis
            -armPosition             // Inward movement (negative for left arm)
        );

        // Right arm movement - rotate inward on Z axis and slightly forward on X axis
        boss.rightArm.setRotation(
            -0.2f + progress * 0.4f, // Slight forward movement (X axis)
            0.0f,                    // No rotation around Y axis
            armPosition              // Inward movement (positive for right arm)
        );
    }

    @Override
    public float getAnimationDuration() {
        return 2f; // Half second for a quick attack
    }
}
