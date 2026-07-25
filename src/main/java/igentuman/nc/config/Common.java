package igentuman.nc.config;

import igentuman.nc.recipe.TagOutputResolver;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.Arrays;
import java.util.List;

public class Common {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final AnomalyConfig ANOMALY_CONFIG;

    public static final ModConfigSpec.ConfigValue<List<? extends String>> MOD_TAG_PRIORITY;
    public static final ModConfigSpec.ConfigValue<Boolean> DEBUG_LOGGING;

    public static final ModConfigSpec.IntValue PIPE_TRANSFER_INTERVAL_TICKS;
    public static final ModConfigSpec.IntValue PIPE_ITEM_THROUGHPUT;
    public static final ModConfigSpec.IntValue PIPE_FLUID_THROUGHPUT;
    public static final ModConfigSpec.IntValue PIPE_ENERGY_THROUGHPUT;
    public static final ModConfigSpec.IntValue PIPE_MAX_NETWORK_SIZE;

    public static final ModConfigSpec.IntValue QNP_ENERGY_STORAGE;
    public static final ModConfigSpec.IntValue QNP_ENERGY_PER_BLOCK;

    public static final ModConfigSpec.IntValue HEV_ENERGY_STORAGE;

    public static final ModConfigSpec.BooleanValue IN_SITU_ENABLE_VEINS;
    public static final ModConfigSpec.ConfigValue<List<? extends Integer>> IN_SITU_VEIN_BLOCKS_AMOUNT;
    public static final ModConfigSpec.IntValue IN_SITU_VEINS_RARITY;
    public static final ModConfigSpec.BooleanValue IN_SITU_RANDOMIZED_ORES;
    public static final ModConfigSpec.BooleanValue IN_SITU_ALLOW_IE_VEINS;

    public static final ModConfigSpec SPEC;

    static {
        ANOMALY_CONFIG = new AnomalyConfig(BUILDER);

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

        BUILDER.push("qnp");
        QNP_ENERGY_STORAGE = BUILDER
                .comment("QNP internal FE capacity")
                .defineInRange("energy_storage", 2097152, 1, Integer.MAX_VALUE);
        QNP_ENERGY_PER_BLOCK = BUILDER
                .comment("FE consumed per block mined")
                .defineInRange("energy_per_block", 200, 1, Integer.MAX_VALUE);
        BUILDER.pop();

        BUILDER.push("hev");
        HEV_ENERGY_STORAGE = BUILDER
                .comment("HEV armor FE capacity per piece")
                .defineInRange("energy_storage", 1_000_000, 1000, Integer.MAX_VALUE);
        BUILDER.pop();

        BUILDER.push("in_situ_leaching");
        IN_SITU_ENABLE_VEINS = BUILDER
                .comment("Enable virtual vein generation")
                .define("enable_veins", true);
        IN_SITU_VEIN_BLOCKS_AMOUNT = BUILDER
                .comment("Min/max blocks per vein")
                .defineList("blocks_per_vein", List.of(50000, 1000000), o -> o instanceof Integer);
        IN_SITU_VEINS_RARITY = BUILDER
                .comment("Higher = less veins (1-5000)")
                .defineInRange("veins_rarity", 70, 1, 5000);
        IN_SITU_RANDOMIZED_ORES = BUILDER
                .comment("All veins get random ores, ignoring vein settings")
                .define("randomized_ores", false);
        IN_SITU_ALLOW_IE_VEINS = BUILDER
                .comment("Allow leaching IE veins via core sample")
                .define("allow_to_leach_ie_veins", true);
        BUILDER.pop();

        SPEC = BUILDER.build();
    }

    /** Pushes the configured priority order into the resolver cache. Call on config load/reload. */
    public static void refreshTagPriority() {
        TagOutputResolver.setPriority(MOD_TAG_PRIORITY.get());
    }

    public static class AnomalyConfig {
        public final ModConfigSpec.BooleanValue ENABLED;
        public final ModConfigSpec.BooleanValue SHADER;
        public final ModConfigSpec.DoubleValue SPAWN_CHANCE_PER_CELL;
        public final ModConfigSpec.IntValue CELL_SIZE;
        public final ModConfigSpec.IntValue ACTIVATION_CELL_RADIUS;

