package igentuman.nc.handler.config;

import igentuman.nc.content.energy.BatteryBlocks;
import igentuman.nc.content.energy.RTGs;
import igentuman.nc.content.energy.SolarPanels;
import igentuman.nc.content.storage.BarrelBlocks;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CommonConfig {
    public static <T> List<T> toList(Collection<T> vals)
    {
        return new ArrayList<>(vals);
    }
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final EnergyGenerationConfig ENERGY_GENERATION = new EnergyGenerationConfig(BUILDER);
    public static final EnergyStorageConfig ENERGY_STORAGE = new EnergyStorageConfig(BUILDER);

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
        public ForgeConfigSpec.ConfigValue<Integer> LIGHTNING_ROD_CHARGE;
        public ForgeConfigSpec.ConfigValue<Integer> QNP_ENERGY_PER_BLOCK;

        public EnergyStorageConfig(ForgeConfigSpec.Builder builder) {
            builder.push("energy_storage");

            LIGHTNING_ROD_CHARGE = builder
                    .define("ligtning_rod_charge", 1000000);

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