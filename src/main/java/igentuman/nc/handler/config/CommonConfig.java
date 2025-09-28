package igentuman.nc.handler.config;

import igentuman.nc.content.energy.BatteryBlocks;
import igentuman.nc.content.energy.RTGs;
import igentuman.nc.content.energy.SolarPanels;
import igentuman.nc.content.storage.BarrelBlocks;
import igentuman.nc.content.storage.ContainerBlocks;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static igentuman.nc.handler.config.CommonConfig.GTCEUCompatibilityConfig.GTCEUTier.*;


public class CommonConfig {
    public static <T> List<T> toList(Collection<T> vals)
    {
        return new ArrayList<>(vals);
    }
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final EnergyGenerationConfig ENERGY_GENERATION = new EnergyGenerationConfig(BUILDER);
    public static final EnergyStorageConfig ENERGY_STORAGE = new EnergyStorageConfig(BUILDER);
    public static final MiscConfig MISC_CONFIG = new MiscConfig(BUILDER);
    public static final GTCEUCompatibilityConfig GTCEU_CONFIG = new GTCEUCompatibilityConfig(BUILDER);

    public static final StorageBlocksConfig STORAGE_BLOCKS = new StorageBlocksConfig(BUILDER);
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

    public static class EnergyGenerationConfig {
        public final ForgeConfigSpec.ConfigValue<List<Boolean>> REGISTER_SOLAR_PANELS;
        public final ForgeConfigSpec.ConfigValue<List<Integer>> SOLAR_PANELS_GENERATION;
        public final ForgeConfigSpec.ConfigValue<List<Boolean>> REGISTER_RTG;
        public final ForgeConfigSpec.ConfigValue<List<Integer>> RTG_GENERATION;
        public final ForgeConfigSpec.ConfigValue<List<Integer>> RTG_RADIATION;
        public final ForgeConfigSpec.ConfigValue<Integer> STEAM_TURBINE;
        public final ForgeConfigSpec.ConfigValue<Integer> DECAY_GENERATOR;
        public final ForgeConfigSpec.ConfigValue<Double> GENERATION_MULTIPLIER;



        public EnergyGenerationConfig(ForgeConfigSpec.Builder builder) {
            builder.push("Energy");

            GENERATION_MULTIPLIER = builder
                    .comment("Multiplier for all power generation in the mod")
                    .defineInRange("generation_multiplier", 1.0, 0.001, 1000.0);

            REGISTER_SOLAR_PANELS = builder
                    .comment("Allow solar panel registration: " + String.join(", ", SolarPanels.all().keySet()))
                    .define("register_panel", SolarPanels.initialRegistered(), o -> o instanceof ArrayList);

            SOLAR_PANELS_GENERATION = builder
                    .comment("Solar panel power generation: " + String.join(", ", SolarPanels.all().keySet()))
                    .define("panel_power", SolarPanels.initialPower(), o -> o instanceof ArrayList);

            REGISTER_RTG = builder
                    .comment("Allow rtg registration: " + String.join(", ", RTGs.all().keySet()))
                    .define("register_rtg", RTGs.initialRegistered(), o -> o instanceof ArrayList);

            RTG_GENERATION = builder
                    .comment("rtg generation: " + String.join(", ", RTGs.all().keySet()))
                    .define("rtg_power", RTGs.initialPower(), o -> o instanceof ArrayList);

            RTG_RADIATION = builder
                    .comment("rtg radiation: " + String.join(", ", RTGs.all().keySet()))
                    .define("rtg_radiation", RTGs.initialRadiation(), o -> o instanceof ArrayList);

            STEAM_TURBINE = builder
                    .comment("Steam turbine (one block) base power gen")
                    .define("steam_turbine_power_gen", 80);

            DECAY_GENERATOR = builder
                    .comment("Decay Generator base power gen")
                    .define("decay_generator_power_gen", 128);

            builder.pop();
        }
    }

