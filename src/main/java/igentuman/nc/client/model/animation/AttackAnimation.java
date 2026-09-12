package igentuman.nc.client.model.animation;

import igentuman.nc.client.model.ModelWastelandBoss;
import net.minecraft.util.Mth;

public final class AttackAnimation implements BossAnimation {

    public static final AttackAnimation INSTANCE = new AttackAnimation();

    private AttackAnimation() {
    }

    @Override
    public void apply(ModelWastelandBoss model, float time) {
        float progress = progress(time);
        float clap = Mth.sin(progress * Mth.PI) * 0.9F;
        model.leftArm.xRot = -0.35F + progress * 0.4F;
        model.rightArm.xRot = -0.35F + progress * 0.4F;
        model.leftArm.zRot = -clap;
        model.rightArm.zRot = clap;
    }

    @Override
    public float durationTicks() {
        return 10.0F;
    }
}
