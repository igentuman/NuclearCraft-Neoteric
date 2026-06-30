package igentuman.nc.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Multiblocks {
    public static final ModConfigSpec.ConfigValue<Boolean> DEBUG_LOGGING;

    public static final ModConfigSpec.IntValue FISSION_MIN_SIZE;
    public static final ModConfigSpec.IntValue FISSION_MAX_SIZE;
    public static final ModConfigSpec.DoubleValue FISSION_EXPLOSION_RADIUS;
    public static final ModConfigSpec.DoubleValue FISSION_HEAT_CAPACITY;
    public static final ModConfigSpec.DoubleValue FISSION_HEAT_MULTIPLIER;
    public static final ModConfigSpec.DoubleValue FISSION_HEAT_MULTIPLIER_CAP;
    public static final ModConfigSpec.DoubleValue FISSION_MODERATOR_FE_MULTIPLIER;
    public static final ModConfigSpec.DoubleValue FISSION_MODERATOR_HEAT_MULTIPLIER;
    public static final ModConfigSpec.ConfigValue<Boolean> FISSION_SUPPORTS_BOILING;
    public static final ModConfigSpec.DoubleValue FISSION_BOILING_MULT;
    public static final ModConfigSpec.DoubleValue FISSION_FE_GENERATION_MULTIPLIER;
    public static final ModConfigSpec.IntValue FISSION_ACTIVE_COOLANT_PER_TICK;
    public static final ModConfigSpec.DoubleValue FISSION_FUEL_HEAT_MULTIPLIER;
    public static final ModConfigSpec.DoubleValue FISSION_DEPLETION_MULTIPLIER;

    public static final ModConfigSpec SPEC;

    // Plain mirrors of the config values, refreshed on (re)load so runtime hot paths read a field
    // instead of ModConfigSpec.get() (which throws before config load). Defaults match the spec.
    public static int fissionMinSize = 3;
    public static int fissionMaxSize = 26;
    public static double fissionExplosionRadius = 4.0;
    public static double fissionHeatCapacity = 1_000_000;
    public static double fissionHeatMultiplier = 1.0;
    public static double fissionHeatMultiplierCap = 3.0;
    public static double fissionModeratorFeMultiplier = 16.666666667;
    public static double fissionModeratorHeatMultiplier = 33.33333333;
    public static boolean fissionSupportsBoiling = true;
    public static double fissionBoilingMult = 100.0;
    public static double fissionFeGenerationMultiplier = 10.0;
    public static int fissionActiveCoolantPerTick = 10;
    public static double fissionFuelHeatMultiplier = 1.0;
    public static double fissionDepletionMultiplier = 1.0;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.push("general");
        DEBUG_LOGGING = builder.comment("Enable debug logging for multiblocks.")
                .define("debug_logging", false);
        builder.pop();

        builder.push("fission_reactor");
        FISSION_MIN_SIZE = builder.comment("Minimum reactor edge length (cuboid casing).")
                .defineInRange("min_size", 3, 3, 24);
        FISSION_MAX_SIZE = builder.comment("Maximum reactor edge length (cuboid casing).")
                .defineInRange("max_size", 26, 5, 32);
        FISSION_EXPLOSION_RADIUS = builder.comment("Explosion radius on meltdown. 4 ~ TNT. 0 disables the explosion.")
                .defineInRange("explosion_radius", 4.0, 0.0, 20.0);
        FISSION_HEAT_CAPACITY = builder.comment("Base heat capacity before the size scaling factor; meltdown when exceeded.")
                .defineInRange("heat_capacity", 1_000_000.0, 1000.0, 100_000_000.0);
        FISSION_HEAT_MULTIPLIER = builder.comment("Heat/cooling ratio coefficient feeding the heat multiplier curve.")
                .defineInRange("heat_multiplier", 1.0, 0.01, 20.0);
        FISSION_HEAT_MULTIPLIER_CAP = builder.comment("Upper bound of the collected-heat multiplier.")
                .defineInRange("heat_multiplier_cap", 3.0, 0.01, 3.0);
        FISSION_MODERATOR_FE_MULTIPLIER = builder.comment("Percent FE bonus per moderator attached to a fuel cell.")
                .defineInRange("moderator_fe_multiplier", 16.666666667, 0.0, 1000.0);
        FISSION_MODERATOR_HEAT_MULTIPLIER = builder.comment("Percent heat bonus per moderator attached to a fuel cell.")
                .defineInRange("moderator_heat_multiplier", 33.33333333, 0.0, 1000.0);
        FISSION_SUPPORTS_BOILING = builder.comment("Allow toggling the reactor into boiling (steam) mode.")
                .define("supports_boiling_mode", true);
        FISSION_BOILING_MULT = builder.comment("Steam production rate multiplier (percent).")
                .defineInRange("boiling_mult", 100.0, 0.01, 1_000_000.0);
        FISSION_FE_GENERATION_MULTIPLIER = builder.comment("Global FE generation multiplier.")
                .defineInRange("fe_generation_multiplier", 10.0, 0.01, 1_000_000.0);
        FISSION_ACTIVE_COOLANT_PER_TICK = builder.comment("mB of coolant a single active heat sink consumes per tick.")
                .defineInRange("active_heatsink_coolant_per_tick", 10, 1, 10000);
        builder.pop();

        builder.push("fission_fuel");
        FISSION_FUEL_HEAT_MULTIPLIER = builder.comment("Heat multiplier applied to all fission fuels.")
                .defineInRange("fuel_heat_multiplier", 1.0, 0.01, 100.0);
        FISSION_DEPLETION_MULTIPLIER = builder.comment("Fuel longevity multiplier; higher = fuel lasts longer.")
                .defineInRange("depletion_multiplier", 1.0, 0.01, 1000.0);
        builder.pop();

        SPEC = builder.build();
    }

    /** Pushes config values into the plain mirror fields. Call on config load/reload. */
    public static void refresh() {
        fissionMinSize = FISSION_MIN_SIZE.get();
        fissionMaxSize = FISSION_MAX_SIZE.get();
        fissionExplosionRadius = FISSION_EXPLOSION_RADIUS.get();
        fissionHeatCapacity = FISSION_HEAT_CAPACITY.get();
        fissionHeatMultiplier = FISSION_HEAT_MULTIPLIER.get();
        fissionHeatMultiplierCap = FISSION_HEAT_MULTIPLIER_CAP.get();
        fissionModeratorFeMultiplier = FISSION_MODERATOR_FE_MULTIPLIER.get();
        fissionModeratorHeatMultiplier = FISSION_MODERATOR_HEAT_MULTIPLIER.get();
        fissionSupportsBoiling = FISSION_SUPPORTS_BOILING.get();
        fissionBoilingMult = FISSION_BOILING_MULT.get();
        fissionFeGenerationMultiplier = FISSION_FE_GENERATION_MULTIPLIER.get();
        fissionActiveCoolantPerTick = FISSION_ACTIVE_COOLANT_PER_TICK.get();
        fissionFuelHeatMultiplier = FISSION_FUEL_HEAT_MULTIPLIER.get();
        fissionDepletionMultiplier = FISSION_DEPLETION_MULTIPLIER.get();
    }
}
