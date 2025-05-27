package igentuman.nc.handler.config;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static igentuman.nc.world.dimension.Dimensions.WASTELAND_ID;

public class WorldConfig {
    public static <T> List<T> toList(Collection<T> vals)
    {
        return new ArrayList<>(vals);
    }
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final DimensionConfig DIMENSION_CONFIG = new DimensionConfig(BUILDER);
    public static final BiomeConfig BIOME_CONFIG = new BiomeConfig(BUILDER);
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

    public static class DimensionConfig {
        public ForgeConfigSpec.ConfigValue<Boolean> registerWasteland;

        public DimensionConfig(ForgeConfigSpec.Builder builder) {
            builder.push("Dimension");
            registerWasteland = builder
                    .comment("Register Wasteland Dimension")
                    .define("wasteland", true);
            builder.pop();
        }
    }

    public static class BiomeConfig {
        public ForgeConfigSpec.ConfigValue<Boolean> registerWasteland;

        public BiomeConfig(ForgeConfigSpec.Builder builder) {
            builder.push("Biome");
            registerWasteland = builder
                    .comment("Generate Wasteland Biome in Overworld")
                    .define("wasteland", true);
            builder.pop();
        }
    }
}