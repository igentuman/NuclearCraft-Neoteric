package igentuman.nc.client.bomb;

import igentuman.nc.config.Client;
import net.minecraft.util.Mth;

public final class NukeTimeline {

    public static final float FLASH_EXPAND = 0.10f;
    public static final float THERMAL_START = 0.10f;
    public static final float MORPH_START = 1.00f;
    public static final float MORPH_DURATION = 10.00f;
    public static final float MIN_MATURE_HOLD = 300.00f;
    public static final float FADE_DURATION = 60.00f;

    public static final float SCALE_PER_YIELD = 32f;
    public static final float MIN_SCALE = 8f;
    public static final float MAX_SCALE = 128f;

    public static final float FLASH_RADIUS = 0.45f;
    public static final float FIREBALL_RADIUS = 0.75f;
    public static final float CAP_RADIUS = 1.6f;
    public static final float CAP_HEIGHT = 2.5f;
    public static final float CAP_THICKNESS = 0.55f;
    public static final float STEM_RADIUS = 0.3f;
    public static final float MATURE_GROWTH = 0.15f;
    public static final float MATURE_ASCENT = 0.2f;
    public static final float CAP_SEED = 0.6f;
    public static final float BASE_SPREAD = 0.2f;
    public static final float BASE_FLATTEN = 0.25f;
    public static final float BASE_COOLING = 4.5f;

    public static final float THERMAL_SPEED = 6f;
    public static final float THERMAL_FADE = 3.0f;
    public static final float THERMAL_THICKNESS = 0.18f;
    public static final float HAZE_RADIUS = 2.5f;
    public static final float HAZE_DURATION = 20f;
    public static final float GROUND_SPEED = 1.5f;
    public static final float GROUND_MAX_RADIUS = 6f;
    public static final float GROUND_WAKE_DURATION = 40f;

    public static final float BOUNDS_MARGIN = 1.25f;

    private NukeTimeline() {}

    public static float matureHold() {
        return Math.max(MIN_MATURE_HOLD, Client.nukeMatureHoldSeconds);
    }

    public static float morphEnd() {
        return MORPH_START + MORPH_DURATION;
    }

    public static float fadeStart() {
        return morphEnd() + matureHold();
    }

    public static float lifeEnd() {
        return fadeStart() + FADE_DURATION;
    }

    public static int lifeEndTicks() {
        return Mth.ceil(lifeEnd() * 20f);
    }

    public static float scale(float yield) {
        float safe = Float.isFinite(yield) && yield > 0f ? yield : 1f;
        return Mth.clamp(SCALE_PER_YIELD * safe, MIN_SCALE, MAX_SCALE);
    }

    public static float morph(float age) {
        return smootherstep(Mth.clamp((age - MORPH_START) / MORPH_DURATION, 0f, 1f));
    }

    public static float matureGrowth(float age) {
        float t = Math.max(0f, age - morphEnd());
        return 1f - (float) Math.exp(-t / (matureHold() * 0.5f));
    }

    public static float fade(float age) {
        float t = Mth.clamp((age - fadeStart()) / FADE_DURATION, 0f, 1f);
        return 1f - smootherstep(t);
    }

    public static float flash(float age) {
        if (age < 0f) return 0f;
        if (age < FLASH_EXPAND) return smootherstep(age / FLASH_EXPAND);
        return (float) Math.exp(-(age - FLASH_EXPAND) / 0.35f);
    }

    private static final float FLASH_SHARPNESS = 4f;
    private static final float FLASH_NORM = 1f - (float) Math.exp(-FLASH_SHARPNESS);
    private static final float FIREBALL_TAU = (FIREBALL_RADIUS - FLASH_RADIUS) * FLASH_EXPAND * FLASH_NORM
            / (FLASH_RADIUS * FLASH_SHARPNESS * (float) Math.exp(-FLASH_SHARPNESS));

    public static float fireballRadius(float age, float r) {
        if (age <= 0f) return 0f;
        if (age < FLASH_EXPAND) {
            float s = age / FLASH_EXPAND;
            return r * FLASH_RADIUS * (1f - (float) Math.exp(-FLASH_SHARPNESS * s)) / FLASH_NORM;
        }
        float grow = 1f - (float) Math.exp(-(age - FLASH_EXPAND) / FIREBALL_TAU);
        return r * (FLASH_RADIUS + (FIREBALL_RADIUS - FLASH_RADIUS) * grow);
    }

    public static float thermalRadius(float age, float r) {
        if (age < THERMAL_START) return -1f;
        return THERMAL_SPEED * r * (age - THERMAL_START);
    }

    public static float thermalStrength(float age) {
        if (age < THERMAL_START) return 0f;
        float t = (age - THERMAL_START) / THERMAL_FADE;
        if (t >= 1f) return 0f;
        float rise = Mth.clamp((age - THERMAL_START) / 0.05f, 0f, 1f);
        return rise * (1f - smootherstep(t));
    }

    public static float hazeStrength(float age) {
        if (age < THERMAL_START) return 0f;
        float t = (age - THERMAL_START) / HAZE_DURATION;
        if (t >= 1f) return 0f;
        float rise = Mth.clamp((age - THERMAL_START) / 0.3f, 0f, 1f);
        return rise * (1f - smootherstep(t));
    }

    public static float groundRadius(float age, float r) {
        if (age < MORPH_START) return 0f;
        float t = age - MORPH_START;
        float max = GROUND_MAX_RADIUS * r;
        float k = GROUND_SPEED / GROUND_MAX_RADIUS;
        return max * (1f - (float) Math.exp(-k * t));
    }

    public static float groundSpeed(float age, float r) {
        if (age < MORPH_START) return 0f;
        float k = GROUND_SPEED / GROUND_MAX_RADIUS;
        return GROUND_SPEED * r * (float) Math.exp(-k * (age - MORPH_START));
    }

    public static float groundIntensity(float age) {
        if (age < MORPH_START) return 0f;
        float t = (age - MORPH_START) / GROUND_WAKE_DURATION;
        if (t >= 1f) return 0f;
        float rise = Mth.clamp((age - MORPH_START) / 0.5f, 0f, 1f);
        return rise * (1f - smootherstep(t));
    }

    public static float heat(float age) {
        if (age <= 0f) return 1f;
        float cooling = (float) Math.exp(-Math.max(0f, age - FLASH_EXPAND) / 3f);
        float residual = 0.12f * (float) Math.exp(-Math.max(0f, age - morphEnd()) / 120f);
        return Math.max(cooling, residual);
    }

    public static float baseHeat(float age) {
        if (age <= 0f) return 1f;
        float cooling = (float) Math.exp(-Math.max(0f, age - FLASH_EXPAND) / BASE_COOLING);
        float residual = 0.06f * (float) Math.exp(-Math.max(0f, age - morphEnd()) / 60f);
        return Math.max(cooling, residual);
    }

    public static float smootherstep(float t) {
        t = Mth.clamp(t, 0f, 1f);
        return t * t * t * (t * (t * 6f - 15f) + 10f);
    }
}
