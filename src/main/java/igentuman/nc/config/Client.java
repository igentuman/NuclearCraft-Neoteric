package igentuman.nc.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Client {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.EnumValue<NukeQuality> NUKE_QUALITY;
    public static final ModConfigSpec.IntValue NUKE_MATURE_HOLD_SECONDS;
    public static final ModConfigSpec.BooleanValue NUKE_THERMAL_REFRACTION;
    public static final ModConfigSpec.BooleanValue NUKE_HUD_FLASH;

    public static final ModConfigSpec SPEC;

    public static NukeQuality nukeQuality = NukeQuality.BALANCED;
    public static int nukeMatureHoldSeconds = 300;
    public static boolean nukeThermalRefraction = true;
    public static boolean nukeHudFlash = true;

    static {
        BUILDER.push("nuke");
        NUKE_QUALITY = BUILDER
                .comment("Volumetric explosion quality preset: LOW, BALANCED or HIGH.")
                .defineEnum("quality", NukeQuality.BALANCED);
        NUKE_MATURE_HOLD_SECONDS = BUILDER
                .comment("Seconds the fully formed mushroom cloud stays before its one-minute fade.")
                .defineInRange("mature_hold_seconds", 300, 300, 3600);
        NUKE_THERMAL_REFRACTION = BUILDER
                .comment("Render the expanding thermal shockwave refraction.")
                .define("thermal_refraction", true);
        NUKE_HUD_FLASH = BUILDER
                .comment("Short screen exposure flash on detonation.")
                .define("hud_flash", true);
        BUILDER.pop();

        SPEC = BUILDER.build();
    }

    public static void refresh() {
        nukeQuality = NUKE_QUALITY.get();
        nukeMatureHoldSeconds = NUKE_MATURE_HOLD_SECONDS.get();
        nukeThermalRefraction = NUKE_THERMAL_REFRACTION.get();
        nukeHudFlash = NUKE_HUD_FLASH.get();
    }

    public enum NukeQuality {
        LOW(0.5f, 32, 2, 128, 0f, 16, 384, 48),
        BALANCED(0.5f, 64, 4, 384, 1f, 48, 1280, 128),
        HIGH(0.75f, 96, 6, 512, 1f, 96, 2560, 256);

        public final float volumeScale;
        public final int maxSamples;
        public final int lightProbes;
        public final int smokeLiveCap;
        public final float detail;
        public final int debrisFires;
        public final int debrisLiveCap;
        public final int shockDebrisLiveCap;

        NukeQuality(float volumeScale, int maxSamples, int lightProbes, int smokeLiveCap, float detail,
                    int debrisFires, int debrisLiveCap, int shockDebrisLiveCap) {
            this.volumeScale = volumeScale;
            this.maxSamples = maxSamples;
            this.lightProbes = lightProbes;
            this.smokeLiveCap = smokeLiveCap;
            this.detail = detail;
            this.debrisFires = debrisFires;
            this.debrisLiveCap = debrisLiveCap;
            this.shockDebrisLiveCap = shockDebrisLiveCap;
        }
    }
}
