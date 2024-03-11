package igentuman.nc.handler.config;

import igentuman.nc.content.Electromagnets;
import igentuman.nc.content.RFAmplifier;
import igentuman.nc.content.energy.BatteryBlocks;
import igentuman.nc.content.energy.RTGs;
import igentuman.nc.content.energy.SolarPanels;
import igentuman.nc.content.fuel.FuelManager;
import igentuman.nc.content.processors.Processors;
import igentuman.nc.content.storage.BarrelBlocks;
import igentuman.nc.multiblock.fission.FissionBlocks;
import igentuman.nc.multiblock.turbine.TurbineRegistration;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.*;


public class ProcessorsConfig {
    public static <T> List<T> toList(Collection<T> vals)
    {
        return new ArrayList<>(vals);
    }
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ProcessorConfig PROCESSOR_CONFIG = new ProcessorConfig(BUILDER);
    public static final InSituLeachingConfig IN_SITU_LEACHING = new InSituLeachingConfig(BUILDER);
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
                    .define("blocks_per_vein", Arrays.asList(30000, 70000), o -> o instanceof ArrayList);

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

    public static class ProcessorConfig {
        public ForgeConfigSpec.ConfigValue<Integer> GT_AMPERAGE;
        public ForgeConfigSpec.ConfigValue<Integer> GT_SUPPORT;
        public ForgeConfigSpec.ConfigValue<Boolean> GT_EXPLODE;
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
                    .comment("Better use value multiple of 8")
                    .define("base_power", 128);

            GT_AMPERAGE = builder
                    .comment("GT EU Amperage")
                    .define("gteu_amperage", 2);

            GT_SUPPORT = builder
                    .comment("GT EU direct support enabled?")
                    .comment("0 - disabled, 1 - enabled EU and FE, 2 - EU only")
                    .define("gteu_support", 1);

            GT_EXPLODE = builder
                    .comment("Enable explosion on wrong GE EU amperage")
                    .define("gteu_explode", false);

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
}