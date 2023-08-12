package igentuman.nc.handler.config;

import igentuman.nc.content.materials.*;
import igentuman.nc.setup.energy.SolarPanels;
import igentuman.nc.setup.multiblocks.FissionBlocks;
import igentuman.nc.content.processors.Processors;
import igentuman.nc.content.fuel.FuelManager;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import static igentuman.nc.world.dimension.Dimensions.WASTELAIND_ID;

public class CommonConfig {
    public static <T> List<T> toList(Collection<T> vals)
    {
        return new ArrayList<>(vals);
    }
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ProcessorConfig PROCESSOR_CONFIG = new ProcessorConfig(BUILDER);
    public static final OresConfig ORE_CONFIG = new OresConfig(BUILDER);
    public static final FuelConfig FUEL_CONFIG = new FuelConfig(BUILDER);
    public static final HeatSinkConfig HEAT_SINK_CONFIG = new HeatSinkConfig(BUILDER);

    public static final FissionConfig FISSION_CONFIG = new FissionConfig(BUILDER);
    public static final RadiationConfig RADIATION_CONFIG = new RadiationConfig(BUILDER);
    public static final EnergyGenerationConfig ENERGY_GENERATION = new EnergyGenerationConfig(BUILDER);
    public static final MaterialProductsConfig MATERIAL_PRODUCTS = new MaterialProductsConfig(BUILDER);
    public static final DimensionConfig DIMENSION_CONFIG = new DimensionConfig(BUILDER);
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

    public static class FuelConfig
    {
        public ForgeConfigSpec.ConfigValue<List<Double>> HEAT;
        public ForgeConfigSpec.ConfigValue<List<Integer>> EFFICIENCY;
        public ForgeConfigSpec.ConfigValue<List<Integer>> DEPLETION;
        public ForgeConfigSpec.ConfigValue<List<Integer>> CRITICALITY;
        public ForgeConfigSpec.ConfigValue<Double> HEAT_MULTIPLIER;
        public ForgeConfigSpec.ConfigValue<Double> DEPLETION_MULTIPLIER;

        public FuelConfig(ForgeConfigSpec.Builder builder) {
            builder.comment("Settings for reactor fuel").push("reactor_fuel");

            HEAT_MULTIPLIER = builder
                    .comment("Heat multiplier for boiling reactor.")
                    .define("heat_multiplier", 3.24444444);

            DEPLETION_MULTIPLIER = builder
                    .comment("Depletion multiplier. Affects how long fuel lasts.")
                    .defineInRange("depletion_multiplier", 100D, 0D, 1000D);

            HEAT = builder
                    .comment("Base Fuel Heat: " + String.join(", ",FuelManager.initialHeat().keySet()))
                    .define("base_heat", toList(FuelManager.initialHeat().values()));
            EFFICIENCY = builder
                    .comment("Base Fuel Efficiency: " + String.join(", ",FuelManager.initialEfficiency().keySet()))
                    .define("base_efficiency", toList(FuelManager.initialEfficiency().values()));
            DEPLETION = builder
                    .comment("Base Fuel Depletion Time (seconds): " + String.join(", ",FuelManager.initialDepletion().keySet()))
                    .define("base_depletion", toList(FuelManager.initialDepletion().values()));
            CRITICALITY = builder
                    .comment("Fuel Criticality: " + String.join(", ",FuelManager.initialCriticality().keySet()))
                    .define("base_criticallity", toList(FuelManager.initialCriticality().values()));
            builder.pop();
        }

    }

    public static class HeatSinkConfig
    {
        public static ForgeConfigSpec.ConfigValue<List<Double>> HEAT;
        public static HashMap<String, ForgeConfigSpec.ConfigValue<List<String>>> PLACEMENT_RULES = new HashMap<>();

