package igentuman.nc.client.sound;

import net.minecraft.client.Minecraft;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;

public final class BlockEntitySounds {

    private BlockEntitySounds() {
    }

    public static Object play(Object current, SoundEvent sound, float volume, BlockPos pos) {
        SoundManager mgr = Minecraft.getInstance().getSoundManager();
        TileSoundInstance cur = (TileSoundInstance) current;
        if (cur != null && (!cur.getLocation().equals(sound.getLocation()) || !mgr.isActive(cur))) {
            mgr.stop(cur);
            cur = null;
        }
        if (cur == null) {
            cur = new TileSoundInstance(sound, volume, pos);
            mgr.play(cur);
        }
        return cur;
    }

    public static void stop(Object current) {
        if (current == null) return;
        Minecraft.getInstance().getSoundManager().stop((TileSoundInstance) current);
    }
}
