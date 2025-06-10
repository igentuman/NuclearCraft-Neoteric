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
        public final ForgeConfigSpec.ConfigValue<List<String>> MODS_PRIORITY;

        public MaterialProductsConfig(ForgeConfigSpec.Builder builder) {

            RAW_CHUNKS = registrationList(builder, "raw_chunks", Chunks.get().all().keySet());
            INGOTS = registrationList(builder, "ingots", Ingots.get().all().keySet());
            NUGGETS = registrationList(builder, "nuggets", Nuggets.get().all().keySet());
            PLATES = registrationList(builder, "plates", Plates.get().all().keySet());
            DUSTS = registrationList(builder, "dusts", Dusts.get().all().keySet());
            BLOCK = registrationList(builder, "blocks", Blocks.get().all().keySet());
            GEMS = registrationList(builder, "gems", Gems.get().all().keySet());

            MODS_PRIORITY = builder
                    .push("forge_tag_priority")
                    .comment("Priority of mods to resolve forge tags to itemstack and fluidstack.")
                    .define("mods_priority", List.of("minecraft", "nuclearcraft", "mekanism", "immersiveengineering", "tconstruct"), o -> o instanceof ArrayList);
        }

        private List<ForgeConfigSpec.ConfigValue<Boolean>> registrationList(ForgeConfigSpec.Builder builder, String subCategory, Set<String> items) {
            List<ForgeConfigSpec.ConfigValue<Boolean>> rawOres = new ArrayList<>();
            builder.push(subCategory);
            for (String item : items) {
                rawOres.add(builder.define(item, true, o -> o instanceof Boolean));
            }
            builder.pop();
            return rawOres;
        }
    }
}