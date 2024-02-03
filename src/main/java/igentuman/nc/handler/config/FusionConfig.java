package igentuman.nc.handler.config;

import igentuman.nc.content.Electromagnets;
import igentuman.nc.content.RFAmplifier;
import igentuman.nc.content.energy.BatteryBlocks;
import igentuman.nc.content.energy.RTGs;
import igentuman.nc.content.energy.SolarPanels;
import igentuman.nc.content.storage.BarrelBlocks;
import igentuman.nc.multiblock.turbine.TurbineRegistration;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import static igentuman.nc.world.dimension.Dimensions.WASTELAIND_ID;

public class FusionConfig {
    public static <T> List<T> toList(Collection<T> vals)
    {
        return new ArrayList<>(vals);
    }
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final FusionReactorConfig FUSION_CONFIG = new FusionReactorConfig(BUILDER);
    public static final ElectromagnetsConfig ELECTROMAGNETS_CONFIG = new ElectromagnetsConfig(BUILDER);
    public static final RFAmplifierConfig RF_AMPLIFIERS_CONFIG = new RFAmplifierConfig(BUILDER);
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

    public static class RFAmplifierConfig {
        public ForgeConfigSpec.ConfigValue<List<Boolean>> REGISTERED;
        public ForgeConfigSpec.ConfigValue<List<Integer>> POWER;
        public ForgeConfigSpec.ConfigValue<List<Integer>> HEAT;
        public ForgeConfigSpec.ConfigValue<List<Integer>> VOLTAGE;

        public RFAmplifierConfig(ForgeConfigSpec.Builder builder) {
            builder.comment("Settings for RF Amplifiers").push("rf_amplifiers");

            REGISTERED = builder
                    .comment("If RF Amplifier are registered.")
                    .define("registered", RFAmplifier.initialRegistered(), o -> o instanceof ArrayList);

            POWER = builder
                    .comment("Power consumption (FE/t): " + String.join(", ", RFAmplifier.all().keySet()))
                    .define("power", toList(RFAmplifier.initialPower()), o -> o instanceof ArrayList);

            HEAT = builder
                    .comment("Heat generation: " + String.join(", ", RFAmplifier.all().keySet()))
                    .define("heat", toList(RFAmplifier.initialHeat()), o -> o instanceof ArrayList);

            VOLTAGE = builder
                    .comment("Amplification Voltage: " + String.join(", ", RFAmplifier.all().keySet()))
                    .define("voltage", toList(RFAmplifier.initialVoltage()), o -> o instanceof ArrayList);

            builder.pop();
        }

    }

    public static class ElectromagnetsConfig {
        public ForgeConfigSpec.ConfigValue<List<Boolean>> REGISTERED;
        public ForgeConfigSpec.ConfigValue<List<Integer>> POWER;
        public ForgeConfigSpec.ConfigValue<List<Integer>> HEAT;
        public ForgeConfigSpec.ConfigValue<List<Double>> MAGNETIC_FIELD;

        public ElectromagnetsConfig(ForgeConfigSpec.Builder builder) {
            builder.comment("Settings for Electromagnets").push("electromagnets");

            REGISTERED = builder
                    .comment("If Electromagnets are registered.")
                    .define("registered", Electromagnets.initialRegistered(), o -> o instanceof ArrayList);

            POWER = builder
                    .comment("Power consumption (FE/t): " + String.join(", ", Electromagnets.all().keySet()))
                    .define("power", toList(Electromagnets.initialPower()), o -> o instanceof ArrayList);

            HEAT = builder
                    .comment("Heat generation: " + String.join(", ", Electromagnets.all().keySet()))
                    .define("heat", toList(Electromagnets.initialHeat()), o -> o instanceof ArrayList);

            MAGNETIC_FIELD = builder
                    .comment("Magnetic field strength: " + String.join(", ", Electromagnets.all().keySet()))
                    .define("heat", toList(Electromagnets.initialMagneticField()), o -> o instanceof ArrayList);

            builder.pop();
        }

    }

    public static class FusionReactorConfig {
        public ForgeConfigSpec.ConfigValue<Integer> MIN_SIZE;
        public ForgeConfigSpec.ConfigValue<Integer> MAX_SIZE;
        public ForgeConfigSpec.ConfigValue<Double> MINIMAL_MAGNETIC_FIELD;
        public ForgeConfigSpec.ConfigValue<Double> RF_AMPLIFICATION_MULTIPLIER;
        public ForgeConfigSpec.ConfigValue<Double> PLASMA_TO_ENERGY_CONVERTION;
        public ForgeConfigSpec.ConfigValue<Double> EXPLOSION_RADIUS;

        public FusionReactorConfig(ForgeConfigSpec.Builder builder) {
            builder.comment("Settings for Fusion Reactor").push("fusion_reactor");

            MIN_SIZE = builder
                    .comment("Min reactor size.")
                    .defineInRange("min_size", 1, 1, 24);

            MAX_SIZE = builder
                    .comment("Max reactor size.")
                    .defineInRange("max_size", 32, 3, 48);

            EXPLOSION_RADIUS = builder
                    .comment("Explosion size if reactor overheats. 4 - TNT size. Set to 0 to disable explosion.")
                    .defineInRange("reactor_explosion_radius", 4f, 0.0f, 20f);

            MINIMAL_MAGNETIC_FIELD = builder
                    .comment("Minimal magnetic field required to operate reactor. (Depends on reactor size).")
                    .defineInRange("minimal_magnetic_field", 8, 1D, 100D);

            RF_AMPLIFICATION_MULTIPLIER = builder
                    .comment("Affects heating rate for plasma by rf amplifiers.")
                    .defineInRange("rf_amplification_multiplier", 5.0D, 0.01D, 100D);

            PLASMA_TO_ENERGY_CONVERTION = builder
                    .comment("Affects plasma energy to FE converion rate.")
                    .defineInRange("plasma_to_energy_convertion", 1D, 0.01D, 10D);

            builder.pop();
        }

    }
}