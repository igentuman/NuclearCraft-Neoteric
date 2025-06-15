package igentuman.nc.handler.config;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class FissionConfig {
    public static <T> List<T> toList(Collection<T> vals)
    {
        return new ArrayList<>(vals);
    }
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final FuelConfig FUEL_CONFIG = new FuelConfig(BUILDER);
    public static final FissionReactorConfig FISSION_CONFIG = new FissionReactorConfig(BUILDER);
    public static final ForgeConfigSpec spec = BUILDER.build();
    private static boolean loaded = false;
    private static List<Runnable> loadActions = new ArrayList<>();

    public static void setLoaded() {
        if (!loaded)
            loadActions.forEach(Runnable::run);
        loaded = true;
    }

    public static boolean isLoaded() {
        return loaded;
    }

    public static void onLoad(Runnable action) {
        if (loaded)
            action.run();
        else
            loadActions.add(action);
    }

    public static class FuelConfig {
        public final ForgeConfigSpec.ConfigValue<Double> HEAT_MULTIPLIER;
        public final ForgeConfigSpec.ConfigValue<Double> FUEL_HEAT_MULTIPLIER;
        public final ForgeConfigSpec.ConfigValue<Double> DEPLETION_MULTIPLIER;

        public FuelConfig(ForgeConfigSpec.Builder builder) {
            builder.comment("Settings for reactor fuel").push("reactor_fuel");

            FUEL_HEAT_MULTIPLIER = builder
                    .comment("Heat multiplier. Affects to all fuels.")
                    .defineInRange("fuel_heat_multiplier", 1, 0.01D, 100D);

            HEAT_MULTIPLIER = builder
                    .comment("Heat multiplier affects on heat/cooling ratio multiplier.")
                    .define("heat_multiplier", 3.24444444);

            DEPLETION_MULTIPLIER = builder
                    .comment("Depletion multiplier. Affects how long fuel lasts.")
                    .defineInRange("depletion_multiplier", 1D, 0D, 1000D);

            builder.pop();
        }

    }

    public static class FissionReactorConfig {
        public final ForgeConfigSpec.ConfigValue<Integer> MIN_SIZE;
        public final ForgeConfigSpec.ConfigValue<Integer> MAX_SIZE;
        public final ForgeConfigSpec.ConfigValue<Double> HEAT_MULTIPLIER;
        public final ForgeConfigSpec.ConfigValue<Double> HEAT_MULTIPLIER_CAP;
        public final ForgeConfigSpec.ConfigValue<Double> MODERATOR_FE_MULTIPLIER;
        public final ForgeConfigSpec.ConfigValue<Double> MODERATOR_HEAT_MULTIPLIER;
        public final ForgeConfigSpec.ConfigValue<Double> EXPLOSION_RADIUS;
        public final ForgeConfigSpec.ConfigValue<Double> HEAT_CAPACITY;

        public final ForgeConfigSpec.ConfigValue<Double> FE_GENERATION_MULTIPLIER;
        public final ForgeConfigSpec.ConfigValue<Double> BOILING_MULTIPLIER;
        public final ForgeConfigSpec.ConfigValue<Boolean> BOILING_ENABLED;
        public final ForgeConfigSpec.ConfigValue<Boolean> ACTIVE_HEATSINK_PRIME;
        public final ForgeConfigSpec.ConfigValue<Integer> ACTIVE_HEATSINK_COOLANT_PER_TICK;

        public FissionReactorConfig(ForgeConfigSpec.Builder builder) {
            builder.comment("Settings for Fission Reactor").push("fission_reactor");

            MIN_SIZE = builder
                    .comment("Reactor min size.")
                    .defineInRange("min_size", 3, 3, 24);

            MAX_SIZE = builder
                    .comment("Reactor max size.")
                    .defineInRange("max_size", 26, 5, 32);

            EXPLOSION_RADIUS = builder
                    .comment("Explosion size if reactor overheats. 4 - TNT size. Set to 0 to disable explosion.")
                    .defineInRange("reactor_explosion_radius", 4f, 0.0f, 20f);

            HEAT_CAPACITY = builder
                    .comment("How much reactor may collect heat before meltdown.")
                    .defineInRange("heat_capacity", 1000000, 1000D, 100000000D);

            HEAT_MULTIPLIER = builder
                    .comment("Affects how relation of reactor cooling and heating affects to FE generation.")
                    .defineInRange("heat_multiplier", 1, 0.01D, 20D);

            HEAT_MULTIPLIER_CAP = builder
                    .comment("Limit for heat_multiplier max value.")
                    .defineInRange("heat_multiplier_cap", 3D, 0.01D, 3D);

            MODERATOR_FE_MULTIPLIER = builder
                    .comment("Each attachment of moderator to fuel cell will increase fuel FE generation by given percent value.")
                    .defineInRange("moderator_fe_multiplier", 16.666666667D, 0D, 1000D);

            MODERATOR_HEAT_MULTIPLIER = builder
                    .comment("Each attachment of moderator to fuel cell will increase fuel heat generation by given percent value.")
                    .defineInRange("moderator_heat_multiplier", 33.33333333D, 0D, 1000D);

            BOILING_ENABLED = builder
                    .comment("If you don't need reactor boiling mode, just disable.")
                    .define("supports_boiling_mode", true);

            BOILING_MULTIPLIER = builder
                    .comment("Rate at which steam recipes produced.")
                    .defineInRange("boiling_mult", 100D, 0.01D, 1000000D);

            FE_GENERATION_MULTIPLIER = builder
                    .comment("Affects how much energy reactors produce.")
                    .defineInRange("fe_generation_multiplier", 10D, 0.01D, 1000000D);

            ACTIVE_HEATSINK_PRIME = builder
                    .comment("If true, active coolers will be counted in placement rules for other heat sinks.")
                    .define("active_heatsink_prime", true);

            ACTIVE_HEATSINK_COOLANT_PER_TICK = builder
                    .comment("How much coolant active heat sink will consume per tick.")
                    .defineInRange("active_heatsink_coolant_per_tick", 10, 1, 10000);

            builder.pop();
        }

    }

}