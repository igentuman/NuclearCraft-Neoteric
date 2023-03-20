package igentuman.nc.handler.config;

import igentuman.nc.setup.materials.Ores;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.ArrayList;
import java.util.List;

import static igentuman.nc.world.dimension.Dimensions.WASTELAIND_ID;

public class CommonConfig {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ProcessorConfig PROCESSOR_CONFIG = new ProcessorConfig(BUILDER);
    public static final OresConfig ORE_CONFIG = new OresConfig(BUILDER);
    public static final MaterialProductsConfig MATERIAL_PRODUCTS = new MaterialProductsConfig(BUILDER);
    public static final DimensionConfig DIMENSION_CONFIG = new DimensionConfig(BUILDER);
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


    public static class MaterialProductsConfig {
        public static ForgeConfigSpec.ConfigValue<List<Boolean>> INGOTS;
        public static ForgeConfigSpec.ConfigValue<List<Boolean>> NUGGET;
        public static ForgeConfigSpec.ConfigValue<List<Boolean>> BLOCK;
        public static ForgeConfigSpec.ConfigValue<List<Boolean>> CHUNKS;
        public static ForgeConfigSpec.ConfigValue<List<Boolean>> PLATES;
        public static ForgeConfigSpec.ConfigValue<List<Boolean>> DUSTS;

        public MaterialProductsConfig(ForgeConfigSpec.Builder builder) {
            builder.comment("Settings for items registration").push("material_products");

            CHUNKS = builder
                    .comment("Enable chunk registration: " + String.join(", ", Ores.all().keySet()))
                    .define("register_chunk", Ores.initialOreRegistration());

            INGOTS = builder
                    .comment("Enable ingots registration: " + String.join(", ", Ores.all().keySet()))
                    .define("register_ingot", Ores.initialOreRegistration());

            PLATES = builder
                    .comment("Enable plate registration: " + String.join(", ", Ores.all().keySet()))
                    .define("register_plate", Ores.initialOreRegistration());

            DUSTS = builder
                    .comment("Enable dust registration: " + String.join(", ", Ores.all().keySet()))
                    .define("register_dust", Ores.initialOreRegistration());

            NUGGET = builder
                    .comment("Enable nuggets registration: " + String.join(", ", Ores.all().keySet()))
                    .define("register_nugget", Ores.initialOreRegistration());

            BLOCK = builder
                    .comment("Enable blocks registration: " + String.join(", ", Ores.all().keySet()))
                    .define("register_block", Ores.initialOreRegistration());


            builder.pop();
        }
    }

    public static class OresConfig {

        public static ForgeConfigSpec.ConfigValue<List<Integer>> ORE_AMOUNT;
        public static ForgeConfigSpec.ConfigValue<List<Integer>> ORE_VEIN_SIZE;
        public static ForgeConfigSpec.ConfigValue<List<List<Integer>>> ORE_DIMENSIONS;
        public static ForgeConfigSpec.ConfigValue<List<Integer>> ORE_MIN_HEIGHT;
        public static ForgeConfigSpec.ConfigValue<List<Integer>> ORE_MAX_HEIGHT;
        public static ForgeConfigSpec.ConfigValue<List<Boolean>> REGISTER_ORE;

        public OresConfig(ForgeConfigSpec.Builder builder) {
            builder.comment("Settings for ore generation").push("ores");

            ORE_DIMENSIONS = builder
                    .comment("List of dimensions to generate ores: " + String.join(", ", Ores.all().keySet()))
                    .define("dimensions", Ores.initialOreDimensions());

            REGISTER_ORE = builder
                    .comment("Enable ore registration: " + String.join(", ", Ores.all().keySet()))
                    .define("register_ore", Ores.initialOreRegistration());

            ORE_VEIN_SIZE = builder
                    .comment("Ore blocks per vein. Order: " + String.join(", ", Ores.all().keySet()))
                    .define("vein_size", Ores.initialOreVeinSizes());

            ORE_AMOUNT = builder
                    .comment("Veins in chunk. Order: " + String.join(", ", Ores.all().keySet()))
                    .define("veins_in_chunk", Ores.initialOreVeinsAmount());

            ORE_MIN_HEIGHT = builder
                    .comment("Minimal generation height. Order: " + String.join(", ", Ores.all().keySet()))
                    .define("min_height", Ores.initialOreMinHeight());

            ORE_MAX_HEIGHT = builder
                    .comment("Max generation height. Order: " + String.join(", ", Ores.all().keySet()))
                    .define("max_height", Ores.initialOreMaxHeight());

            builder.pop();
        }
    }

    public static class DimensionConfig {
        public final ForgeConfigSpec.ConfigValue<Boolean> registerWasteland;
        public final ForgeConfigSpec.ConfigValue<Integer> wastelandID;

        public DimensionConfig(ForgeConfigSpec.Builder builder) {
            builder.push("Dimension");
            registerWasteland = builder
                    .comment("Register Wasteland Dimension")
                    .define("wasteland", true);
            wastelandID = builder
                    .comment("Dimension ID for Wasteland")
                    .define("wastelandID", WASTELAIND_ID);
            builder.pop();
        }
    }

    public static class ProcessorConfig {
        public final ForgeConfigSpec.ConfigValue<Integer> base_time;
        public final ForgeConfigSpec.ConfigValue<Integer> base_power;


        public ProcessorConfig(ForgeConfigSpec.Builder builder) {
            builder.push("Processor");
            base_time = builder
                    .comment("Ticks")
                    .define("base_time", 240);
            base_power = builder
                    .comment("FE per Tick")
                    .define("base_power", 100);
            builder.pop();
        }
    }
}