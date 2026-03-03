package igentuman.nc.handler.config;

import igentuman.nc.content.materials.*;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.*;

public class OreGenConfig {
    public static <T> List<T> toList(Collection<T> vals)
    {
        return new ArrayList<>(vals);
    }
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final OresConfig ORE_CONFIG = new OresConfig(BUILDER);
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

    public static class OresConfig {

        public HashMap<String, OreGenSpec> ORES;

        public OresConfig(ModConfigSpec.Builder builder) {
            ORES = new HashMap<>();
            for(String name: Ores.all().keySet()) {
                ORES.put(name, buildOreConfig(builder, name));
            }
        }

        public static class OreGenSpec {

            public final ModConfigSpec.ConfigValue<Boolean> register;
            public final ModConfigSpec.ConfigValue<List<String>> dimensions;
            public final ModConfigSpec.ConfigValue<Integer> veinSize;
            public final ModConfigSpec.ConfigValue<Integer> min_height;
            public final ModConfigSpec.ConfigValue<Integer> max_height;

            OreGenSpec(ModConfigSpec.Builder builder, boolean register, List<String> dimensions, int veinSize, int min_height, int max_height) {
                this.register = builder.define("register", register);
                this.dimensions = builder.define("gen_dimensions", dimensions, o -> o instanceof ArrayList<?>);
                this.veinSize = builder.defineInRange("vein_size", veinSize, 0, 64);
                this.min_height = builder.defineInRange("min_height", min_height, -64, 255);
                this.max_height = builder.defineInRange("max_height", max_height, -64, 255);
            }
        }

        private OreGenSpec buildOreConfig(ModConfigSpec.Builder builder, String name) {
            builder.push(name).comment("Ore generation settings for " + name);
            int veinSize = Ores.all().get(name).veinSize;
            if(name.equals("platinum")) {
                veinSize = 3;
            }
            OreGenSpec oreGen = new OreGenSpec(builder, true, Ores.all().get(name).dimensions, veinSize, Ores.all().get(name).height[0], Ores.all().get(name).height[1]);
            builder.pop();
            return oreGen;
        }
    }

}