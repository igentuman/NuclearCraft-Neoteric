package igentuman.nc.client.model.animation;

import igentuman.nc.client.model.ModelWastelandBoss;
import net.minecraft.util.Mth;

public final class RadiationBurstAnimation implements BossAnimation {

    public static final RadiationBurstAnimation INSTANCE = new RadiationBurstAnimation();

    private RadiationBurstAnimation() {
    }

    @Override
    public void apply(ModelWastelandBoss model, float time) {
        float progress = progress(time);
        float raised = progress < 0.3F
                ? progress / 0.3F
                : progress > 0.7F ? (1.0F - progress) / 0.3F : 1.0F;
        float wave = progress >= 0.3F && progress <= 0.7F
                ? Mth.sin((progress - 0.3F) * Mth.PI * 7.5F) * 0.25F : 0.0F;
        model.leftArm.xRot = -2.4F * raised;
        model.rightArm.xRot = -2.4F * raised;
        model.leftArm.zRot = -0.3F * raised - wave;
        model.rightArm.zRot = 0.3F * raised + wave;
        model.body.y -= Mth.sin(progress * Mth.PI) * 1.5F;
    }

    @Override
    public float durationTicks() {
        return 30.0F;
    }
}
