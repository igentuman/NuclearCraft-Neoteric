package igentuman.nc.client.model.animation;

import igentuman.nc.client.model.ModelWastelandBoss;
import net.minecraft.util.Mth;

public final class RangedAttackAnimation implements BossAnimation {

    public static final RangedAttackAnimation INSTANCE = new RangedAttackAnimation();

    private RangedAttackAnimation() {
    }

    @Override
    public void apply(ModelWastelandBoss model, float time) {
        float progress = progress(time);
        if (progress < 0.25F) {
            float crouch = progress / 0.25F;
            model.body.xRot = Mth.lerp(crouch, 0.0F, 0.6F);
            model.head.xRot = model.body.xRot;
            model.head.y += 5.0F * crouch;
            model.head.z -= 10.0F * crouch;
            model.leftArm.xRot = 0.9F * crouch;
            model.rightArm.xRot = model.leftArm.xRot;
            return;
        }
        if (progress < 0.55F) {
            float lift = (progress - 0.25F) / 0.3F;
            model.body.xRot = Mth.lerp(lift, 0.6F, -0.15F);
            model.head.xRot = Mth.lerp(lift, 0.6F, 0.1F);
            model.head.y += Mth.lerp(lift, 5.0F, 0.0F);
            model.head.z -= Mth.lerp(lift, 10.0F, 0.0F);
            model.leftArm.xRot = Mth.lerp(lift, 0.9F, -1.8F);
            model.rightArm.xRot = model.leftArm.xRot;
            return;
        }
        float recovery = (progress - 0.55F) / 0.45F;
        model.body.xRot = Mth.lerp(recovery, -0.15F, 0.0F);
        model.leftArm.xRot = Mth.lerp(recovery, -1.8F, 0.0F);
        model.rightArm.xRot = model.leftArm.xRot;
    }

    @Override
    public float durationTicks() {
        return 30.0F;
    }
}
