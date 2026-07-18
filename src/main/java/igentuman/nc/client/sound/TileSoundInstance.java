package igentuman.nc.client.sound;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;

/** Looping, block-positioned sound whose volume falls off with distance to the client player. */
public class TileSoundInstance extends AbstractTickableSoundInstance {

    private final float baseVolume;

    public TileSoundInstance(SoundEvent sound, float volume, BlockPos pos) {
        super(sound, SoundSource.BLOCKS, RandomSource.create());
        this.baseVolume = volume;
        this.x = pos.getX() + 0.5;
        this.y = pos.getY() + 0.5;
        this.z = pos.getZ() + 0.5;
        this.looping = true;
        this.delay = 0;
        this.volume = volume;
    }

    @Override
    public void tick() {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;
        double distSq = player.position().distanceToSqr(x, y, z);
        volume = baseVolume * Math.max(0f, Math.min(1f, 1f - (float) distSq / 400f));
    }

    public void done() {
        stop();
    }
}