    public static class StorageBlocksConfig {
        public final ForgeConfigSpec.ConfigValue<List<Boolean>> REGISTER_BARREL;
        public final ForgeConfigSpec.ConfigValue<List<Boolean>> REGISTER_CONTAINER;
        public final ForgeConfigSpec.ConfigValue<List<Integer>> BARREL_CAPACITY;

        public StorageBlocksConfig(ForgeConfigSpec.Builder builder) {

            builder.push("storage_blocks")
                    .comment("Blocks to store items, fluids, etc...");

            REGISTER_CONTAINER = builder
                    .comment("Allow container registration: " + String.join(", ", BarrelBlocks.all().keySet()))
                    .define("container_block_registration", ContainerBlocks.initialRegistered(), o -> o instanceof ArrayList);

            REGISTER_BARREL = builder
                    .comment("Allow barrel registration: " + String.join(", ", BarrelBlocks.all().keySet()))
                    .define("barrel_block_registration", BarrelBlocks.initialRegistered(), o -> o instanceof ArrayList);

            BARREL_CAPACITY = builder
                    .comment("Barrel capacity in Buckets: " + String.join(", ", BarrelBlocks.all().keySet()))
                    .define("barrel_capacity", BarrelBlocks.initialCapacity(), o -> o instanceof ArrayList);

            builder.pop();
        }

    }

    public static class EnergyStorageConfig {
        public final ForgeConfigSpec.ConfigValue<List<Boolean>> REGISTER_ENERGY_BLOCK;
        public final ForgeConfigSpec.ConfigValue<List<Integer>> ENERGY_BLOCK_STORAGE;
        public final ForgeConfigSpec.ConfigValue<Integer> LITHIUM_ION_BATTERY_STORAGE;
        public final ForgeConfigSpec.ConfigValue<Integer> QNP_ENERGY_STORAGE;
        public final ForgeConfigSpec.ConfigValue<Integer> LIGHTNING_ROD_CHARGE;
        public final ForgeConfigSpec.ConfigValue<Integer> QNP_ENERGY_PER_BLOCK;

