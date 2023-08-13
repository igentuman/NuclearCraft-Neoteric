package igentuman.nc.client.sound;

import igentuman.nc.radiation.client.ClientRadiationData;
import igentuman.nc.radiation.data.PlayerRadiation;
import igentuman.nc.radiation.data.PlayerRadiationProvider;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import static igentuman.nc.setup.registration.NCSounds.GEIGER_SOUNDS;

public class GeigerSound extends PlayerSound {

    public int radiationLevel = 0;
    public static GeigerSound create(@NotNull Player player) {
        ClientRadiationData.setCurrentChunk(player.chunkPosition().x, player.chunkPosition().z);
        int level = Math.min(5, (int)((float)ClientRadiationData.getCurrentWorldRadiation()/400000));
        if(level == 0) return null;
        return new GeigerSound(player, level);
    }

    private GeigerSound(@NotNull Player player, int level) {
        super(player, GEIGER_SOUNDS.get(level-1).get());
        radiationLevel = level;
        setFade(1, 1);
    }

    @Override
    public boolean shouldPlaySound(@NotNull Player player) {
        return radiationLevel > 0;
    }

    @Override
    public float getVolume() {
        return super.getVolume() * 0.5F;
    }
}