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
    public static final ParticleChamberConfig PARTICLE_CHAMBER_CONFIG = new ParticleChamberConfig(BUILDER);
    public static final DecayChamberConfig DECAY_CHAMBER_CONFIG = new DecayChamberConfig(BUILDER);
    public static final CollisionChamberConfig COLLISION_CHAMBER_CONFIG = new CollisionChamberConfig(BUILDER);
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

    public static class ParticleChamberConfig {
        public final ForgeConfigSpec.ConfigValue<Integer> MIN_SIZE;
        public final ForgeConfigSpec.ConfigValue<Integer> MAX_SIZE;

        public ParticleChamberConfig(ForgeConfigSpec.Builder builder) {
            builder.comment("Target chamber").push("target_chamber");

            MIN_SIZE = builder
                    .comment("Min exterior size (cube edge, odd).")
                    .defineInRange("min_size", 5, 5, 11);

            MAX_SIZE = builder
                    .comment("Max exterior size (cube edge, odd).")
                    .defineInRange("max_size", 11, 7, 11);

            builder.pop();
        }
    }

    public static class DecayChamberConfig {
        public final ForgeConfigSpec.ConfigValue<Integer> MIN_SIZE;
        public final ForgeConfigSpec.ConfigValue<Integer> MAX_SIZE;
        public final ForgeConfigSpec.ConfigValue<Integer> BASE_POWER;

        public DecayChamberConfig(ForgeConfigSpec.Builder builder) {
            builder.comment("Decay chamber").push("decay_chamber");

            MIN_SIZE = builder
                    .comment("Min size (cube edge, odd).")
                    .defineInRange("min_size", 5, 5, 11);

            MAX_SIZE = builder
                    .comment("Max size (cube edge, odd).")
                    .defineInRange("max_size", 11, 7, 11);

            BASE_POWER = builder
                    .comment("Base RF/tick consumed while running.")
                    .defineInRange("base_power", 2000, 0, Integer.MAX_VALUE);

            builder.pop();
        }
    }

    public static class CollisionChamberConfig {
        public final ForgeConfigSpec.ConfigValue<Integer> MIN_SIZE;
        public final ForgeConfigSpec.ConfigValue<Integer> MAX_SIZE;
        public final ForgeConfigSpec.ConfigValue<Integer> BASE_POWER;

        public CollisionChamberConfig(ForgeConfigSpec.Builder builder) {
            builder.comment("Collision chamber").push("collision_chamber");

            MIN_SIZE = builder
                    .comment("Min interior size (cube edge, odd).")
                    .defineInRange("min_size", 17, 13, 21);

            MAX_SIZE = builder
                    .comment("Max interior size (cube edge, odd).")
                    .defineInRange("max_size", 17, 13, 21);

            BASE_POWER = builder
                    .comment("Base RF/tick consumed while running.")
                    .defineInRange("base_power", 2000, 0, Integer.MAX_VALUE);

            builder.pop();
        }
    }

    public static class GeneralConfig {
        public final ForgeConfigSpec.ConfigValue<Integer> SCALE;
        public final ForgeConfigSpec.ConfigValue<Double> BEAM_ATTENUATION_RATE;
        public final ForgeConfigSpec.ConfigValue<Double> BEAM_SCALING;
        public final ForgeConfigSpec.ConfigValue<Integer> BASE_HEAT_CAPACITY;
        public final ForgeConfigSpec.ConfigValue<Integer> BASE_ENERGY_REQUIREMENT;
        public final ForgeConfigSpec.ConfigValue<Double> THERMAL_CONDUCTIVITY;
        public final ForgeConfigSpec.ConfigValue<Integer> MAX_TEMP;
        public final ForgeConfigSpec.ConfigValue<Integer> RING_ACCELERATOR_INPUT_PARTICLE_MIN_ENERGY;
        public final ForgeConfigSpec.ConfigValue<Boolean> MELTDOWN_ENABLED;

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

            BASE_HEAT_CAPACITY = builder
                    .comment("Base heat capacity per accelerator block.")
                    .comment("Total capacity = base * block count. Larger accelerators = larger thermal mass.")
                    .defineInRange("base_heat_capacity", 25000, 1, Integer.MAX_VALUE);

            BASE_ENERGY_REQUIREMENT = builder
                    .comment("Base energy requirement per accelerator.")
                    .defineInRange("base_energy_requirement", 10000, 1, Integer.MAX_VALUE);

            THERMAL_CONDUCTIVITY = builder
                    .comment("Thermal conductivity of casing. Used for external (biome) heat exchange.")
                    .comment("Higher = faster equalization to ambient temperature.")
                    .defineInRange("thermal_conductivity", 0.0025d, 0.0, 1.0);

            MAX_TEMP = builder
                    .comment("Maximum representable temperature (Kelvin). Heat is mapped to [0, MAX_TEMP] using capacity.")
                    .defineInRange("max_temp", 400, 1, 10000);

            RING_ACCELERATOR_INPUT_PARTICLE_MIN_ENERGY = builder
                    .comment("Minimal energy of input particle for ring accelerators in kEV.")
                    .defineInRange("ring_accelerator_input_particle_min_energy", 5000, 1, 100000);

            MELTDOWN_ENABLED = builder
                    .comment("If true, components exceeding their max operating temperature will explode.")
                    .define("meltdown_enabled", true);

            builder.pop();
        }

    }
}
