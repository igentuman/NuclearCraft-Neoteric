package igentuman.nc.client.bomb;

import net.minecraft.core.BlockPos;

public class ActiveBomb {

    public static final int FLASH_MAX_DISTANCE = 1500;
    public static final int SHAKE_MAX_DISTANCE = 800;
    public static final int FIREBALL_DURATION = 120;
    public static final int MUSHROOM_DURATION = 680;
    public static final int GROUND_CLOUD_DURATION = 900;
    public static final int FADE_TICKS = 80;

    public final int id;
    public final BlockPos epicenter;
    public final float yield;
    public final double distance;
    public final float distanceFactor;
    public final float shakeFactor;
    public final int flashDuration;

    public int tickCounter;
    public int soundDelayTicks;
    public boolean soundPlayed;

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
        this.flashDuration = (int) (3 * (13 + 14 * distanceFactor));
    }

    public static final int GROUND_CLOUD_OFFSET = 1;
    public static final int STEM_OFFSET = 5;
    public static final int CAP_OFFSET = 30;

    public int fireballStart() { return flashDuration; }
    public int fireballEnd() { return flashDuration + FIREBALL_DURATION; }
    public int groundCloudStart() { return flashDuration + GROUND_CLOUD_OFFSET; }
    public int stemStart() { return flashDuration + STEM_OFFSET; }
    public int capStart() { return flashDuration + CAP_OFFSET; }
    public int mushroomStart() { return stemStart(); }
    public int mushroomEnd() { return stemStart() + MUSHROOM_DURATION; }
    public int groundCloudEnd() { return groundCloudStart() + GROUND_CLOUD_DURATION; }
    public int lifeEnd() { return Math.max(Math.max(fireballEnd(), mushroomEnd()), groundCloudEnd()) + FADE_TICKS; }

    public Phase currentPhase() {
        if (tickCounter < flashDuration) return Phase.FLASH;
        if (tickCounter < mushroomStart()) return Phase.FIREBALL;
        if (tickCounter < mushroomEnd()) return Phase.MUSHROOM;
        return Phase.FADE;
    }

    public float flashAlpha(float partialTick) {
        float t = tickCounter + partialTick;
        if (t >= flashDuration || distanceFactor <= 0f) return 0f;
        float peak = Math.max(1f, flashDuration * 0.25f);
        float a;
        if (t < peak) a = t / peak;
        else a = 1f - (t - peak) / Math.max(0.5f, flashDuration - peak);
        return Math.max(0f, Math.min(1f, a)) * distanceFactor;
    }

    public static final int CLOUD_WIPE_GROW_TICKS = 100;
    public static final int CLOUD_WIPE_HOLD_TICKS = 100;
    public static final int CLOUD_WIPE_FADE_TICKS = 300;

    public float cloudWipeRadius(float partialTick) {
        float t = tickCounter + partialTick - flashDuration;
        if (t < 0f) return 0f;
        float maxR = Math.max(200f, yield * 250f);
        int grow = CLOUD_WIPE_GROW_TICKS;
        int hold = CLOUD_WIPE_HOLD_TICKS;
        int fade = CLOUD_WIPE_FADE_TICKS;
        if (t < grow) {
            float x = t / grow;
            float ease = 1f - (1f - x) * (1f - x) * (1f - x);
            return maxR * ease;
        }
        if (t < grow + hold) return maxR;
        float f = (t - grow - hold) / fade;
        if (f >= 1f) return 0f;
        return maxR * (1f - f);
    }

    public float cameraShakeAmount(float partialTick) {
        if (shakeFactor <= 0f) return 0f;
        float t = tickCounter + partialTick;
        float halfFlash = flashDuration * 0.5f;
        float halfFireball = FIREBALL_DURATION * 0.5f;
        float envelope;
        if (t < halfFlash) {
            envelope = 1.0f;
        } else {
            float local = t - halfFlash;
            if (local < halfFireball) {
                envelope = 1.0f - 1.0f * (local / halfFireball);
            } else {
                envelope = 0f;
            }
        }
        return Math.max(0f, envelope) * shakeFactor;
    }

    public enum Phase { FLASH, FIREBALL, MUSHROOM, FADE }
}
