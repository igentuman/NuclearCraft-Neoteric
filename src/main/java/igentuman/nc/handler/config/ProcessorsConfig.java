package igentuman.nc.handler.config;

import igentuman.nc.content.processors.Processors;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class ProcessorsConfig {
    public static <T> List<T> toList(Collection<T> vals)
    {
        return new ArrayList<>(vals);
    }
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ProcessorConfig PROCESSOR_CONFIG = new ProcessorConfig(BUILDER);
    public static final InSituLeachingConfig IN_SITU_LEACHING = new InSituLeachingConfig(BUILDER);
    public static final ModConfigSpec spec = BUILDER.build();
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
        public final ModConfigSpec.ConfigValue<Boolean> ENABLE_VEINS;
        public final ModConfigSpec.ConfigValue<List<Integer>> VEIN_BLOCKS_AMOUNT;
        public final ModConfigSpec.ConfigValue<Integer> VEINS_RARITY;
        public final ModConfigSpec.ConfigValue<Boolean> RANDOMIZED_ORES;
        public final ModConfigSpec.ConfigValue<Boolean> ADD_IE_VEINS;
        public final ModConfigSpec.ConfigValue<Boolean> ALLOW_TO_LEACH_IE_VEINS;


        public InSituLeachingConfig(ModConfigSpec.Builder builder) {
            builder.comment("Settings for In situ leaching").push("in_situ_leaching");

            ENABLE_VEINS = builder
                    .comment("Enable veins generation.")
                    .define("enable_veins", true);

            VEIN_BLOCKS_AMOUNT = builder
                    .comment("Min and max values of blocks per vein.")
                    .comment("Result amount will be random value in this range.")
                    .define("blocks_per_vein", List.of(30000, 70000), o -> o instanceof List);

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
        public final ModConfigSpec.ConfigValue<Integer> GT_AMPERAGE;
        public final ModConfigSpec.ConfigValue<Integer> GT_SUPPORT;
        public final ModConfigSpec.ConfigValue<Boolean> GT_EXPLODE;
        public final ModConfigSpec.ConfigValue<Integer> BASE_TIME;
        public final ModConfigSpec.ConfigValue<Integer> BASE_POWER;
        public final ModConfigSpec.ConfigValue<Integer> SKIP_TICKS;
        public HashMap<String, ProcessorConfigSpec> PROCESSOR_CONFIG;

        public static class ProcessorConfigSpec {
            public final ModConfigSpec.ConfigValue<Boolean> register;
            public final ModConfigSpec.ConfigValue<Integer> base_power;
            public final ModConfigSpec.ConfigValue<Integer> base_time;

            public ProcessorConfigSpec(ModConfigSpec.Builder builder, boolean register, int base_power, int base_time) {
                this.register = builder.define("register", register);
                this.base_power = builder.define("base_power", base_power);
                this.base_time = builder.define("base_time", base_time);
            }
        }

        public ProcessorConfig(ModConfigSpec.Builder builder) {
            builder.push("Common settings");
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
            builder.pop();

            PROCESSOR_CONFIG = new HashMap<>();
            for(String processor: Processors.all().keySet()) {
                builder.push(processor);
                PROCESSOR_CONFIG.put(processor, new ProcessorConfigSpec(
                        builder, true,
                        Processors.all().get(processor).getPower(),
                        Processors.all().get(processor).getTime())
                );
                builder.pop();
            }

        }
    }
}