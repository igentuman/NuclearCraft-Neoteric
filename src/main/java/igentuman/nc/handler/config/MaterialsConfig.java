package igentuman.nc.handler.config;

import igentuman.nc.content.materials.*;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MaterialsConfig {
    public static <T> List<T> toList(Collection<T> vals)
    {
        return new ArrayList<>(vals);
    }
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final OresConfig ORE_CONFIG = new OresConfig(BUILDER);
    public static final MaterialProductsConfig MATERIAL_PRODUCTS = new MaterialProductsConfig(BUILDER);
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
        public ForgeConfigSpec.ConfigValue<List<Boolean>> INGOTS;
        public ForgeConfigSpec.ConfigValue<List<Boolean>> NUGGET;
        public ForgeConfigSpec.ConfigValue<List<Boolean>> BLOCK;
        public ForgeConfigSpec.ConfigValue<List<Boolean>> CHUNKS;
        public ForgeConfigSpec.ConfigValue<List<Boolean>> PLATES;
        public ForgeConfigSpec.ConfigValue<List<Boolean>> DUSTS;
        public ForgeConfigSpec.ConfigValue<List<Boolean>> GEMS;
        public ForgeConfigSpec.ConfigValue<List<String>> SLURRIES;

        public ForgeConfigSpec.ConfigValue<List<String>> MODS_PRIORITY;

        public MaterialProductsConfig(ForgeConfigSpec.Builder builder) {
            builder.comment("Settings for items registration").push("material_products");

            SLURRIES = builder
                    .comment("List of available slurries (dissolved ores in acid)")
                    .comment("Color for slurry will be calculate from average texture color")
                    .comment("Texture location has to be: nuclearcraft:textures/block/ore/(slurry_name)_ore.png")
                    .comment("If no texture found it will generate random color")
                    .define("register_slurries", List.of(
                            "uranium", "iron", "gold", "aluminum", "thorium", "boron", "silver",
                            "lead", "tin", "copper", "zinc", "cobalt", "platinum", "lithium", "magnesium"
                    ), o -> o instanceof ArrayList);

            CHUNKS = builder
                    .comment("Enable chunk registration: " + String.join(", ", Chunks.all().keySet()))
                    .define("register_chunk", Chunks.initialRegistration(), o -> o instanceof ArrayList);

            INGOTS = builder
                    .comment("Enable ingots registration: " + String.join(", ", Ingots.all().keySet()))
                    .define("register_ingot", Ingots.initialRegistration(), o -> o instanceof ArrayList);

            PLATES = builder
                    .comment("Enable plate registration: " + String.join(", ", Plates.all().keySet()))
                    .define("register_plate", Plates.initialRegistration(), o -> o instanceof ArrayList);

            DUSTS = builder
                    .comment("Enable dust registration: " + String.join(", ", Dusts.all().keySet()))
                    .define("register_dust", Dusts.initialRegistration(), o -> o instanceof ArrayList);

            NUGGET = builder
                    .comment("Enable nuggets registration: " + String.join(", ", Nuggets.all().keySet()))
                    .define("register_nugget", Nuggets.initialRegistration(), o -> o instanceof ArrayList);

            BLOCK = builder
                    .comment("Enable blocks registration: " + String.join(", ", Blocks.all().keySet()))
                    .define("register_block", Blocks.initialRegistration(), o -> o instanceof ArrayList);

            GEMS = builder
                    .comment("Enable gems registration: " + String.join(", ", Gems.all().keySet()))
                    .define("register_block", Gems.initialRegistration(), o -> o instanceof ArrayList);

            builder.pop();

            builder.comment("Forge Tag priority").push("forge_tag_priority");

            MODS_PRIORITY = builder
                    .comment("Priority of mods to resolve forge tags to itemstack.")
                    .define("mods_priority", List.of("nuclearcraft", "mekanism", "immersiveengineering", "tconstruct"), o -> o instanceof ArrayList);

            builder.pop();

        }
    }

    public static class OresConfig {

        public ForgeConfigSpec.ConfigValue<List<Integer>> ORE_AMOUNT;
        public ForgeConfigSpec.ConfigValue<List<Integer>> ORE_VEIN_SIZE;
        public ForgeConfigSpec.ConfigValue<List<List<Integer>>> ORE_DIMENSIONS;
        public ForgeConfigSpec.ConfigValue<List<Integer>> ORE_MIN_HEIGHT;
        public ForgeConfigSpec.ConfigValue<List<Integer>> ORE_MAX_HEIGHT;
        public ForgeConfigSpec.ConfigValue<List<Boolean>> REGISTER_ORE;

        public OresConfig(ForgeConfigSpec.Builder builder) {
            builder.comment("Settings for ore generation").push("ores");

            ORE_DIMENSIONS = builder
                    .comment("List of dimensions to generate ores: " + String.join(", ", Ores.all().keySet()))
                    .define("dimensions", Ores.initialOreDimensions(), o -> o instanceof ArrayList);

            REGISTER_ORE = builder
                    .comment("Enable ore registration: " + String.join(", ", Ores.all().keySet()))
                    .define("register_ore", Ores.initialOreRegistration(), o -> o instanceof ArrayList);

            ORE_VEIN_SIZE = builder
                    .comment("Ore blocks per vein. Order: " + String.join(", ", Ores.all().keySet()))
                    .define("vein_size", Ores.initialOreVeinSizes(), o -> o instanceof ArrayList);

            ORE_AMOUNT = builder
                    .comment("Veins in chunk. Order: " + String.join(", ", Ores.all().keySet()))
                    .define("veins_in_chunk", Ores.initialOreVeinsAmount(), o -> o instanceof ArrayList);

            ORE_MIN_HEIGHT = builder
                    .comment("Minimal generation height. Order: " + String.join(", ", Ores.all().keySet()))
                    .define("min_height", Ores.initialOreMinHeight(), o -> o instanceof ArrayList);

            ORE_MAX_HEIGHT = builder
                    .comment("Max generation height. Order: " + String.join(", ", Ores.all().keySet()))
                    .define("max_height", Ores.initialOreMaxHeight(), o -> o instanceof ArrayList);

            builder.pop();
        }
    }

}