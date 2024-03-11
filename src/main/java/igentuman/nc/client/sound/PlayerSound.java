package igentuman.nc.client.sound;

import net.minecraft.client.audio.TickableSound;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.SoundEvent;
import org.antlr.v4.runtime.misc.NotNull;;
import javax.annotation.Nullable;

import java.lang.ref.WeakReference;

import static net.minecraft.util.SoundCategory.PLAYERS;

public abstract class PlayerSound extends TickableSound {

    @NotNull
    private final WeakReference<PlayerEntity> playerReference;
    private float lastX;
    private float lastY;
    private float lastZ;

    private float fadeUpStep = 0.1f;
    private float fadeDownStep = 0.1f;

    public PlayerSound(@NotNull PlayerEntity player, @NotNull SoundEvent sound) {
        super(sound, PLAYERS);
        this.playerReference = new WeakReference<>(player);
        this.lastX = (float) player.getX();
        this.lastY = (float) player.getY();
        this.lastZ = (float) player.getZ();
        this.looping = true;
        this.delay = 0;

        this.volume = 0.1F;
    }

    @Nullable
    private PlayerEntity getPlayer() {
        return playerReference.get();
    }

    protected void setFade(float fadeUpStep, float fadeDownStep) {
        this.fadeUpStep = fadeUpStep;
        this.fadeDownStep = fadeDownStep;
    }

    @Override
    public double getX() {
        PlayerEntity player = getPlayer();
        if (player != null) {
            this.lastX = (float) player.getX();
        }
        return this.lastX;
    }

    @Override
    public double getY() {
        PlayerEntity player = getPlayer();
        if (player != null) {
            this.lastY = (float) player.getY();
        }
        return this.lastY;
    }

    @Override
    public double getZ() {
        PlayerEntity player = getPlayer();
        if (player != null) {
            this.lastZ = (float) player.getZ();
        }
        return this.lastZ;
    }

    @Override
    public void tick() {
        PlayerEntity player = getPlayer();
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

    public abstract boolean shouldPlaySound(@NotNull PlayerEntity player);

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
        PlayerEntity player = getPlayer();
        if (player == null) {
            return super.canPlaySound();
        }
        return !player.isSilent();
    }
}