package igentuman.nc.handler.config;

import igentuman.nc.content.materials.*;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.*;

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
        public List<ForgeConfigSpec.ConfigValue<Boolean>> INGOTS;
        public List<ForgeConfigSpec.ConfigValue<Boolean>> NUGGETS;
        public List<ForgeConfigSpec.ConfigValue<Boolean>> BLOCK;
        public List<ForgeConfigSpec.ConfigValue<Boolean>> RAW_CHUNKS;
        public List<ForgeConfigSpec.ConfigValue<Boolean>> PLATES;
        public List<ForgeConfigSpec.ConfigValue<Boolean>> DUSTS;
        public List<ForgeConfigSpec.ConfigValue<Boolean>> GEMS;
        public ForgeConfigSpec.ConfigValue<List<String>> SLURRIES;

        public ForgeConfigSpec.ConfigValue<List<String>> MODS_PRIORITY;

        public MaterialProductsConfig(ForgeConfigSpec.Builder builder) {
            builder.push("slurries");

            SLURRIES = builder
                    .comment("List of available slurries (dissolved ores in acid)")
                    .comment("Color for slurry will be calculate from average texture color")
                    .comment("Texture location has to be: nuclearcraft:textures/block/ore/(slurry_name)_ore.png")
                    .comment("If no texture found it will generate random color")
                    .define("register_slurries", List.of(
                            "uranium", "iron", "gold", "aluminum", "thorium", "boron", "silver",
                            "lead", "tin", "copper", "zinc", "cobalt", "platinum", "lithium", "magnesium"
                    ), o -> o instanceof ArrayList);
            builder.pop();
            RAW_CHUNKS = registrationList(builder, "raw_chunks", Chunks.get().all().keySet());
            INGOTS = registrationList(builder, "ingots", Ingots.get().all().keySet());
            NUGGETS = registrationList(builder, "nuggets", Nuggets.get().all().keySet());
            PLATES = registrationList(builder, "plates", Plates.get().all().keySet());
            DUSTS = registrationList(builder, "dusts", Dusts.get().all().keySet());
            BLOCK = registrationList(builder, "blocks", Blocks.get().all().keySet());
            GEMS = registrationList(builder, "gems", Gems.get().all().keySet());

            builder.comment("Forge Tag priority").push("forge_tag_priority");

            MODS_PRIORITY = builder
                    .comment("Priority of mods to resolve forge tags to itemstack and fluidstack.")
                    .define("mods_priority", List.of("minecraft", "nuclearcraft", "mekanism", "immersiveengineering", "tconstruct"), Objects::nonNull);
            builder.pop();
        }

        private List<ForgeConfigSpec.ConfigValue<Boolean>> registrationList(ForgeConfigSpec.Builder builder, String subCategory, Set<String> items) {
            List<ForgeConfigSpec.ConfigValue<Boolean>> rawOres = new ArrayList<>();
            builder.push(subCategory);
            for (String item : items) {
                rawOres.add(BUILDER.define(item, true));
            }
            builder.pop();
            return rawOres;
        }
    }

    public static class OresConfig {

        public HashMap<String, List<ForgeConfigSpec.ConfigValue<?>>> ORES;

        public OresConfig(ForgeConfigSpec.Builder builder) {
            builder.comment("Settings for ore generation").push("ores").pop();
            ORES = new HashMap<>();
            for(String name: Ores.all().keySet()) {
                ORES.put(name, buildOreConfig(builder, name));
            }
        }

        private List<ForgeConfigSpec.ConfigValue<?>> buildOreConfig(ForgeConfigSpec.Builder builder, String name) {
            List<ForgeConfigSpec.ConfigValue<?>> options = new ArrayList<>();
            builder.push(name);
            ForgeConfigSpec.ConfigValue<Boolean> register = builder.define("register", true);
            ForgeConfigSpec.ConfigValue<List<Integer>> dimensions = builder.define("dimensions", Ores.all().get(name).dimensions, o -> o instanceof ArrayList);
            ForgeConfigSpec.ConfigValue<Integer> veinSize = builder.define("vein_size", Ores.all().get(name).veinSize);
            ForgeConfigSpec.ConfigValue<Integer> min_height = builder.define("min_height", Ores.all().get(name).height[0]);
            ForgeConfigSpec.ConfigValue<Integer> max_height = builder.define("max_height", Ores.all().get(name).height[1]);
            options.add(register);
            options.add(dimensions);
            options.add(veinSize);
            options.add(min_height);
            options.add(max_height);
            builder.pop();
            return options;
        }
    }

}