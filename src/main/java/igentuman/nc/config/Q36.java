package igentuman.nc.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Q36 {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.IntValue PULSE_COOLDOWN_TICKS;
    public static final ModConfigSpec.IntValue PULSE_QC_COST;
    public static final ModConfigSpec.DoubleValue PULSE_DAMAGE;
    public static final ModConfigSpec.LongValue PULSE_RADIATION;
    public static final ModConfigSpec.IntValue PULSE_FX_TICKS;

    public static final ModConfigSpec.IntValue BEAM_COOLDOWN_TICKS;
    public static final ModConfigSpec.IntValue BEAM_QC_COST;
    public static final ModConfigSpec.DoubleValue BEAM_DAMAGE;
    public static final ModConfigSpec.LongValue BEAM_RADIATION;
    public static final ModConfigSpec.DoubleValue BEAM_RANGE;
    public static final ModConfigSpec.IntValue BEAM_BLOCK_BREAK_RADIUS;
    public static final ModConfigSpec.IntValue BEAM_FX_TICKS;

    public static final ModConfigSpec SPEC;

    static {
        BUILDER.push("q36_quantite_disruptor");
        PULSE_COOLDOWN_TICKS = BUILDER.defineInRange("pulse_cooldown_ticks", 10, 1, 1200);
        PULSE_QC_COST = BUILDER.defineInRange("pulse_qc_cost", 200, 0, Integer.MAX_VALUE);
        PULSE_DAMAGE = BUILDER.defineInRange("pulse_damage", 50.0, 0.0, 100000.0);
        PULSE_RADIATION = BUILDER.defineInRange("pulse_radiation", 200000L, 0L, Long.MAX_VALUE);
        PULSE_FX_TICKS = BUILDER.defineInRange("pulse_fx_ticks", 10, 0, 200);
        BEAM_COOLDOWN_TICKS = BUILDER.defineInRange("beam_cooldown_ticks", 60, 1, 1200);
        BEAM_QC_COST = BUILDER.defineInRange("beam_qc_cost", 2000, 0, Integer.MAX_VALUE);
        BEAM_DAMAGE = BUILDER.defineInRange("beam_damage", 200.0, 0.0, 100000.0);
        BEAM_RADIATION = BUILDER.defineInRange("beam_radiation", 2000000L, 0L, Long.MAX_VALUE);
        BEAM_RANGE = BUILDER.defineInRange("beam_range", 64.0, 1.0, 512.0);
        BEAM_BLOCK_BREAK_RADIUS = BUILDER.defineInRange("beam_block_break_radius", 2, 0, 16);
        BEAM_FX_TICKS = BUILDER.defineInRange("beam_fx_ticks", 10, 0, 200);
        BUILDER.pop();

        SPEC = BUILDER.build();
    }
}
