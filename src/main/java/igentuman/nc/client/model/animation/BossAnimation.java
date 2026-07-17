package igentuman.nc.client.model.animation;

import igentuman.nc.client.model.ModelWastelandBoss;

public interface BossAnimation {
    void playAnimation(ModelWastelandBoss<?> boss, float deltaTime);
    float getAnimationDuration();
}
