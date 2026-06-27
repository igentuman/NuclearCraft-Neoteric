package igentuman.nc.config;

import igentuman.nc.recipe.TagOutputResolver;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public class Common {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.ConfigValue<List<? extends String>> MOD_TAG_PRIORITY;

    public static final ModConfigSpec SPEC;

    static {
        BUILDER.push("tags");
        MOD_TAG_PRIORITY = BUILDER
                .comment("Mod id priority for resolving tag-based recipe outputs.",
                        "Earlier entries win; namespaces not listed rank last (tiebreak = tag order).")
                .defineList("mod_tag_priority", List.of("minecraft"), o -> o instanceof String);
        BUILDER.pop();

        SPEC = BUILDER.build();
    }

    /** Pushes the configured priority order into the resolver cache. Call on config load/reload. */
    public static void refreshTagPriority() {
        TagOutputResolver.setPriority(MOD_TAG_PRIORITY.get());
    }
}
