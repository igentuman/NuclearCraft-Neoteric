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
    public static final HeatSinkConfig HEAT_SINK_CONFIG = new HeatSinkConfig(BUILDER);
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
        public ForgeConfigSpec.ConfigValue<List<Double>> HEAT;
        public ForgeConfigSpec.ConfigValue<List<Integer>> EFFICIENCY;
        public ForgeConfigSpec.ConfigValue<List<Integer>> DEPLETION;
        public ForgeConfigSpec.ConfigValue<List<Integer>> CRITICALITY;
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
                    .defineInRange("depletion_multiplier", 100D, 0D, 1000D);

            HEAT = builder
                    .comment("Base Fuel Heat: " + String.join(", ",FuelManager.initialHeat().keySet()))
                    .define("base_heat", toList(FuelManager.initialHeat().values()), o -> o instanceof ArrayList);
            EFFICIENCY = builder
                    .comment("Base Fuel Efficiency: " + String.join(", ",FuelManager.initialEfficiency().keySet()))
                    .define("base_efficiency", toList(FuelManager.initialEfficiency().values()), o -> o instanceof ArrayList);
            DEPLETION = builder
                    .comment("Base Fuel Depletion Time (seconds): " + String.join(", ",FuelManager.initialDepletion().keySet()))
                    .define("base_depletion", toList(FuelManager.initialDepletion().values()), o -> o instanceof ArrayList);
            CRITICALITY = builder
                    .comment("Fuel Criticality: " + String.join(", ",FuelManager.initialCriticality().keySet()))
                    .define("base_criticallity", toList(FuelManager.initialCriticality().values()), o -> o instanceof ArrayList);
            builder.pop();
        }

    }

    public static class HeatSinkConfig {
        public ForgeConfigSpec.ConfigValue<List<Double>> HEAT;
        public HashMap<String, ForgeConfigSpec.ConfigValue<List<String>>> PLACEMENT_RULES = new HashMap<>();

        public HeatSinkConfig(ForgeConfigSpec.Builder builder) {
            builder.comment("Settings for heat sinks").push("heat_sink");

            HEAT = builder
                    .comment("Cooling rate H/t: " + String.join(", ", FissionBlocks.initialHeat().keySet()))
                    .define("cooling_rate", toList(FissionBlocks.initialHeat().values()), o -> o instanceof ArrayList);
            builder
                    .comment("You can define blocks by block_name. So water_heat_sink will fall back to nuclearcraft:water_heat_sink. Or qualify it with namespace like some_mod:some_block.")
                    .comment("Or use block tag key. #nuclearcraft:fission_reactor_casing will fall back to blocks with this tag. Do not forget to put #.")
                    .comment("if you need AND condition, add comma separated values \"block1\", \"block2\" means AND condition")
                    .comment("if you need OR condition, use | separator. \"block1|block2\" means block1 or block2")
                    .comment("By default you have rule condition is 'At least 1'. So if you define some block, it will go in the rule as 'at least 1'")
                    .comment("Validation options: >2 means at least 2 (use any number)")
                    .comment("-2 means between, it is always 2 (opposite sides)")
                    .comment("<2 means less than 2 (use any number)")
                    .comment("=2 means exact 2 (use any number)")
                    .comment("^3 means 3 blocks in the corner (shared vertex or edge). possible values 2 and 3")
                   .comment("Default placement rules have all examples")
                    .define("placement_explanations", "");

           for(String name: FissionBlocks.heatsinks().keySet()) {
                if(name.contains("empty")) continue;
                PLACEMENT_RULES.put(name, builder
                        .define(name, FissionBlocks.initialPlacementRules(name), o -> o instanceof ArrayList));
            }
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
                    .defineInRange("boiling_mult", 5D, 0.01D, 1000000D);

            builder.pop();
        }

    }

}