        public HeatSinkConfig(ForgeConfigSpec.Builder builder) {
            builder.comment("Settings for heat sinks").push("heat_sink");

            HEAT = builder
                    .comment("Cooling rate H/t: " + String.join(", ", FissionBlocks.initialHeat().keySet()))
                    .define("cooling_rate", toList(FissionBlocks.initialHeat().values()));
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
                PLACEMENT_RULES.put(name,
                        builder
                                .define(name+"_heat_sink_placement_rule", FissionBlocks.initialPlacementRules(name))
                        );
            }


            builder.pop();
        }

    }

    public static class FissionConfig
    {
        public static ForgeConfigSpec.ConfigValue<Integer> MIN_SIZE;
        public static ForgeConfigSpec.ConfigValue<Integer> MAX_SIZE;
        public static ForgeConfigSpec.ConfigValue<Double> HEAT_MULTIPLIER;
        public static ForgeConfigSpec.ConfigValue<Double> HEAT_MULTIPLIER_CAP;
        public static ForgeConfigSpec.ConfigValue<Double> MODERATOR_FE_MULTIPLIER;
        public static ForgeConfigSpec.ConfigValue<Double> MODERATOR_HEAT_MULTIPLIER;
        public static ForgeConfigSpec.ConfigValue<Double> EXPLOSION_RADIUS;

        public FissionConfig(ForgeConfigSpec.Builder builder) {
            builder.comment("Settings for Fission Reactor").push("fission_reactor");

            MIN_SIZE = builder
                    .comment("Explosion size if reactor overheats. 4 - TNT size. Set to 0 to disable explosion.")
                    .defineInRange("reactor_explosion_radius", 3, 3, 24);

            MAX_SIZE = builder
                    .comment("Explosion size if reactor overheats. 4 - TNT size. Set to 0 to disable explosion.")
                    .defineInRange("reactor_explosion_radius", 24, 5, 24);

            EXPLOSION_RADIUS = builder
                    .comment("Explosion size if reactor overheats. 4 - TNT size. Set to 0 to disable explosion.")
                    .defineInRange("reactor_explosion_radius", 4f, 0.0f, 20f);

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

            builder.pop();
        }

    }

    public static class RadiationConfig
    {
        public ForgeConfigSpec.ConfigValue<Boolean> ENABLED;
        public ForgeConfigSpec.ConfigValue<Integer> SPREAD_GATE;
        public ForgeConfigSpec.ConfigValue<Double> SPREAD_MULTIPLIER;
        public ForgeConfigSpec.ConfigValue<Integer> DECAY_SPEED;
        public ForgeConfigSpec.ConfigValue<Integer> DECAY_SPEED_FOR_PLAYER;
        public ForgeConfigSpec.ConfigValue<List<String>> BIOME_RADIATION;
        public ForgeConfigSpec.ConfigValue<List<String>> DIMENSION_RADIATION;

        public ForgeConfigSpec.ConfigValue<Integer> RADIATION_UPDATE_INTERVAL;

        public RadiationConfig(ForgeConfigSpec.Builder builder) {
            builder.comment("Settings for Radiation").push("radiation");

            ENABLED = builder
                    .comment("If radiation is enabled.")
                    .define("enabled", true);

            SPREAD_MULTIPLIER = builder
                    .comment("Spread multiplier. How much radiation spreads from chunk to chunk. Bigger values might cause lag.")
                    .defineInRange("spread_multiplier", 0.3d, 0.01d, 0.9d);

            SPREAD_GATE = builder
                    .comment("If chunk radiation (uRad) less than this value it won't affect chunks nearby.", "Bigger values - less lag, but less accurate radiation spread.")
                    .defineInRange("spread_gate", 5000, 100, 100000);

            DECAY_SPEED = builder
                    .comment("How fast contamination decays (uRad/s).")
                    .defineInRange("decay_speed", 5, 1, 20);

            DECAY_SPEED_FOR_PLAYER = builder
                    .comment("How fast contamination decays in player's body (uRad/s).")
                    .defineInRange("decay_speed_for_player", 3, 1, 20);

            BIOME_RADIATION = builder
                    .comment("Natural radiation per biome: uRad", "Format: biome_id: radiation")
                    .define("biome_radiation", List.of("wasteland: 2000", "nether_wastes: 500"));

            DIMENSION_RADIATION = builder
                    .comment("Natural radiation per dimension: uRad", "Format: dim_id: radiation")
                    .define("dimension_radiation", List.of("-4848: 2000", "-1: 100"));

            RADIATION_UPDATE_INTERVAL = builder
                    .comment("Interval between radiation updates in ticks. 20 ticks = 1 second.", "Bigger interval - less lag, but less accurate radiation spread.")
                    .defineInRange("update_interval", 100, 20, 1000);

            builder.pop();
        }
    }

    public static class MaterialProductsConfig {
        public ForgeConfigSpec.ConfigValue<List<Boolean>> INGOTS;
        public ForgeConfigSpec.ConfigValue<List<Boolean>> NUGGET;
        public ForgeConfigSpec.ConfigValue<List<Boolean>> BLOCK;
        public ForgeConfigSpec.ConfigValue<List<Boolean>> CHUNKS;
        public ForgeConfigSpec.ConfigValue<List<Boolean>> PLATES;
        public ForgeConfigSpec.ConfigValue<List<Boolean>> DUSTS;
        public ForgeConfigSpec.ConfigValue<List<Boolean>> GEMS;

        public ForgeConfigSpec.ConfigValue<List<String>> MODS_PRIORITY;

        public MaterialProductsConfig(ForgeConfigSpec.Builder builder) {
            builder.comment("Settings for items registration").push("material_products");

            CHUNKS = builder
                    .comment("Enable chunk registration: " + String.join(", ", Chunks.all().keySet()))
                    .define("register_chunk", Chunks.initialRegistration());

            INGOTS = builder
                    .comment("Enable ingots registration: " + String.join(", ", Ingots.all().keySet()))
                    .define("register_ingot", Ingots.initialRegistration());

            PLATES = builder
                    .comment("Enable plate registration: " + String.join(", ", Plates.all().keySet()))
                    .define("register_plate", Plates.initialRegistration());

            DUSTS = builder
                    .comment("Enable dust registration: " + String.join(", ", Dusts.all().keySet()))
                    .define("register_dust", Dusts.initialRegistration());

            NUGGET = builder
                    .comment("Enable nuggets registration: " + String.join(", ", Nuggets.all().keySet()))
                    .define("register_nugget", Nuggets.initialRegistration());

            BLOCK = builder
                    .comment("Enable blocks registration: " + String.join(", ", Blocks.all().keySet()))
                    .define("register_block", Blocks.initialRegistration());

            GEMS = builder
                    .comment("Enable gems registration: " + String.join(", ", Gems.all().keySet()))
                    .define("register_block", Gems.initialRegistration());

            builder.pop();

            builder.comment("Forge Tag priority").push("forge_tag_priority");

            MODS_PRIORITY = builder
                    .comment("Priority of mods to resolve forge tags to itemstack.")
                    .define("mods_priority", List.of("nuclearcraft", "mekanism", "immersiveengineering", "tconstruct"));

            builder.pop();

        }
    }

    public static class OresConfig {

        public static ForgeConfigSpec.ConfigValue<List<Integer>> ORE_AMOUNT;
        public static ForgeConfigSpec.ConfigValue<List<Integer>> ORE_VEIN_SIZE;
        public static ForgeConfigSpec.ConfigValue<List<List<Integer>>> ORE_DIMENSIONS;
        public static ForgeConfigSpec.ConfigValue<List<Integer>> ORE_MIN_HEIGHT;
        public static ForgeConfigSpec.ConfigValue<List<Integer>> ORE_MAX_HEIGHT;
        public static ForgeConfigSpec.ConfigValue<List<Boolean>> REGISTER_ORE;

        public OresConfig(ForgeConfigSpec.Builder builder) {
            builder.comment("Settings for ore generation").push("ores");

            ORE_DIMENSIONS = builder
                    .comment("List of dimensions to generate ores: " + String.join(", ", Ores.all().keySet()))
                    .define("dimensions", Ores.initialOreDimensions());

            REGISTER_ORE = builder
                    .comment("Enable ore registration: " + String.join(", ", Ores.all().keySet()))
                    .define("register_ore", Ores.initialOreRegistration());

            ORE_VEIN_SIZE = builder
                    .comment("Ore blocks per vein. Order: " + String.join(", ", Ores.all().keySet()))
                    .define("vein_size", Ores.initialOreVeinSizes());

            ORE_AMOUNT = builder
                    .comment("Veins in chunk. Order: " + String.join(", ", Ores.all().keySet()))
                    .define("veins_in_chunk", Ores.initialOreVeinsAmount());

            ORE_MIN_HEIGHT = builder
                    .comment("Minimal generation height. Order: " + String.join(", ", Ores.all().keySet()))
                    .define("min_height", Ores.initialOreMinHeight());

            ORE_MAX_HEIGHT = builder
                    .comment("Max generation height. Order: " + String.join(", ", Ores.all().keySet()))
                    .define("max_height", Ores.initialOreMaxHeight());

            builder.pop();
        }
    }

    public static class DimensionConfig {
        public final ForgeConfigSpec.ConfigValue<Boolean> registerWasteland;
        public final ForgeConfigSpec.ConfigValue<Integer> wastelandID;

        public DimensionConfig(ForgeConfigSpec.Builder builder) {
            builder.push("Dimension");
            registerWasteland = builder
                    .comment("Register Wasteland Dimension")
                    .define("wasteland", true);
            wastelandID = builder
                    .comment("Dimension ID for Wasteland")
                    .define("wastelandID", WASTELAIND_ID);
            builder.pop();
        }
    }

    public static class ProcessorConfig {
        public static ForgeConfigSpec.ConfigValue<Integer> base_time;
        public static ForgeConfigSpec.ConfigValue<Integer> base_power;
        public static ForgeConfigSpec.ConfigValue<List<Boolean>> REGISTER_PROCESSOR;
        public static ForgeConfigSpec.ConfigValue<List<Integer>> PROCESSOR_POWER;
        public static ForgeConfigSpec.ConfigValue<List<Integer>> PROCESSOR_TIME;


        public ProcessorConfig(ForgeConfigSpec.Builder builder) {
            builder.push("Processor");
            base_time = builder
                    .comment("Ticks")
                    .define("base_time", 240);
            base_power = builder
                    .comment("FE per Tick")
                    .define("base_power", 100);

            REGISTER_PROCESSOR = builder
                    .comment("Allow processor registration: " + String.join(", ", Processors.all().keySet()))
                    .define("register_processor", Processors.initialRegistered());

            PROCESSOR_POWER = builder
                    .comment("Processor power: " + String.join(", ", Processors.all().keySet()))
                    .define("processor_power", Processors.initialPower());

            PROCESSOR_TIME = builder
                    .comment("Time for processor to proceed recipe: " + String.join(", ", Processors.all().keySet()))
                    .define("processor_time", Processors.initialTime());
            builder.pop();


        }
    }

    public static class EnergyGenerationConfig {
            public static ForgeConfigSpec.ConfigValue<List<Boolean>> REGISTER_SOLAR_PANELS;
            public static ForgeConfigSpec.ConfigValue<List<Integer>> SOLAR_PANELS_GENERATION;
            public static ForgeConfigSpec.ConfigValue<Integer> STEAM_TURBINE;


            public EnergyGenerationConfig(ForgeConfigSpec.Builder builder) {
                builder.push("energy_generation");

                REGISTER_SOLAR_PANELS = builder
                        .comment("Allow processor registration: " + String.join(", ", SolarPanels.all().keySet()))
                        .define("register_processor", SolarPanels.initialRegistered());

                SOLAR_PANELS_GENERATION = builder
                        .comment("Processor power: " + String.join(", ", SolarPanels.all().keySet()))
                        .define("processor_power", SolarPanels.initialPower());
                STEAM_TURBINE = builder
                        .comment("Steam turbine (one block) base power gen")
                        .define("steam_turbine_power_gen", 50);

                builder.pop();

            }
        }

}