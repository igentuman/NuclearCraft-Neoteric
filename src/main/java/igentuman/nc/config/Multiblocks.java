package igentuman.nc.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Multiblocks {
    public static final ModConfigSpec.ConfigValue<Boolean> DEBUG_LOGGING;
    public static final ModConfigSpec SPEC;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.push("general");
        DEBUG_LOGGING = builder.comment("Enable debug logging for multiblocks.")
                .define("debug_logging", false);
        builder.pop();
        SPEC = builder.build();
    }
}
