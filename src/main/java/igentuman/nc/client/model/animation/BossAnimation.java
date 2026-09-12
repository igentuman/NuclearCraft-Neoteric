package igentuman.nc.client.model.animation;

import igentuman.nc.client.model.ModelWastelandBoss;

public interface BossAnimation {
    void apply(ModelWastelandBoss model, float time);

    float durationTicks();

    default float progress(float time) {
        return Math.clamp(time / durationTicks(), 0.0F, 1.0F);
    }
}
