package igentuman.nc.client.model.animation;

import igentuman.nc.client.model.ModelWastelandBoss;
import net.minecraft.util.Mth;

public final class SlamAnimation implements BossAnimation {

    public static final SlamAnimation INSTANCE = new SlamAnimation();

    private SlamAnimation() {
    }

    @Override
    public void apply(ModelWastelandBoss model, float time) {
        float progress = progress(time);
        if (progress < 0.4F) {
            float windup = progress / 0.4F;
            model.leftArm.xRot = Mth.lerp(windup, 0.0F, -2.0F);
            model.rightArm.xRot = model.leftArm.xRot;
            model.body.xRot = Mth.lerp(windup, 0.0F, -0.2F);
            model.head.xRot = Mth.lerp(windup, 0.0F, 0.3F);
            return;
        }
        if (progress < 0.55F) {
            float slam = (progress - 0.4F) / 0.15F;
            model.leftArm.xRot = Mth.lerp(slam, -2.0F, -0.8F);
            model.rightArm.xRot = model.leftArm.xRot;
            model.leftArm.y += 8.5F * slam;
            model.rightArm.y += 8.5F * slam;
            model.leftArm.z -= 8.5F * slam;
            model.rightArm.z -= 8.5F * slam;
            model.body.xRot = Mth.lerp(slam, -0.2F, 1.7F);
            model.head.xRot = Mth.lerp(slam, 0.3F, 1.7F);
            model.head.y += 12.5F * slam;
            model.head.z -= 10.5F * slam;
            return;
        }
        float recovery = (progress - 0.55F) / 0.45F;
        float shake = Mth.sin(recovery * 25.0F) * (1.0F - recovery) * 0.08F;
        model.leftArm.xRot = Mth.lerp(recovery, -0.8F, 0.0F) + shake;
        model.rightArm.xRot = model.leftArm.xRot;
        model.leftArm.y += Mth.lerp(recovery, 8.5F, 0.0F);
        model.rightArm.y += Mth.lerp(recovery, 8.5F, 0.0F);
        model.leftArm.z -= Mth.lerp(recovery, 8.5F, 0.0F);
        model.rightArm.z -= Mth.lerp(recovery, 8.5F, 0.0F);
        model.body.xRot = Mth.lerp(recovery, 1.7F, 0.0F) + shake;
        model.head.xRot = Mth.lerp(recovery, 1.7F, 0.0F);
        model.head.y += Mth.lerp(recovery, 12.5F, 0.0F);
        model.head.z -= Mth.lerp(recovery, 10.5F, 0.0F);
    }

    @Override
    public float durationTicks() {
        return 20.0F;
    }
}
