package igentuman.nc.client.sound;

import igentuman.nc.entity.anomaly.AnomalyEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvent;

public final class AnomalySoundController {

    private AnomalySoundController() {
    }

    public static boolean isActive(Object handle) {
        return handle instanceof AnomalyAmbientSound sound
                && Minecraft.getInstance().getSoundManager().isActive(sound);
    }

    public static Object play(AnomalyEntity entity, SoundEvent event, float volume) {
        AnomalyAmbientSound sound = new AnomalyAmbientSound(entity, event, volume);
        Minecraft.getInstance().getSoundManager().play(sound);
        return sound;
    }

    public static void stop(Object handle) {
        if (handle instanceof AnomalyAmbientSound sound) {
            Minecraft.getInstance().getSoundManager().stop(sound);
        }
    }
}
