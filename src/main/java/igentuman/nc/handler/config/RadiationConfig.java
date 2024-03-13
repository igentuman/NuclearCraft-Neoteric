package igentuman.nc.handler.config;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class RadiationConfig {
    public static <T> List<T> toList(Collection<T> vals)
    {
        return new ArrayList<>(vals);
    }
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final RadiationConf RADIATION_CONFIG = new RadiationConf(BUILDER);
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

    public static class RadiationConf {
        public ForgeConfigSpec.ConfigValue<Boolean> ENABLED;
        public ForgeConfigSpec.ConfigValue<Integer> SPREAD_GATE;
        public ForgeConfigSpec.ConfigValue<Integer> NATURAL_RADIATION;
        public ForgeConfigSpec.ConfigValue<Double> SPREAD_MULTIPLIER;
        public ForgeConfigSpec.ConfigValue<Integer> DECAY_SPEED;
        public ForgeConfigSpec.ConfigValue<Integer> DECAY_SPEED_FOR_PLAYER;
        public ForgeConfigSpec.ConfigValue<Double> GAIN_SPEED_FOR_PLAYER;
        public ForgeConfigSpec.ConfigValue<List<String>> ITEM_RADIATION;
        public ForgeConfigSpec.ConfigValue<List<String>> RADIATION_REMOVAL_ITEMS;
        public ForgeConfigSpec.ConfigValue<List<String>> ARMOR_PROTECTION;
        public ForgeConfigSpec.ConfigValue<List<String>> BIOME_RADIATION;
        public ForgeConfigSpec.ConfigValue<List<String>> DIMENSION_RADIATION;
        public ForgeConfigSpec.ConfigValue<Integer> RADIATION_UPDATE_INTERVAL;
        public ForgeConfigSpec.ConfigValue<Boolean> MEKANISM_RADIATION_INTEGRATION;
        protected HashMap<String, Integer> biomeRadiationMap;
        public int biomeRadiation(String id)
        {
            if(biomeRadiationMap == null) {
                biomeRadiationMap = new HashMap<>();
                for(String line: BIOME_RADIATION.get()) {
                    String[] split = line.split("\\|");
                    if(split.length != 2) {
                        continue;
                    }
                    biomeRadiationMap.put(split[0].trim(), Integer.parseInt(split[1].trim()));
                }
            }
            if(biomeRadiationMap.containsKey(id)) {
                return biomeRadiationMap.get(id);
            }
            return 0;
        }

        protected HashMap<String, Integer> dimensionRadiationMap;
        public int dimensionRadiation(String id)
        {
            if(dimensionRadiationMap == null) {
                dimensionRadiationMap = new HashMap<>();
                for(String line: DIMENSION_RADIATION.get()) {
                    String[] split = line.split("\\|");
                    if(split.length != 2) {
                        continue;
                    }
                    dimensionRadiationMap.put(split[0].trim(), Integer.parseInt(split[1].trim()));
                }
            }
            if(dimensionRadiationMap.containsKey(id)) {
                return dimensionRadiationMap.get(id);
            }
            return 0;
        }

        public RadiationConf(ForgeConfigSpec.Builder builder) {
            builder.comment("Settings for Radiation").push("radiation");

            ENABLED = builder
                    .comment("If radiation is enabled.")
                    .define("enabled", true);

            NATURAL_RADIATION = builder
                    .comment("General background radiation everywhere (pRad).","Total radiation = background_radiation + dimension_radiation + chunk_radiation + in-game exposure")
                    .defineInRange("background_radiation", 50, 0, 10000);

            SPREAD_MULTIPLIER = builder
                    .comment("Spread multiplier. How much radiation spreads from chunk to chunk per simulation. Bigger values might cause lag.")
                    .defineInRange("spread_multiplier", 0.3d, 0.01d, 0.9d);

            SPREAD_GATE = builder
                    .comment("If chunk radiation (uRad) less than this value it won't affect chunks nearby.", "Bigger values - less lag, but less accurate radiation spread.")
                    .defineInRange("spread_gate", 1000, 100, 100000);

            DECAY_SPEED = builder
                    .comment("How fast contamination decays (pRad/s).")
                    .defineInRange("decay_speed", 1500, 1000, 10000);

            DECAY_SPEED_FOR_PLAYER = builder
                    .comment("How fast contamination decays in player's body (uRad/s).")
                    .defineInRange("decay_speed_for_player", 50, 1, 5000);

            GAIN_SPEED_FOR_PLAYER = builder
                    .comment("Rate at which player gets radiation dose.")
                    .defineInRange("gain_speed_for_player", 0.015D, 0, 5D);

            RADIATION_REMOVAL_ITEMS = builder
                    .comment("List of items what cleans player radiation when used (pRad). Format: item_id|radiation")
                    .define("radiation_removal_items", List.of(
                            "minecraft:golden_carrot|50000000",
                            "minecraft:golden_apple|200000000",
                            "minecraft:enchanted_golden_apple|5000000000",
                            "nuclearcraft:dominos|500000000",
                            "nuclearcraft:moresmore|5000000000",
                            "nuclearcraft:evenmoresmore|2000000000",
                            "nuclearcraft:radaway|10000000000"
                    ), o -> o instanceof ArrayList);

            ITEM_RADIATION = builder
                    .comment("List of items what have radiation (pRad). Format: item_id|radiation")
                    .define("items_radiation", List.of(
                            "nuclearcraft:spaxelhoe_thorium|50000",
                            "mekanism:pellet_polonium|4000000",
                            "mekanism:pellet_plutonium|2500000",
                            "mekanism:reprocessed_fissile_fragment|1800000"
                    ), o -> o instanceof ArrayList);

            ARMOR_PROTECTION = builder
                    .comment("List of armor items and default shielding lvl. Format: item_id|radiation")
                    .define("armor_shielding", List.of(
                            "mekanism:hazmat_mask|3",
                            "mekanism:hazmat_gown|5",
                            "mekanism:hazmat_pants|4",
                            "mekanism:hazmat_boots|3",
                            "mekanism:mekasuit_helmet|5",
                            "mekanism:mekasuit_bodyarmor|5",
                            "mekanism:mekasuit_pants|5",
                            "mekanism:mekasuit_boots|5",
                            "nuclearcraft:hazmat_mask|3",
                            "nuclearcraft:hazmat_chest|5",
                            "nuclearcraft:hazmat_pants|4",
                            "nuclearcraft:hazmat_boots|3",
                            "nuclearcraft:hev_helmet|5",
                            "nuclearcraft:hev_chest|7",
                            "nuclearcraft:hev_pants|6",
                            "nuclearcraft:hev_boots|5"
                    ), o -> o instanceof ArrayList);

            BIOME_RADIATION = builder
                    .comment("Natural radiation per biome: uRad", "Format: biome_id|radiation")
                    .define("biome_radiation", List.of("nuclearcraft:wasteland|2000", "minecraft:nether_wastes|500"), o -> o instanceof ArrayList);

            DIMENSION_RADIATION = builder
                    .comment("Natural radiation per dimension: uRad", "Format: dim_id|radiation")
                    .define("dimension_radiation", List.of("nuclearcraft:wasteland|200000", "minecraft:the_nether|1000"), o -> o instanceof ArrayList);

            RADIATION_UPDATE_INTERVAL = builder
                    .comment("Interval between radiation updates in ticks. 20 ticks = 1 second.", "Bigger interval - less lag, but less accurate radiation spread.")
                    .defineInRange("update_interval", 40, 2, 1000);

            MEKANISM_RADIATION_INTEGRATION = builder
                    .comment(
                            "NC radiation sources will generate mekanism radiation and wise-versa.",
                            "You can disable mekanism radiation, but radiation sources in mekanism still will generate NC radiation.",
                            "You can disable NC radiation, but NC radiation sources still will generate mekanism radiation.")
                    .define("mekanism_radiation_integration", true);

            builder.pop();
        }
    }
}