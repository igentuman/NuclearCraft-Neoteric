package igentuman.nc.handler.config;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class ClientConfig {
    public static <T> List<T> toList(Collection<T> vals)
    {
        return new ArrayList<>(vals);
    }
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final MiscConfig MISC_CONFIG = new MiscConfig(BUILDER);
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

    public static class MiscConfig {
        public final ModConfigSpec.ConfigValue<Boolean> HIDE_PARTICLES;

        public MiscConfig(ModConfigSpec.Builder builder) {
            builder.push("Misc");

            HIDE_PARTICLES = builder
                    .comment("Hide particles from JEI/EMI")
                    .define("hide_particles", false);

            builder.pop();
        }
    }
}