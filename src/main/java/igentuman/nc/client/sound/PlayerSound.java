package igentuman.nc.client.sound;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import org.antlr.v4.runtime.misc.NotNull;;
import javax.annotation.Nullable;

import java.lang.ref.WeakReference;

public abstract class PlayerSound extends AbstractTickableSoundInstance {

    @NotNull
    private final WeakReference<Player> playerReference;
    private float lastX;
    private float lastY;
    private float lastZ;

    private float fadeUpStep = 0.1f;
    private float fadeDownStep = 0.1f;

    public PlayerSound(@NotNull Player player, @NotNull SoundEvent sound) {
        super(sound, SoundSource.PLAYERS);
        this.playerReference = new WeakReference<>(player);
        this.lastX = (float) player.getX();
        this.lastY = (float) player.getY();
        this.lastZ = (float) player.getZ();
        this.looping = true;
        this.delay = 0;

        this.volume = 0.1F;
    }

    @Nullable
    private Player getPlayer() {
        return playerReference.get();
    }

    protected void setFade(float fadeUpStep, float fadeDownStep) {
        this.fadeUpStep = fadeUpStep;
        this.fadeDownStep = fadeDownStep;
    }

    @Override
    public double getX() {
        Player player = getPlayer();
        if (player != null) {
            this.lastX = (float) player.getX();
        }
        return this.lastX;
    }

    @Override
    public double getY() {
        Player player = getPlayer();
        if (player != null) {
            this.lastY = (float) player.getY();
        }
        return this.lastY;
    }

    @Override
    public double getZ() {
        Player player = getPlayer();
        if (player != null) {
            this.lastZ = (float) player.getZ();
        }
        return this.lastZ;
    }

    @Override
    public void tick() {
        Player player = getPlayer();
        if (player == null || !player.isAlive()) {
            stop();
            volume = 0.0F;
            return;
        }

        if (shouldPlaySound(player)) {
            if (volume < 1.0F) {
                volume = Math.min(1.0F, volume + fadeUpStep);
            }
        } else if (volume > 0.0F) {
            volume = Math.max(0.0F, volume - fadeDownStep);
        }
    }

    public abstract boolean shouldPlaySound(@NotNull Player player);

    @Override
    public float getVolume() {
        return super.getVolume() * 1f;
    }//todo read config

    @Override
    public boolean canStartSilent() {
        return true;
    }

    @Override
    public boolean canPlaySound() {
        Player player = getPlayer();
        if (player == null) {
            return super.canPlaySound();
        }
        return !player.isSilent();
    }
}