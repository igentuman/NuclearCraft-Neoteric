package igentuman.nc.handler.config;

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

public class TurbineConfig {
    public static <T> List<T> toList(Collection<T> vals)
    {
        return new ArrayList<>(vals);
    }
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final TurbineConf TURBINE_CONFIG = new TurbineConf(BUILDER);
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

    public static class TurbineConf {
        public ForgeConfigSpec.ConfigValue<Integer> MIN_SIZE;
        public ForgeConfigSpec.ConfigValue<Integer> MAX_SIZE;
        public ForgeConfigSpec.ConfigValue<Double> ENERGY_GEN;
        public ForgeConfigSpec.ConfigValue<Integer> BLADE_FLOW;
        public ForgeConfigSpec.ConfigValue<List<Double>> EFFICIENCY;
        public HashMap<String, ForgeConfigSpec.ConfigValue<List<String>>> PLACEMENT_RULES = new HashMap<>();

        public TurbineConf(ForgeConfigSpec.Builder builder) {
            builder.comment("Settings for Turbine").push("turbine");

            MIN_SIZE = builder
                    .comment("Multiblock min size.")
                    .defineInRange("min_size", 5, 5, 25);

            MAX_SIZE = builder
                    .comment("Multiblock max size.")
                    .defineInRange("max_size", 24, 5, 25);

            BLADE_FLOW = builder
                    .comment("Steam flow per blade mB/t")
                    .defineInRange("blade_flow", 2000, 100, 1000000);

            ENERGY_GEN = builder
                    .comment("Energy gen multiplier")
                    .defineInRange("energy_gen", 10D, 1D, 1000000D);

            EFFICIENCY = builder
                    .comment("Efficiency %: " + String.join(", ", TurbineRegistration.initialEfficiency().keySet()))
                    .define("efficiency", toList(TurbineRegistration.initialEfficiency().values()), o -> o instanceof ArrayList);

            builder
                    .comment("You can define blocks by block_name. So copper_turbine_coil will fall back to nuclearcraft:copper_turbine_coil. Or qualify it with namespace like some_mod:some_block.")
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

            for(String name: TurbineRegistration.coils().keySet()) {
                if(name.contains("empty")) continue;
                PLACEMENT_RULES.put(name, builder
                        .define(name, TurbineRegistration.initialPlacementRules(name), o -> o instanceof ArrayList));
            }

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