package igentuman.nc.config;

import igentuman.nc.recipe.TagOutputResolver;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public class Common {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.ConfigValue<List<? extends String>> MOD_TAG_PRIORITY;
    public static final ModConfigSpec.ConfigValue<Boolean> DEBUG_LOGGING;

    public static final ModConfigSpec.IntValue PIPE_TRANSFER_INTERVAL_TICKS;
    public static final ModConfigSpec.IntValue PIPE_ITEM_THROUGHPUT;
    public static final ModConfigSpec.IntValue PIPE_FLUID_THROUGHPUT;
    public static final ModConfigSpec.IntValue PIPE_ENERGY_THROUGHPUT;
    public static final ModConfigSpec.IntValue PIPE_MAX_NETWORK_SIZE;

    public static final ModConfigSpec SPEC;

    static {
        BUILDER.push("tags");
        MOD_TAG_PRIORITY = BUILDER
                .comment("Mod id priority for resolving tag-based recipe outputs.",
                        "Earlier entries win; namespaces not listed rank last (tiebreak = tag order).")
                .defineList("mod_tag_priority", List.of("minecraft"), o -> o instanceof String);
        BUILDER.pop();

        BUILDER.push("misc");
        DEBUG_LOGGING = BUILDER
                .comment("Debug logging output.",
                        "Enable in case of issues for investigation, otherwise might reduce performance.")
                .define("debug_logging", false);
        BUILDER.pop();

        BUILDER.push("pipes");
        PIPE_TRANSFER_INTERVAL_TICKS = BUILDER
                .comment("Ticks between active pipe transfer passes")
                .defineInRange("transfer_interval_ticks", 1, 1, 200);
        PIPE_ITEM_THROUGHPUT = BUILDER
                .comment("Max items a PULL connector moves per active transfer pass")
                .defineInRange("item_throughput", 512, 1, Integer.MAX_VALUE);
        PIPE_FLUID_THROUGHPUT = BUILDER
                .comment("Max mB a PULL connector moves per active transfer pass")
                .defineInRange("fluid_throughput", 512000, 1, Integer.MAX_VALUE);
        PIPE_ENERGY_THROUGHPUT = BUILDER
                .comment("Max FE a PULL connector moves per active transfer pass")
                .defineInRange("energy_throughput", 512000, 1, Integer.MAX_VALUE);
        PIPE_MAX_NETWORK_SIZE = BUILDER
                .comment("Node budget for a single pipe network's flood-fill discovery; caps recompute cost")
                .defineInRange("max_network_size", 4096, 16, Integer.MAX_VALUE);
        BUILDER.pop();

        SPEC = BUILDER.build();
    }

    /** Pushes the configured priority order into the resolver cache. Call on config load/reload. */
    public static void refreshTagPriority() {
        TagOutputResolver.setPriority(MOD_TAG_PRIORITY.get());
    }
}
