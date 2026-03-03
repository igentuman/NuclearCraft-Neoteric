package igentuman.nc.handler.config;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static igentuman.nc.world.dimension.Dimensions.WASTELAND_ID;

public class WorldConfig {
    public static <T> List<T> toList(Collection<T> vals)
    {
        return new ArrayList<>(vals);
    }
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final DimensionConfig DIMENSION_CONFIG = new DimensionConfig(BUILDER);
    public static final VillageConfig VILLAGE_CONFIG = new VillageConfig(BUILDER);
    public static final BiomeConfig BIOME_CONFIG = new BiomeConfig(BUILDER);
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

    public static class VillageConfig {
        public final ModConfigSpec.ConfigValue<Boolean> generateScientistHouse;
        public final ModConfigSpec.ConfigValue<Boolean> addWandererTrades;

        public VillageConfig(ModConfigSpec.Builder builder) {
            builder.push("Villages");
            generateScientistHouse = builder
                    .comment("Generate Scientist House in Villages")
                    .define("scientist_house", true);
            addWandererTrades = builder
                    .comment("Add Wandering Trader trades")
                    .define("wandering_trader_trades", true);
            builder.pop();
        }
    }

    public static class DimensionConfig {
        public final ModConfigSpec.ConfigValue<Boolean> registerWasteland;

        public DimensionConfig(ModConfigSpec.Builder builder) {
            builder.push("Dimension");
            registerWasteland = builder
                    .comment("Register Wasteland Dimension")
                    .define("wasteland", true);
            builder.pop();
        }
    }

    public static class BiomeConfig {
        public final ModConfigSpec.ConfigValue<Boolean> registerWasteland;

        public BiomeConfig(ModConfigSpec.Builder builder) {
            builder.push("Biome");
            registerWasteland = builder
                    .comment("Generate Wasteland Biome in Overworld")
                    .define("wasteland", true);
            builder.pop();
        }
    }
}