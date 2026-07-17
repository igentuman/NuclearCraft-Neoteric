package igentuman.nc.client.sound;

import igentuman.nc.entity.anomaly.AnomalyEntity;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

import java.lang.ref.WeakReference;

/** Looping ambient hum that follows an anomaly entity and stops when it is removed. */
public class AnomalyAmbientSound extends AbstractTickableSoundInstance {

    private final WeakReference<AnomalyEntity> ref;

    public AnomalyAmbientSound(AnomalyEntity entity, SoundEvent sound, float volume) {
        super(sound, SoundSource.HOSTILE, entity.level().getRandom());
        this.ref = new WeakReference<>(entity);
        this.looping = true;
        this.delay = 0;
        this.volume = volume;
        this.x = (float) entity.getX();
        this.y = (float) entity.getY();
        this.z = (float) entity.getZ();
    }

    @Override
    public void tick() {
        AnomalyEntity entity = ref.get();
        if (entity == null || entity.isRemoved() || !entity.isAlive()) {
            stop();
            return;
        }
        this.x = (float) entity.getX();
        this.y = (float) entity.getY();
        this.z = (float) entity.getZ();
    }

    @Override
    public boolean canStartSilent() {
        return true;
    }
}
