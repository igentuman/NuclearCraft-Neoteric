package igentuman.nc.handler.config;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class KugelblitzConfig {
    public static <T> List<T> toList(Collection<T> vals)
    {
        return new ArrayList<>(vals);
    }
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final GeneralConfig KUGELBLITZ_CONFIG = new GeneralConfig(BUILDER);
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

    public static class GeneralConfig {
        public ForgeConfigSpec.ConfigValue<Integer> LASER_DISTANCE;
        public ForgeConfigSpec.ConfigValue<Double> GENERATION_MULTIPLIER;
        public ForgeConfigSpec.ConfigValue<Double> EVAPORATION_MULTIPLIER;

        public GeneralConfig(ForgeConfigSpec.Builder builder) {
            builder.comment("Settings for Fusion Reactor").push("general");

            LASER_DISTANCE = builder
                    .comment("Laser burst distance.")
                    .defineInRange("min_size", 32, 8, 64);

            GENERATION_MULTIPLIER = builder
                    .comment("Multiplier for kugelblitz chamber FE generation.")
                    .defineInRange("fe_generation_multiplier", 1.0, 0.001, 1000.0);

            EVAPORATION_MULTIPLIER = builder
                    .comment("Adjust rate of blackhole evaporation.")
                    .defineInRange("blackhole_evaporation_rate", 1.0, 0.001, 1000.0);

            builder.pop();
        }

    }
}