package igentuman.nc.handler.config;

import igentuman.nc.content.Electromagnets;
import igentuman.nc.content.RFAmplifier;
import igentuman.nc.content.energy.BatteryBlocks;
import igentuman.nc.content.energy.RTGs;
import igentuman.nc.content.energy.SolarPanels;
import igentuman.nc.content.fuel.FuelManager;
import igentuman.nc.content.storage.BarrelBlocks;
import igentuman.nc.multiblock.fission.FissionBlocks;
import igentuman.nc.multiblock.turbine.TurbineRegistration;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
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
        public ForgeConfigSpec.ConfigValue<Double> HEAT_MULTIPLIER;
        public ForgeConfigSpec.ConfigValue<Double> FUEL_HEAT_MULTIPLIER;
        public ForgeConfigSpec.ConfigValue<Double> DEPLETION_MULTIPLIER;

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
        public ForgeConfigSpec.ConfigValue<Integer> MIN_SIZE;
        public ForgeConfigSpec.ConfigValue<Integer> MAX_SIZE;
        public ForgeConfigSpec.ConfigValue<Double> HEAT_MULTIPLIER;
        public ForgeConfigSpec.ConfigValue<Double> HEAT_MULTIPLIER_CAP;
        public ForgeConfigSpec.ConfigValue<Double> MODERATOR_FE_MULTIPLIER;
        public ForgeConfigSpec.ConfigValue<Double> MODERATOR_HEAT_MULTIPLIER;
        public ForgeConfigSpec.ConfigValue<Double> EXPLOSION_RADIUS;
        public ForgeConfigSpec.ConfigValue<Double> HEAT_CAPACITY;

        public ForgeConfigSpec.ConfigValue<Double> FE_GENERATION_MULTIPLIER;
        public ForgeConfigSpec.ConfigValue<Double> BOILING_MULTIPLIER;

        public FissionReactorConfig(ForgeConfigSpec.Builder builder) {
            builder.comment("Settings for Fission Reactor").push("fission_reactor");

            MIN_SIZE = builder
                    .comment("Reactor min size.")
                    .defineInRange("min_size", 3, 3, 24);

            MAX_SIZE = builder
                    .comment("Reactor max size.")
                    .defineInRange("max_size", 24, 5, 24);

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
                    .defineInRange("moderator_fe_multiplier", 16.67D, 0D, 1000D);

            MODERATOR_HEAT_MULTIPLIER = builder
                    .comment("Each attachment of moderator to fuel cell will increase fuel heat generation by given percent value.")
                    .defineInRange("moderator_heat_multiplier", 33.34D, 0D, 1000D);

            BOILING_MULTIPLIER = builder
                    .comment("Rate at which steam recipes produced.")
                    .defineInRange("boiling_mult", 100D, 0.01D, 1000000D);

            FE_GENERATION_MULTIPLIER = builder
                    .comment("Affects how much energy reactors produce.")
                    .defineInRange("fe_generation_multiplier", 10D, 0.01D, 1000000D);

            builder.pop();
        }

    }

}