        public final ModConfigSpec.BooleanValue ENABLE_GRAVITATIONAL;
        public final ModConfigSpec.BooleanValue ENABLE_ELECTRIC;
        public final ModConfigSpec.BooleanValue ENABLE_RADIOACTIVE;
        public final ModConfigSpec.BooleanValue ENABLE_BURNING;
        public final ModConfigSpec.BooleanValue ENABLE_PSYCHO;
        public final ModConfigSpec.BooleanValue ENABLE_TELEPORTING;
        public final ModConfigSpec.IntValue WEIGHT_GRAVITATIONAL;
        public final ModConfigSpec.IntValue WEIGHT_ELECTRIC;
        public final ModConfigSpec.IntValue WEIGHT_RADIOACTIVE;
        public final ModConfigSpec.IntValue WEIGHT_BURNING;
        public final ModConfigSpec.IntValue WEIGHT_PSYCHO;
        public final ModConfigSpec.IntValue WEIGHT_TELEPORTING;

        public final ModConfigSpec.DoubleValue GRAV_RADIUS;
        public final ModConfigSpec.DoubleValue GRAV_PULL;
        public final ModConfigSpec.DoubleValue GRAV_LAUNCH_CHANCE;
        public final ModConfigSpec.DoubleValue GRAV_LAUNCH_POWER;
        public final ModConfigSpec.IntValue GRAV_ABSORB_THRESHOLD;
        public final ModConfigSpec.IntValue GRAV_ABSORB_INTERVAL;
        public final ModConfigSpec.BooleanValue GRAV_BLOCK_ABSORB;
        public final ModConfigSpec.BooleanValue GRAV_EXPLOSION;
        public final ModConfigSpec.DoubleValue GRAV_EXPLOSION_POWER;
        public final ModConfigSpec.DoubleValue GRAV_MASS_SCALE;

        public final ModConfigSpec.DoubleValue ELECTRIC_RADIUS;
        public final ModConfigSpec.DoubleValue ELECTRIC_DAMAGE;
        public final ModConfigSpec.IntValue ELECTRIC_INTERVAL;
        public final ModConfigSpec.IntValue ELECTRIC_DISCHARGE_FE;

        public final ModConfigSpec.IntValue RAD_RADIUS;
        public final ModConfigSpec.LongValue RAD_DOSE;
        public final ModConfigSpec.IntValue RAD_INTERVAL;

        public final ModConfigSpec.DoubleValue BURN_RADIUS;
        public final ModConfigSpec.DoubleValue BURN_EXPLOSION_RADIUS;
        public final ModConfigSpec.IntValue BURN_EXPLOSION_MIN_TICKS;
        public final ModConfigSpec.IntValue BURN_EXPLOSION_MAX_TICKS;
        public final ModConfigSpec.IntValue BURN_WATER_BLOCKS_TO_NEUTRALIZE;
        public final ModConfigSpec.BooleanValue BURN_BLOCK_IGNITE;
        public final ModConfigSpec.BooleanValue BURN_EXPLOSION;

        public final ModConfigSpec.DoubleValue PSYCHO_RADIUS;
        public final ModConfigSpec.IntValue PSYCHO_INTERVAL;
        public final ModConfigSpec.IntValue PSYCHO_AMPLIFIER;
        public final ModConfigSpec.IntValue PSYCHO_VEX_INTERVAL;
        public final ModConfigSpec.IntValue PSYCHO_VEX_MAX;
        public final ModConfigSpec.IntValue PSYCHO_VEX_LIFETIME;

        public final ModConfigSpec.DoubleValue TP_VICTIM_RADIUS;
        public final ModConfigSpec.IntValue TP_MAX_DISTANCE;
        public final ModConfigSpec.IntValue TP_SELF_MIN_TICKS;
        public final ModConfigSpec.IntValue TP_SELF_MAX_TICKS;
        public final ModConfigSpec.DoubleValue TP_SELF_RADIUS;
        public final ModConfigSpec.IntValue TP_HOVER_OFFSET;

