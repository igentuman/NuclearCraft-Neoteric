package igentuman.nc.handler.config;

import igentuman.nc.content.materials.*;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.*;

public class OreGenConfig {
    public static <T> List<T> toList(Collection<T> vals)
    {
        return new ArrayList<>(vals);
    }
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final OresConfig ORE_CONFIG = new OresConfig(BUILDER);
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

    public static class OresConfig {

        public HashMap<String, OreGenSpec> ORES;

        public OresConfig(ForgeConfigSpec.Builder builder) {
            ORES = new HashMap<>();
            for(String name: Ores.all().keySet()) {
                ORES.put(name, buildOreConfig(builder, name));
            }
        }

        public static class OreGenSpec {

            public final ForgeConfigSpec.ConfigValue<Boolean> register;
            public final ForgeConfigSpec.ConfigValue<List<String>> dimensions;
            public final ForgeConfigSpec.ConfigValue<Integer> veinSize;
            public final ForgeConfigSpec.ConfigValue<Integer> min_height;
            public final ForgeConfigSpec.ConfigValue<Integer> max_height;

            OreGenSpec(ForgeConfigSpec.Builder builder, boolean register, List<String> dimensions, int veinSize, int min_height, int max_height) {
                this.register = builder.define("register", register);
                this.dimensions = builder.define("gen_dimensions", dimensions, o -> o instanceof ArrayList<?>);
                this.veinSize = builder.defineInRange("vein_size", veinSize, 0, 64);
                this.min_height = builder.defineInRange("min_height", min_height, -64, 255);
                this.max_height = builder.defineInRange("max_height", max_height, -64, 255);
            }
        }

        private OreGenSpec buildOreConfig(ForgeConfigSpec.Builder builder, String name) {
            builder.push(name).comment("Ore generation settings for " + name);
            OreGenSpec oreGen = new OreGenSpec(builder, true, Ores.all().get(name).dimensions, Ores.all().get(name).veinSize, Ores.all().get(name).height[0], Ores.all().get(name).height[1]);
            builder.pop();
            return oreGen;
        }
    }

}