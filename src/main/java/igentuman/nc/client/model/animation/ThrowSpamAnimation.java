package igentuman.nc.client.model.animation;

import igentuman.nc.client.model.ModelWastelandBoss;
import igentuman.nc.entity.EntityWastelandBoss;
import net.minecraft.util.Mth;

public final class ThrowSpamAnimation implements BossAnimation {

    public static final ThrowSpamAnimation INSTANCE = new ThrowSpamAnimation();

    private ThrowSpamAnimation() {
    }

    @Override
    public void apply(ModelWastelandBoss model, float time) {
        if (time < 35.0F) {
            float windup = time / 35.0F;
            model.body.xRot = Mth.lerp(windup, 0.0F, -0.25F);
            model.leftArm.xRot = Mth.lerp(windup, 0.0F, -2.0F);
            model.rightArm.xRot = model.leftArm.xRot;
            model.leftArm.zRot = -0.35F * windup;
            model.rightArm.zRot = 0.35F * windup;
            return;
        }
        float volleyTime = time - 35.0F;
        float throwCycle = (volleyTime % EntityWastelandBoss.THROW_SPAM_INTERVAL)
                / EntityWastelandBoss.THROW_SPAM_INTERVAL;
        float throwMotion = Mth.sin(throwCycle * Mth.PI);
        model.body.yRot = Mth.sin(volleyTime * 0.35F) * 0.12F;
        model.leftArm.xRot = -1.2F - throwMotion * 0.8F;
        model.rightArm.xRot = -1.2F - throwMotion * 0.8F;
        model.leftArm.zRot = -0.2F - throwMotion * 0.2F;
        model.rightArm.zRot = 0.2F + throwMotion * 0.2F;
    }

    @Override
    public float durationTicks() {
        return 240.0F;
    }
}