        public final ModConfigSpec.BooleanValue SHARD_DROPS_ENABLED;
        public final ModConfigSpec.IntValue SHARD_DROP_MIN;
        public final ModConfigSpec.IntValue SHARD_DROP_MAX;
        public final ModConfigSpec.DoubleValue SHARD_DROP_CHANCE;
        public final ModConfigSpec.IntValue RARITY_WEIGHT_COMMON;
        public final ModConfigSpec.IntValue RARITY_WEIGHT_RARE;
        public final ModConfigSpec.IntValue RARITY_WEIGHT_EPIC;
        public final ModConfigSpec.IntValue RARITY_WEIGHT_LEGENDARY;
        public final ModConfigSpec.IntValue BUFF_REFRESH_TICKS;
        public final ModConfigSpec.IntValue BUFF_DURATION_TICKS;
        public final ModConfigSpec.ConfigValue<List<? extends String>> BUFF_EFFECT_BLACKLIST;

        public AnomalyConfig(ModConfigSpec.Builder builder) {
            builder.push("anomalies");

            ENABLED = builder.comment("Master switch for the Wasteland anomaly system").define("enabled", true);
            SHADER = builder.comment("Enable per-variant anomaly post-process shaders. Disable on weak GPUs.").define("shader", true);
            SPAWN_CHANCE_PER_CELL = builder.comment("Probability (0..1) that a placement cell contains an anomaly").defineInRange("spawn_chance_per_cell", 0.45D, 0.0D, 1.0D);
            CELL_SIZE = builder.comment("Side length in blocks of each square placement cell").defineInRange("cell_size", 128, 16, 4096);
            ACTIVATION_CELL_RADIUS = builder.comment("Cells around each player scanned for spawning").defineInRange("activation_cell_radius", 2, 0, 8);

            builder.push("variants");
            ENABLE_GRAVITATIONAL = builder.define("enable_gravitational", true);
            WEIGHT_GRAVITATIONAL = builder.defineInRange("weight_gravitational", 2, 0, 1000);
            ENABLE_ELECTRIC = builder.define("enable_electric", true);
            WEIGHT_ELECTRIC = builder.defineInRange("weight_electric", 3, 0, 1000);
            ENABLE_RADIOACTIVE = builder.define("enable_radioactive", true);
            WEIGHT_RADIOACTIVE = builder.defineInRange("weight_radioactive", 3, 0, 1000);
            ENABLE_BURNING = builder.define("enable_burning", true);
            WEIGHT_BURNING = builder.defineInRange("weight_burning", 3, 0, 1000);
            ENABLE_PSYCHO = builder.define("enable_psycho", true);
            WEIGHT_PSYCHO = builder.defineInRange("weight_psycho", 2, 0, 1000);
            ENABLE_TELEPORTING = builder.define("enable_teleporting", true);
            WEIGHT_TELEPORTING = builder.defineInRange("weight_teleporting", 2, 0, 1000);
            builder.pop();

            builder.push("gravitational");
            GRAV_RADIUS = builder.defineInRange("radius", 12.0D, 1.0D, 32.0D);
            GRAV_PULL = builder.defineInRange("pull", 0.3D, 0.0D, 2.0D);
            GRAV_LAUNCH_CHANCE = builder.defineInRange("launch_chance", 0.05D, 0.0D, 1.0D);
            GRAV_LAUNCH_POWER = builder.defineInRange("launch_power", 2.0D, 0.0D, 10.0D);
            GRAV_ABSORB_THRESHOLD = builder.defineInRange("absorb_threshold", 320, 1, 100000);
            GRAV_ABSORB_INTERVAL = builder.defineInRange("absorb_interval_ticks", 50, 1, 1200);
            GRAV_BLOCK_ABSORB = builder.define("block_absorb", true);
            GRAV_EXPLOSION = builder.define("explosion_on_collapse", true);
            GRAV_EXPLOSION_POWER = builder.defineInRange("explosion_power", 18.0D, 0.0D, 64.0D);
            GRAV_MASS_SCALE = builder.defineInRange("mass_scale", 1.0D, 0.0D, 8.0D);
            builder.pop();

            builder.push("electric");
            ELECTRIC_RADIUS = builder.defineInRange("radius", 28.0D, 1.0D, 64.0D);
            ELECTRIC_DAMAGE = builder.defineInRange("damage", 16.0D, 0.0D, 1000.0D);
            ELECTRIC_INTERVAL = builder.defineInRange("strike_interval_ticks", 50, 1, 1200);
            ELECTRIC_DISCHARGE_FE = builder.defineInRange("discharge_fe_threshold", 100_000_000, 1, Integer.MAX_VALUE);
            builder.pop();

            builder.push("radioactive");
            RAD_RADIUS = builder.defineInRange("radius", 48, 4, 256);
            RAD_DOSE = builder.defineInRange("dose", 100_000L, 0L, Long.MAX_VALUE);
            RAD_INTERVAL = builder.defineInRange("emit_interval_ticks", 20, 1, 1200);
            builder.pop();

            builder.push("burning");
            BURN_RADIUS = builder.defineInRange("radius", 10.0D, 1.0D, 64.0D);
            BURN_EXPLOSION_RADIUS = builder.defineInRange("explosion_radius", 2.0D, 0.0D, 64.0D);
            BURN_EXPLOSION_MIN_TICKS = builder.defineInRange("explosion_min_ticks", 200, 1, 24000);
            BURN_EXPLOSION_MAX_TICKS = builder.defineInRange("explosion_max_ticks", 400, 1, 24000);
            BURN_WATER_BLOCKS_TO_NEUTRALIZE = builder.defineInRange("water_blocks_to_neutralize", 8, 1, 1000);
            BURN_BLOCK_IGNITE = builder.define("block_ignite", true);
            BURN_EXPLOSION = builder.define("periodic_explosion", true);
            builder.pop();

            builder.push("psycho");
            PSYCHO_RADIUS = builder.defineInRange("radius", 48.0D, 1.0D, 128.0D);
            PSYCHO_INTERVAL = builder.defineInRange("interval_ticks", 40, 1, 1200);
            PSYCHO_AMPLIFIER = builder.defineInRange("effect_amplifier", 0, 0, 10);
            PSYCHO_VEX_INTERVAL = builder.defineInRange("vex_spawn_interval_ticks", 40, 1, 1200);
            PSYCHO_VEX_MAX = builder.defineInRange("vex_max_nearby", 6, 0, 64);
            PSYCHO_VEX_LIFETIME = builder.defineInRange("vex_lifetime_ticks", 1200, 0, 24000);
            builder.pop();

            builder.push("teleporting");
            TP_VICTIM_RADIUS = builder.defineInRange("victim_radius", 5.0D, 1.0D, 32.0D);
            TP_MAX_DISTANCE = builder.defineInRange("victim_max_distance", 100, 1, 1000);
            TP_SELF_MIN_TICKS = builder.defineInRange("self_blink_min_ticks", 60, 1, 1200);
            TP_SELF_MAX_TICKS = builder.defineInRange("self_blink_max_ticks", 120, 1, 1200);
            TP_SELF_RADIUS = builder.defineInRange("self_blink_radius", 12.0D, 1.0D, 32.0D);
            TP_HOVER_OFFSET = builder.defineInRange("hover_offset", 3, 0, 32);
            builder.pop();

            builder.push("shards");
            SHARD_DROPS_ENABLED = builder.define("shard_drops_enabled", true);
            SHARD_DROP_MIN = builder.defineInRange("shard_drop_min", 1, 0, 64);
            SHARD_DROP_MAX = builder.defineInRange("shard_drop_max", 2, 0, 64);
            SHARD_DROP_CHANCE = builder.defineInRange("shard_drop_chance", 1.0D, 0.0D, 1.0D);
            builder.pop();

            builder.push("crystal");
            RARITY_WEIGHT_COMMON    = builder.defineInRange("rarity_weight_common",    85, 0, 1000000);
            RARITY_WEIGHT_RARE      = builder.defineInRange("rarity_weight_rare",        8, 0, 1000000);
            RARITY_WEIGHT_EPIC      = builder.defineInRange("rarity_weight_epic",         6, 0, 1000000);
            RARITY_WEIGHT_LEGENDARY = builder.defineInRange("rarity_weight_legendary",    1, 0, 1000000);
            BUFF_REFRESH_TICKS      = builder.comment("Ticks between passive buff re-applications").defineInRange("buff_refresh_ticks", 120, 1, 1200);
            BUFF_DURATION_TICKS     = builder.comment("Duration of the applied buff effect").defineInRange("buff_duration_ticks", 240, 1, 24000);
            BUFF_EFFECT_BLACKLIST   = builder.comment("Effect ids excluded from crystal buff pool").defineList("buff_effect_blacklist",
                    Arrays.asList("minecraft:hero_of_the_village", "minecraft:dolphins_grace", "minecraft:conduit_power"),
                    o -> o instanceof String);
            builder.pop();

            builder.pop();
        }
    }
}
