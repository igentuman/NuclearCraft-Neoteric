package igentuman.nc.client.bomb;

import net.minecraft.core.BlockPos;

public class ActiveBomb {

    public static final int FLASH_MAX_DISTANCE = 1500;
    public static final int SHAKE_MAX_DISTANCE = 800;
    public static final int SHAKE_DECAY_TICKS = 60;
    public static final float HUD_FLASH_PEAK = 0.05f;
    public static final float HUD_FLASH_DECAY = 0.25f;
    public static final float HUD_FLASH_STRENGTH = 0.85f;

    public final int id;
    public final BlockPos epicenter;
    public final float yield;
    public final double distance;
    public final float distanceFactor;
    public final float shakeFactor;
    public final int shakeHoldTicks;

    public final long seed;
    public final NukeVisualState visual = new NukeVisualState();
    public final SurfaceHeightCache surface;

    public int tickCounter;
    public int soundDelayTicks;
    public boolean soundPlayed;
    public float debrisSweep;

    public ActiveBomb(int id, BlockPos epicenter, float yield, int soundDelayTicks, double distance) {
        this.id = id;
        this.epicenter = epicenter;
        this.yield = yield;
        this.soundDelayTicks = soundDelayTicks;
        this.tickCounter = 0;
        this.soundPlayed = soundDelayTicks < 0;
        this.distance = distance;
        this.distanceFactor = (float) Math.sqrt(Math.max(0.0, Math.min(1.0, 1.0 - distance / FLASH_MAX_DISTANCE)));
        this.shakeFactor = (float) Math.max(0.0, Math.min(1.0, 1.0 - distance / SHAKE_MAX_DISTANCE));
        this.shakeHoldTicks = (int) (1.5f * (13 + 14 * distanceFactor));
        this.seed = epicenter.asLong() * 31L + id;
        this.surface = new SurfaceHeightCache(epicenter,
                NukeTimeline.GROUND_MAX_RADIUS * NukeTimeline.scale(yield) * NukeTimeline.BOUNDS_MARGIN);
    }

    public float ageSeconds(float partialTick) {
        return (tickCounter + partialTick) / 20f;
    }

    public boolean expired() {
        return tickCounter > NukeTimeline.lifeEndTicks();
    }

    public float flashAlpha(float partialTick) {
        if (distanceFactor <= 0f) return 0f;
        float age = ageSeconds(partialTick);
        float a;
        if (age < HUD_FLASH_PEAK) a = age / HUD_FLASH_PEAK;
        else a = (float) Math.exp(-(age - HUD_FLASH_PEAK) / HUD_FLASH_DECAY);
        return Math.max(0f, Math.min(1f, a)) * HUD_FLASH_STRENGTH * distanceFactor;
    }

    public float cameraShakeAmount(float partialTick) {
        if (shakeFactor <= 0f) return 0f;
        float t = tickCounter + partialTick;
        float envelope;
        if (t < shakeHoldTicks) {
            envelope = 1.0f;
        } else {
            envelope = 1.0f - (t - shakeHoldTicks) / SHAKE_DECAY_TICKS;
        }
        return Math.max(0f, envelope) * shakeFactor;
    }
}
