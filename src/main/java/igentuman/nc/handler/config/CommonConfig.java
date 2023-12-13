package igentuman.nc.handler.config;

import igentuman.nc.content.Electromagnets;
import igentuman.nc.content.RFAmplifier;
import igentuman.nc.content.materials.*;
import igentuman.nc.content.energy.BatteryBlocks;
import igentuman.nc.content.energy.RTGs;
import igentuman.nc.content.energy.SolarPanels;
import igentuman.nc.multiblock.fission.FissionBlocks;
import igentuman.nc.content.processors.Processors;
import igentuman.nc.content.fuel.FuelManager;
import igentuman.nc.content.storage.BarrelBlocks;
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
    public static final FusionConfig FUSION_CONFIG = new FusionConfig(BUILDER);
    public static final FissionConfig FISSION_CONFIG = new FissionConfig(BUILDER);
    public static final TurbineConfig TURBINE_CONFIG = new TurbineConfig(BUILDER);
    public static final RadiationConfig RADIATION_CONFIG = new RadiationConfig(BUILDER);
    public static final EnergyGenerationConfig ENERGY_GENERATION = new EnergyGenerationConfig(BUILDER);
    public static final ElectromagnetsConfig ELECTROMAGNETS_CONFIG = new ElectromagnetsConfig(BUILDER);
    public static final RFAmplifierConfig RF_AMPLIFIERS_CONFIG = new RFAmplifierConfig(BUILDER);
    public static final EnergyStorageConfig ENERGY_STORAGE = new EnergyStorageConfig(BUILDER);
    public static final StorageBlocksConfig STORAGE_BLOCKS = new StorageBlocksConfig(BUILDER);
    public static final MaterialProductsConfig MATERIAL_PRODUCTS = new MaterialProductsConfig(BUILDER);
    public static final InSituLeachingConfig IN_SITU_LEACHING = new InSituLeachingConfig(BUILDER);
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

    public static class FuelConfig {
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

    public static class InSituLeachingConfig {
        public ForgeConfigSpec.ConfigValue<Boolean> ENABLE_VEINS;
        public ForgeConfigSpec.ConfigValue<List<Integer>> VEIN_BLOCKS_AMOUNT;
        public ForgeConfigSpec.ConfigValue<Integer> VEINS_RARITY;
        public ForgeConfigSpec.ConfigValue<Boolean> RANDOMIZED_ORES;
        public ForgeConfigSpec.ConfigValue<Boolean> ADD_IE_VEINS;
        public ForgeConfigSpec.ConfigValue<Boolean> ALLOW_TO_LEACH_IE_VEINS;


        public InSituLeachingConfig(ForgeConfigSpec.Builder builder) {
            builder.comment("Settings for In situ leaching").push("in_situ_leaching");

            ENABLE_VEINS = builder
                    .comment("Enable veins generation.")
                    .define("enable_veins", true);

            VEIN_BLOCKS_AMOUNT = builder
                    .comment("Min and max values of blocks per vein.")
                    .comment("Result amount will be random value in this range.")
                    .define("blocks_per_vein", List.of(30000, 70000), o -> o instanceof ArrayList);

            VEINS_RARITY = builder
                    .comment("Veins rarity. Bigger value - less veins.")
                    .defineInRange("veins_rarity", 100, 1, 5000);

            RANDOMIZED_ORES = builder
                    .comment("All veins will have random ores. It will ignore vein settings")
                    .define("randomized_ores", false);

            ADD_IE_VEINS = builder
                    .comment("Add new veins to generation for Immersive Engineering.")
                    .define("add_ie_veins", true);

            ALLOW_TO_LEACH_IE_VEINS = builder
                    .comment("Allow to leach veins from Immersive Engineering.")
                    .comment("To do so, you need to put IE core sample into leacher.")
                    .define("allow_to_leach_ie_veins", true);

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

    public static class FusionConfig {
        public ForgeConfigSpec.ConfigValue<Integer> MIN_SIZE;
        public ForgeConfigSpec.ConfigValue<Integer> MAX_SIZE;
        public ForgeConfigSpec.ConfigValue<Double> MINIMAL_MAGNETIC_FIELD;
        public ForgeConfigSpec.ConfigValue<Double> RF_AMPLIFICATION_MULTIPLIER;
        public ForgeConfigSpec.ConfigValue<Double> PLASMA_TO_ENERGY_CONVERTION;
        public ForgeConfigSpec.ConfigValue<Double> EXPLOSION_RADIUS;

        public FusionConfig(ForgeConfigSpec.Builder builder) {
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
                    .defineInRange("minimal_magnetic_field", 10, 1D, 100D);

            RF_AMPLIFICATION_MULTIPLIER = builder
                    .comment("Affects heating rate for plasma by rf amplifiers.")
                    .defineInRange("rf_amplification_multiplier", 4.0D, 0.01D, 100D);

            PLASMA_TO_ENERGY_CONVERTION = builder
                    .comment("Affects plasma energy to FE converion rate.")
                    .defineInRange("plasma_to_energy_convertion", 1D, 0.01D, 10D);

            builder.pop();
        }

    }

    public static class TurbineConfig {
        public ForgeConfigSpec.ConfigValue<Integer> MIN_SIZE;
        public ForgeConfigSpec.ConfigValue<Integer> MAX_SIZE;

        public TurbineConfig(ForgeConfigSpec.Builder builder) {
            builder.comment("Settings for Turbine").push("turbine");

            MIN_SIZE = builder
                    .comment("Explosion size if reactor overheats. 4 - TNT size. Set to 0 to disable explosion.")
                    .defineInRange("min_size", 3, 3, 24);

            MAX_SIZE = builder
                    .comment("Explosion size if reactor overheats. 4 - TNT size. Set to 0 to disable explosion.")
                    .defineInRange("max_size", 24, 5, 24);



            builder.pop();
        }

    }

    public static class FissionConfig {
        public ForgeConfigSpec.ConfigValue<Integer> MIN_SIZE;
        public ForgeConfigSpec.ConfigValue<Integer> MAX_SIZE;
        public ForgeConfigSpec.ConfigValue<Double> HEAT_MULTIPLIER;
        public ForgeConfigSpec.ConfigValue<Double> HEAT_MULTIPLIER_CAP;
        public ForgeConfigSpec.ConfigValue<Double> MODERATOR_FE_MULTIPLIER;
        public ForgeConfigSpec.ConfigValue<Double> MODERATOR_HEAT_MULTIPLIER;
        public ForgeConfigSpec.ConfigValue<Double> EXPLOSION_RADIUS;
        public ForgeConfigSpec.ConfigValue<Double> HEAT_CAPACITY;

        public FissionConfig(ForgeConfigSpec.Builder builder) {
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

            builder.pop();
        }

    }


    public static class RadiationConfig {
        public ForgeConfigSpec.ConfigValue<Boolean> ENABLED;
        public ForgeConfigSpec.ConfigValue<Integer> SPREAD_GATE;
        public ForgeConfigSpec.ConfigValue<Integer> NATURAL_RADIATION;
        public ForgeConfigSpec.ConfigValue<Double> SPREAD_MULTIPLIER;
        public ForgeConfigSpec.ConfigValue<Integer> DECAY_SPEED;
        public ForgeConfigSpec.ConfigValue<Integer> DECAY_SPEED_FOR_PLAYER;
        public ForgeConfigSpec.ConfigValue<Double> GAIN_SPEED_FOR_PLAYER;
        public ForgeConfigSpec.ConfigValue<List<String>> ITEM_RADIATION;
        public ForgeConfigSpec.ConfigValue<List<String>> RADIATION_REMOVAL_ITEMS;
        public ForgeConfigSpec.ConfigValue<List<String>> ARMOR_PROTECTION;
        public ForgeConfigSpec.ConfigValue<List<String>> BIOME_RADIATION;
        public ForgeConfigSpec.ConfigValue<List<String>> DIMENSION_RADIATION;
        public ForgeConfigSpec.ConfigValue<Integer> RADIATION_UPDATE_INTERVAL;
        public ForgeConfigSpec.ConfigValue<Boolean> MEKANISM_RADIATION_INTEGRATION;
        protected HashMap<String, Integer> biomeRadiationMap;
        public int biomeRadiation(String id)
        {
            if(biomeRadiationMap == null) {
                biomeRadiationMap = new HashMap<>();
                for(String line: BIOME_RADIATION.get()) {
                    String[] split = line.split("\\|");
                    if(split.length != 2) {
                        continue;
                    }
                    biomeRadiationMap.put(split[0].trim(), Integer.parseInt(split[1].trim()));
                }
            }
            if(biomeRadiationMap.containsKey(id)) {
                return biomeRadiationMap.get(id);
            }
            return 0;
        }

        protected HashMap<String, Integer> dimensionRadiationMap;
        public int dimensionRadiation(String id)
        {
            if(dimensionRadiationMap == null) {
                dimensionRadiationMap = new HashMap<>();
                for(String line: DIMENSION_RADIATION.get()) {
                    String[] split = line.split("\\|");
                    if(split.length != 2) {
                        continue;
                    }
                    dimensionRadiationMap.put(split[0].trim(), Integer.parseInt(split[1].trim()));
                }
            }
            if(dimensionRadiationMap.containsKey(id)) {
                return dimensionRadiationMap.get(id);
            }
            return 0;
        }

        public RadiationConfig(ForgeConfigSpec.Builder builder) {
            builder.comment("Settings for Radiation").push("radiation");

            ENABLED = builder
                    .comment("If radiation is enabled.")
                    .define("enabled", true);

            NATURAL_RADIATION = builder
                    .comment("General background radiation everywhere (pRad).","Total radiation = background_radiation + dimension_radiation + chunk_radiation + in-game exposure")
                    .defineInRange("background_radiation", 50, 0, 10000);

            SPREAD_MULTIPLIER = builder
                    .comment("Spread multiplier. How much radiation spreads from chunk to chunk per simulation. Bigger values might cause lag.")
                    .defineInRange("spread_multiplier", 0.3d, 0.01d, 0.9d);

            SPREAD_GATE = builder
                    .comment("If chunk radiation (uRad) less than this value it won't affect chunks nearby.", "Bigger values - less lag, but less accurate radiation spread.")
                    .defineInRange("spread_gate", 1000, 100, 100000);

            DECAY_SPEED = builder
                    .comment("How fast contamination decays (pRad/s).")
                    .defineInRange("decay_speed", 1500, 1000, 10000);

            DECAY_SPEED_FOR_PLAYER = builder
                    .comment("How fast contamination decays in player's body (uRad/s).")
                    .defineInRange("decay_speed_for_player", 50, 1, 5000);

            GAIN_SPEED_FOR_PLAYER = builder
                    .comment("Rate at which player gets radiation dose.")
                    .defineInRange("gain_speed_for_player", 0.015D, 0, 5D);

            RADIATION_REMOVAL_ITEMS = builder
                    .comment("List of items what cleans player radiation when used (pRad). Format: item_id|radiation")
                    .define("radiation_removal_items", List.of(
                            "minecraft:golden_carrot|500000",
                            "minecraft:golden_apple|20000000",
                            "minecraft:enchanted_golden_apple|100000000",
                            "nuclearcraft:dominos|50000000",
                            "nuclearcraft:moresmore|100000000",
                            "nuclearcraft:evenmoresmore|200000000",
                            "nuclearcraft:radaway|300000000"
                    ), o -> o instanceof ArrayList);

            ITEM_RADIATION = builder
                    .comment("List of items what have radiation (pRad). Format: item_id|radiation")
                    .define("items_radiation", List.of(
                            "nuclearcraft:spaxelhoe_thorium|50000",
                            "mekanism:pellet_polonium|4000000",
                            "mekanism:pellet_plutonium|2500000",
                            "mekanism:reprocessed_fissile_fragment|1800000"
                    ), o -> o instanceof ArrayList);

            ARMOR_PROTECTION = builder
                    .comment("List of armor items and default shielding lvl. Format: item_id|radiation")
                    .define("armor_shielding", List.of(
                            "mekanism:hazmat_mask|3",
                            "mekanism:hazmat_gown|5",
                            "mekanism:hazmat_pants|4",
                            "mekanism:hazmat_boots|3",
                            "mekanism:mekasuit_helmet|5",
                            "mekanism:mekasuit_bodyarmor|5",
                            "mekanism:mekasuit_pants|5",
                            "mekanism:mekasuit_boots|5",
                            "nuclearcraft:hazmat_mask|3",
                            "nuclearcraft:hazmat_chest|5",
                            "nuclearcraft:hazmat_pants|4",
                            "nuclearcraft:hazmat_boots|3",
                            "nuclearcraft:hev_helmet|5",
                            "nuclearcraft:hev_chest|7",
                            "nuclearcraft:hev_pants|6",
                            "nuclearcraft:hev_boots|5"
                    ), o -> o instanceof ArrayList);

            BIOME_RADIATION = builder
                    .comment("Natural radiation per biome: uRad", "Format: biome_id|radiation")
                    .define("biome_radiation", List.of("nuclearcraft:wasteland|2000", "minecraft:nether_wastes|500"), o -> o instanceof ArrayList);

            DIMENSION_RADIATION = builder
                    .comment("Natural radiation per dimension: uRad", "Format: dim_id|radiation")
                    .define("dimension_radiation", List.of("nuclearcraft:wasteland|200000", "minecraft:the_nether|1000"), o -> o instanceof ArrayList);

            RADIATION_UPDATE_INTERVAL = builder
                    .comment("Interval between radiation updates in ticks. 20 ticks = 1 second.", "Bigger interval - less lag, but less accurate radiation spread.")
                    .defineInRange("update_interval", 40, 2, 1000);

            MEKANISM_RADIATION_INTEGRATION = builder
                    .comment(
                            "NC radiation sources will generate mekanism radiation and wise-versa.",
                            "You can disable mekanism radiation, but radiation sources in mekanism still will generate NC radiation.",
                            "You can disable NC radiation, but NC radiation sources still will generate mekanism radiation.")
                    .define("mekanism_radiation_integration", true);

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
        public ForgeConfigSpec.ConfigValue<List<String>> SLURRIES;

        public ForgeConfigSpec.ConfigValue<List<String>> MODS_PRIORITY;

        public MaterialProductsConfig(ForgeConfigSpec.Builder builder) {
            builder.comment("Settings for items registration").push("material_products");

            SLURRIES = builder
                    .comment("List of available slurries (dissolved ores in acid)")
                    .comment("Color for slurry will be calculate from average texture color")
                    .comment("Texture location has to be: nuclearcraft:textures/block/ore/(slurry_name)_ore.png")
                    .comment("If no texture found it will generate random color")
                    .define("register_slurries", List.of(
                            "uranium", "iron", "gold", "aluminum", "thorium", "boron", "silver",
                            "lead", "tin", "copper", "zinc", "cobalt", "platinum", "lithium", "magnesium"
                    ), o -> o instanceof ArrayList);

            CHUNKS = builder
                    .comment("Enable chunk registration: " + String.join(", ", Chunks.all().keySet()))
                    .define("register_chunk", Chunks.initialRegistration(), o -> o instanceof ArrayList);

            INGOTS = builder
                    .comment("Enable ingots registration: " + String.join(", ", Ingots.all().keySet()))
                    .define("register_ingot", Ingots.initialRegistration(), o -> o instanceof ArrayList);

            PLATES = builder
                    .comment("Enable plate registration: " + String.join(", ", Plates.all().keySet()))
                    .define("register_plate", Plates.initialRegistration(), o -> o instanceof ArrayList);

            DUSTS = builder
                    .comment("Enable dust registration: " + String.join(", ", Dusts.all().keySet()))
                    .define("register_dust", Dusts.initialRegistration(), o -> o instanceof ArrayList);

            NUGGET = builder
                    .comment("Enable nuggets registration: " + String.join(", ", Nuggets.all().keySet()))
                    .define("register_nugget", Nuggets.initialRegistration(), o -> o instanceof ArrayList);

            BLOCK = builder
                    .comment("Enable blocks registration: " + String.join(", ", Blocks.all().keySet()))
                    .define("register_block", Blocks.initialRegistration(), o -> o instanceof ArrayList);

            GEMS = builder
                    .comment("Enable gems registration: " + String.join(", ", Gems.all().keySet()))
                    .define("register_block", Gems.initialRegistration(), o -> o instanceof ArrayList);

            builder.pop();

            builder.comment("Forge Tag priority").push("forge_tag_priority");

            MODS_PRIORITY = builder
                    .comment("Priority of mods to resolve forge tags to itemstack.")
                    .define("mods_priority", List.of("nuclearcraft", "mekanism", "immersiveengineering", "tconstruct"), o -> o instanceof ArrayList);

            builder.pop();

        }
    }

    public static class OresConfig {

        public ForgeConfigSpec.ConfigValue<List<Integer>> ORE_AMOUNT;
        public ForgeConfigSpec.ConfigValue<List<Integer>> ORE_VEIN_SIZE;
        public ForgeConfigSpec.ConfigValue<List<List<Integer>>> ORE_DIMENSIONS;
        public ForgeConfigSpec.ConfigValue<List<Integer>> ORE_MIN_HEIGHT;
        public ForgeConfigSpec.ConfigValue<List<Integer>> ORE_MAX_HEIGHT;
        public ForgeConfigSpec.ConfigValue<List<Boolean>> REGISTER_ORE;

        public OresConfig(ForgeConfigSpec.Builder builder) {
            builder.comment("Settings for ore generation").push("ores");

            ORE_DIMENSIONS = builder
                    .comment("List of dimensions to generate ores: " + String.join(", ", Ores.all().keySet()))
                    .define("dimensions", Ores.initialOreDimensions(), o -> o instanceof ArrayList);

            REGISTER_ORE = builder
                    .comment("Enable ore registration: " + String.join(", ", Ores.all().keySet()))
                    .define("register_ore", Ores.initialOreRegistration(), o -> o instanceof ArrayList);

            ORE_VEIN_SIZE = builder
                    .comment("Ore blocks per vein. Order: " + String.join(", ", Ores.all().keySet()))
                    .define("vein_size", Ores.initialOreVeinSizes(), o -> o instanceof ArrayList);

            ORE_AMOUNT = builder
                    .comment("Veins in chunk. Order: " + String.join(", ", Ores.all().keySet()))
                    .define("veins_in_chunk", Ores.initialOreVeinsAmount(), o -> o instanceof ArrayList);

            ORE_MIN_HEIGHT = builder
                    .comment("Minimal generation height. Order: " + String.join(", ", Ores.all().keySet()))
                    .define("min_height", Ores.initialOreMinHeight(), o -> o instanceof ArrayList);

            ORE_MAX_HEIGHT = builder
                    .comment("Max generation height. Order: " + String.join(", ", Ores.all().keySet()))
                    .define("max_height", Ores.initialOreMaxHeight(), o -> o instanceof ArrayList);

            builder.pop();
        }
    }

    public static class DimensionConfig {
        public ForgeConfigSpec.ConfigValue<Boolean> registerWasteland;
        public ForgeConfigSpec.ConfigValue<Integer> wastelandID;

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
        public ForgeConfigSpec.ConfigValue<Integer> BASE_TIME;
        public ForgeConfigSpec.ConfigValue<Integer> BASE_POWER;
        public ForgeConfigSpec.ConfigValue<Integer> SKIP_TICKS;
        public ForgeConfigSpec.ConfigValue<List<Boolean>> REGISTER_PROCESSOR;
        public ForgeConfigSpec.ConfigValue<List<Integer>> PROCESSOR_POWER;
        public ForgeConfigSpec.ConfigValue<List<Integer>> PROCESSOR_TIME;


        public ProcessorConfig(ForgeConfigSpec.Builder builder) {
            builder.push("Processor");
            BASE_TIME = builder
                    .comment("Ticks")
                    .define("base_time", 240);

            BASE_POWER = builder
                    .comment("FE per Tick")
                    .define("base_power", 100);

            SKIP_TICKS = builder
                    .comment("Generally used for server optimization. Processors will skip defined amount of ticks then and do nothing.")
                    .comment("This won't affect recipe production performance")
                    .comment("Let's say it will skip 2 ticks, and then it will multiply recipe progress by amount if skipped ticks.")
                    .comment("So it won't do the job each tick. But production will be the same as if it was done each tick.")
                    .comment("This only works if processor has recipe in work")
                    .comment("May lead to unknown issues, Please test first")
                    .defineInRange("skip_ticks", 0, 0, 10);

            REGISTER_PROCESSOR = builder
                    .comment("Allow processor registration: " + String.join(", ", Processors.all().keySet()))
                    .define("register_processor", Processors.initialRegistered(), o -> o instanceof ArrayList);

            PROCESSOR_POWER = builder
                    .comment("Processor power: " + String.join(", ", Processors.all().keySet()))
                    .define("processor_power", Processors.initialPower(), o -> o instanceof ArrayList);

            PROCESSOR_TIME = builder
                    .comment("Time for processor to proceed recipe: " + String.join(", ", Processors.all().keySet()))
                    .define("processor_time", Processors.initialTime(), o -> o instanceof ArrayList);
            builder.pop();


        }
    }

    public static class EnergyGenerationConfig {
        public ForgeConfigSpec.ConfigValue<List<Boolean>> REGISTER_SOLAR_PANELS;
        public ForgeConfigSpec.ConfigValue<List<Integer>> SOLAR_PANELS_GENERATION;
        public ForgeConfigSpec.ConfigValue<List<Boolean>> REGISTER_RTG;
        public ForgeConfigSpec.ConfigValue<List<Integer>> RTG_GENERATION;
        public ForgeConfigSpec.ConfigValue<List<Integer>> RTG_RADIATION;
        public ForgeConfigSpec.ConfigValue<Integer> STEAM_TURBINE;


        public EnergyGenerationConfig(ForgeConfigSpec.Builder builder) {
            builder.push("energy_generation");

            REGISTER_SOLAR_PANELS = builder
                    .comment("Allow panel registration: " + String.join(", ", SolarPanels.all().keySet()))
                    .define("register_panel", SolarPanels.initialRegistered(), o -> o instanceof ArrayList);

            SOLAR_PANELS_GENERATION = builder
                    .comment("Panel power generation: " + String.join(", ", SolarPanels.all().keySet()))
                    .define("panel_power", SolarPanels.initialPower(), o -> o instanceof ArrayList);

            REGISTER_RTG = builder
                    .comment("Allow rtg registration: " + String.join(", ", RTGs.all().keySet()))
                    .define("register_panel", RTGs.initialRegistered(), o -> o instanceof ArrayList);

            RTG_GENERATION = builder
                    .comment("rtg generation: " + String.join(", ", RTGs.all().keySet()))
                    .define("rtg_power", RTGs.initialPower(), o -> o instanceof ArrayList);

            RTG_RADIATION = builder
                    .comment("rtg radiation: " + String.join(", ", RTGs.all().keySet()))
                    .define("rtg_radiation", RTGs.initialRadiation(), o -> o instanceof ArrayList);

            STEAM_TURBINE = builder
                    .comment("Steam turbine (one block) base power gen")
                    .define("steam_turbine_power_gen", 50);

            builder.pop();
        }
    }

    public static class StorageBlocksConfig {
        public ForgeConfigSpec.ConfigValue<List<Boolean>> REGISTER_BARREL;
        public ForgeConfigSpec.ConfigValue<List<Integer>> BARREL_CAPACITY;

        public StorageBlocksConfig(ForgeConfigSpec.Builder builder) {
            builder.push("storage_blocks")
                    .comment("Blocks to store items, fluids, etc...");

            REGISTER_BARREL = builder
                    .comment("Allow barrel registration: " + String.join(", ", BarrelBlocks.all().keySet()))
                    .define("energy_block_registration", BarrelBlocks.initialRegistered(), o -> o instanceof ArrayList);

            BARREL_CAPACITY = builder
                    .comment("Barrel capacity in Buckets: " + String.join(", ", BarrelBlocks.all().keySet()))
                    .define("barrel_capacity", BarrelBlocks.initialCapacity(), o -> o instanceof ArrayList);


            builder.pop();
        }

        public int getLiquidCapacityFor(String code) {
            return BatteryBlocks.all().get(code).config().getStorage();
        }
    }

    public static class EnergyStorageConfig {
        public ForgeConfigSpec.ConfigValue<List<Boolean>> REGISTER_ENERGY_BLOCK;
        public ForgeConfigSpec.ConfigValue<List<Integer>> ENERGY_BLOCK_STORAGE;
        public ForgeConfigSpec.ConfigValue<Integer> LITHIUM_ION_BATTERY_STORAGE;
        public ForgeConfigSpec.ConfigValue<Integer> QNP_ENERGY_STORAGE;
        public ForgeConfigSpec.ConfigValue<Integer> QNP_ENERGY_PER_BLOCK;

        public EnergyStorageConfig(ForgeConfigSpec.Builder builder) {
            builder.push("energy_storage");

            REGISTER_ENERGY_BLOCK = builder
                    .comment("Allow block registration: " + String.join(", ", BatteryBlocks.all().keySet()))
                    .define("energy_block_registration", BatteryBlocks.initialRegistered(), o -> o instanceof ArrayList);

            ENERGY_BLOCK_STORAGE = builder
                    .comment("Storage: " + String.join(", ", BatteryBlocks.all().keySet()))
                    .define("energy_block_storage", BatteryBlocks.initialPower(), o -> o instanceof ArrayList);

            LITHIUM_ION_BATTERY_STORAGE = builder
                    .define("lithium_ion_battery_storage", 1000000);

            QNP_ENERGY_STORAGE = builder
                    .define("qnp_energy_storage", 2000000);

            QNP_ENERGY_PER_BLOCK = builder
                    .define("qnp_energy_per_block", 200);

            builder.pop();
        }

        public int getCapacityFor(String code) {
            if(code.equals("lithium_ion_cell")) {
                return LITHIUM_ION_BATTERY_STORAGE.get();
            }
            return BatteryBlocks.all().get(code).config().getStorage();
        }
    }
}