        public EnergyStorageConfig(ForgeConfigSpec.Builder builder) {
            builder.push("energy_storage");

            LIGHTNING_ROD_CHARGE = builder
                    .define("ligtning_rod_charge", 1048576);

            REGISTER_ENERGY_BLOCK = builder
                    .comment("Allow block registration: " + String.join(", ", BatteryBlocks.all().keySet()))
                    .define("energy_block_registration", BatteryBlocks.initialRegistered(), o -> o instanceof ArrayList);

            ENERGY_BLOCK_STORAGE = builder
                    .comment("Storage: " + String.join(", ", BatteryBlocks.all().keySet()))
                    .define("energy_block_storage", BatteryBlocks.initialPower(), o -> o instanceof ArrayList);

            LITHIUM_ION_BATTERY_STORAGE = builder
                    .define("lithium_ion_battery_storage", 1048576);

            QNP_ENERGY_STORAGE = builder
                    .define("qnp_energy_storage", 2097152);

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


    public static class MiscConfig {
        public final ForgeConfigSpec.ConfigValue<Boolean> DEBUG_LOG;

        public MiscConfig(ForgeConfigSpec.Builder builder) {
            builder.push("Misc");

            DEBUG_LOG = builder
                    .comment("Debug logging. Enable in case of issues to collect more data")
                    .define("debug_logging", false);

            builder.pop();
        }
    }

    public static class GTCEUCompatibilityConfig {
        public enum GTCEUCompatibility {
            ONLY_FE, ONLY_GTCEU, GTCEU_AND_FE
        }
        public enum GTCEUTier {
            ULV, LV, MV, HV, EV, IV, LuV, ZPM, UV, UHV, UEV, UIV, UXV, OpV, MAX;

            public static GTCEUTier byId(int energyTier) {
                if (energyTier < 0 || energyTier >= GTCEUTier.values().length) {
                    return GTCEUTier.MAX;
                }
                return GTCEUTier.values()[energyTier];
            }
        }

        public final ForgeConfigSpec.ConfigValue<GTCEUCompatibility> COMPATIBILITY;
        public final ForgeConfigSpec.ConfigValue<Boolean> OVERCHARGE_EXPLOSIONS;
        public final ForgeConfigSpec.ConfigValue<Boolean> LIMIT_FE_OUTPUT;
        public final ForgeConfigSpec.ConfigValue<GTCEUTier> FISSION_REACTOR_TIER;
        public final ForgeConfigSpec.ConfigValue<GTCEUTier> TURBINE_ENERGY_TIER;
        public final ForgeConfigSpec.ConfigValue<GTCEUTier> FUSION_REACTOR_ENERGY_TIER;
        public final ForgeConfigSpec.ConfigValue<GTCEUTier> KUGELBLITZ_ENERGY_TIER;
        public final ForgeConfigSpec.ConfigValue<GTCEUTier> ACCELERATORS_ENERGY_TIER;
        public final ForgeConfigSpec.ConfigValue<GTCEUTier> PROCESSOR_ENERGY_TIER;
        public final ForgeConfigSpec.ConfigValue<Integer> ENERGY_UPGRADES_NEEDED_TO_NEXT_TIER;

        public GTCEUCompatibilityConfig(ForgeConfigSpec.Builder builder) {
            builder.push("GregTech Energy Compatibility");
            List<String> tiers = Arrays.stream(GTCEUTier.values())
                    .map(GTCEUTier::name)
                    .toList();
            builder.comment("Tiers: " + String.join(", ", tiers));

            COMPATIBILITY = builder
                    .comment("ONLY_FE - Only FE energy system is used")
                    .comment("ONLY_GTCEU - Only GregTech Energy system is used")
                    .comment("GTCEU_AND_FE - Both systems are used, but GTCEU is preferred")
                    .defineEnum("gregtech_energy_compatibility", GTCEUCompatibility.GTCEU_AND_FE);

            FISSION_REACTOR_TIER = builder
                    .comment("This only counts if GTCEU is supported")
                    .define("fission_reactor_energy_tier", EV);
            
            OVERCHARGE_EXPLOSIONS = builder
                    .comment("This only counts if GTCEU is supported")
                    .comment("Explode machines when input energy is more than max input")
                    .comment("This doesn't count FE energy input")
                    .define("gregtech_energy_overcharge_explosions", true);

            LIMIT_FE_OUTPUT = builder
                    .comment("This only counts if GTCEU_AND_FE compatibility used")
                    .comment("Output FE/t will be limited to max GTCEU output")
                    .comment("Formula: FE/t = voltage * amperage * (EU to FE convertion rate, usually 4)")
                    .define("limit_fe_output", false);

            TURBINE_ENERGY_TIER = builder
                    .comment("This only counts if GTCEU is supported")
                    .define("turbine_energy_tier", EV);

            FUSION_REACTOR_ENERGY_TIER = builder
                    .comment("This only counts if GTCEU is supported")
                    .define("fusion_reactor_energy_tier", IV);

            KUGELBLITZ_ENERGY_TIER = builder
                    .comment("This only counts if GTCEU is supported")
                    .define("kugelblitz_energy_tier", LuV);

            ACCELERATORS_ENERGY_TIER = builder
                    .comment("This only counts if GTCEU is supported")
                    .define("accelerators_energy_tier", LuV);

            PROCESSOR_ENERGY_TIER = builder
                    .comment("This only counts if GTCEU is supported")
                    .define("processor_energy_tier", MV);

            ENERGY_UPGRADES_NEEDED_TO_NEXT_TIER = builder
                    .comment("This only counts if GTCEU is supported")
                    .comment("How many energy upgrades are needed for processor to reach next energy tier")
                    .defineInRange("energy_upgrades_for_next_tier", 16, 8, 64);

            builder.pop();
        }
    }
}