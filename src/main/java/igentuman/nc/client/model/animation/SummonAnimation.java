package igentuman.nc.client.model.animation;

import igentuman.nc.client.model.ModelWastelandBoss;
import net.minecraft.util.Mth;

public final class SummonAnimation implements BossAnimation {

    public static final SummonAnimation INSTANCE = new SummonAnimation();

    private SummonAnimation() {
    }

    @Override
    public void apply(ModelWastelandBoss model, float time) {
        float progress = progress(time);
        float raised = Mth.sin(progress * Mth.PI);
        model.leftArm.xRot = -1.9F * raised;
        model.rightArm.xRot = -1.9F * raised;
        model.leftArm.zRot = -0.4F * raised;
        model.rightArm.zRot = 0.4F * raised;
        model.body.xRot += Mth.sin(progress * Mth.PI * 4.0F) * 0.08F * raised;
    }

    @Override
    public float durationTicks() {
        return 40.0F;
    }
}
