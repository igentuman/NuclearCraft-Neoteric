package igentuman.nc.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Bomb {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.IntValue RADIUS_HORIZONTAL;
    public static final ModConfigSpec.IntValue RADIUS_VERTICAL;
    public static final ModConfigSpec.IntValue FUSE_TICKS;
    public static final ModConfigSpec.IntValue OPS_PER_TICK;
    public static final ModConfigSpec.IntValue CHUNK_RESENDS_PER_TICK;
    public static final ModConfigSpec.BooleanValue FAST_BLOCK_WRITES;
    public static final ModConfigSpec.BooleanValue RADIATION_ENABLED;
    public static final ModConfigSpec.DoubleValue RADIATION_FALLOUT_AREAL_DENSITY;
    public static final ModConfigSpec.DoubleValue RADIATION_SOURCE_YIELD_SCALE;

    public static final ModConfigSpec SPEC;

    static {
        BUILDER.push("pu_239_bomb");
        RADIUS_HORIZONTAL = BUILDER
                .comment("Horizontal blast radius in blocks (default 196).")
                .defineInRange("radius_horizontal", 196, 16, 384);
        RADIUS_VERTICAL = BUILDER
                .comment("Vertical blast radius in blocks (default 64).")
                .defineInRange("radius_vertical", 64, 8, 256);
        FUSE_TICKS = BUILDER
                .comment("Ticks between redstone arming and detonation (default 60 = 3 s).")
                .defineInRange("fuse_ticks", 60, 1, 12000);
        OPS_PER_TICK = BUILDER
                .comment("Block ops applied per server tick during detonation (default 16384).")
                .defineInRange("ops_per_tick", 16384, 64, 65536);
        CHUNK_RESENDS_PER_TICK = BUILDER
                .comment("Whole-chunk packets resent to clients per server tick during detonation. Raise to make the blast visible faster; bandwidth scales with player count.")
                .defineInRange("chunk_resends_per_tick", 16, 1, 1024);
        FAST_BLOCK_WRITES = BUILDER
                .comment("If true, bombs mutate chunk sections directly and resend whole chunks instead of using server.setBlock per op. ~50-100x faster but skips neighbor updates, fluid flow, falling-block triggers, and block events inside the blast. Set false to fall back to the vanilla setBlock path.")
                .define("fast_block_writes", true);
        RADIATION_ENABLED = BUILDER
                .comment("Spawn a Persistent radiation source at the epicenter and stamp soil-profile fallout on every chunk inside the blast radius. Requires NuclearRadiation to be installed.")
                .define("radiation_enabled", true);
        RADIATION_FALLOUT_AREAL_DENSITY = BUILDER
                .comment("Atom-count multiplier applied to per-chunk fallout (distance-scaled). 1.0 mirrors the fission meltdown chunk stamp; raise for hotter fallout near the edge.")
                .defineInRange("radiation_fallout_areal_density", 1.0, 0.0, 4.0);
        RADIATION_SOURCE_YIELD_SCALE = BUILDER
                .comment("Atom-count multiplier applied to the epicenter LeftOverRadSource. Tune against meltdown magnitudes for consistent feel.")
                .defineInRange("radiation_source_yield_scale", 1.0, 0.0, 8.0);
        BUILDER.pop();

        SPEC = BUILDER.build();
    }
}
