package igentuman.nc.client.sound;

import igentuman.nc.entity.anomaly.AnomalyEntity;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

public class AnomalyAmbientSound extends AbstractTickableSoundInstance {

    private final AnomalyEntity entity;
    private final float baseVolume;

    public AnomalyAmbientSound(AnomalyEntity entity, SoundEvent sound, float volume) {
        super(sound, SoundSource.HOSTILE, entity.getRandom());
        this.entity = entity;
        this.baseVolume = volume;
        this.x = entity.getX();
        this.y = entity.getY();
        this.z = entity.getZ();
        this.looping = true;
        this.delay = 0;
        this.volume = volume;
        this.pitch = 1.0F;
        this.relative = false;
    }

    @Override
    public void tick() {
        if (entity.isRemoved()) {
            stop();
            return;
        }
        this.x = entity.getX();
        this.y = entity.getY();
        this.z = entity.getZ();
        this.volume = baseVolume;
    }

    @Override
    public boolean canStartSilent() {
        return true;
    }
}
