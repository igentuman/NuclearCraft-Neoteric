package igentuman.nc.handler.config;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class AcceleratorConfig {
    public static <T> List<T> toList(Collection<T> vals)
    {
        return new ArrayList<>(vals);
    }
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final GeneralConfig ACCELERATOR_CONFIG = new GeneralConfig(BUILDER);
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
        public ForgeConfigSpec.ConfigValue<Integer> SCALE;
        public ForgeConfigSpec.ConfigValue<Double> BEAM_ATTENUATION_RATE;
        public ForgeConfigSpec.ConfigValue<Double> BEAM_SCALING;

        public GeneralConfig(ForgeConfigSpec.Builder builder) {
            builder.comment("Settings for accelerators").push("general");

            SCALE = builder
                    .comment("Accelerators size scale.")
                    .comment("Defines size range for accelerators and affects calculations.")
                    .comment("Size chart according to the scale:")
                    .comment("1 - min size = 6, max size = 100")
                    .comment("2 - min size = 60, max size = 1000")
                    .comment("3 - min size = 600, max size = 10000")
                    .defineInRange("scale_preset", 1, 1, 3);

            BEAM_ATTENUATION_RATE = builder
                    .comment("Beam attenuation rate.")
                    .comment("Defines how much focus is lost per block in the beamline.")
                    .comment("Default value is 0.02, which means 2% energy loss per block.")
                    .comment("It also depends on scale preset")
                    .defineInRange("beam_attenuation_rate", 0.02, 0.0, 1.0);

            BEAM_SCALING = builder
                    .comment("The scaling factor for the beam attenuation equation.")
                    .defineInRange("beam_scaling", 10000, 0.0, Integer.MAX_VALUE);

            builder.pop();
        }

    }
}