package igentuman.nc.world;

import igentuman.nc.content.materials.Ores;
import igentuman.nc.world.ore.NCOre;
import igentuman.nc.world.ore.OreGenerator;
import igentuman.nc.world.placement.HeightmapChunkPlacement;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.*;
import net.minecraftforge.registries.RegistryObject;

import java.util.HashMap;
import java.util.List;

import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.setup.registration.Registries.PLACED_FEATURES;
import static igentuman.nc.world.NCConfiguredFeatures.CONFIGURED_FEATURES;
import static igentuman.nc.world.NCConfiguredFeatures.CONFIGURED_WASTELAND_RUINS;


public class NCPlacedFeatures {

    public static final HashMap<String, ResourceKey<PlacedFeature>> PLACED_FEATURES_KEYS = initPlaceFeatures();
    public static final RegistryObject<PlacedFeature> WASTELAND_RUINS_PLACED_FEATURE = PLACED_FEATURES.register("wasteland_ruins", () -> new PlacedFeature(CONFIGURED_WASTELAND_RUINS.getHolder().get(), List.of()));

    private static HashMap<String, ResourceKey<PlacedFeature>> initPlaceFeatures() {
        HashMap<String, ResourceKey<PlacedFeature>> map = new HashMap<>();
        for(String name: Ores.all().keySet()) {
            map.put(name, registerKey(name + "_placed"));
            map.put(name+"_wasteland", registerKey(name + "_wasteland_placed"));
        }
        for(String name: List.of("uranium", "thorium")) {
            map.put(name+"_additional_wasteland", registerKey(name + "_additional_wasteland_placed"));
        }
        map.put("glowing_mushroom", registerKey("glowing_mushroom_placed"));
        map.put("glowing_mushroom_wasteland", registerKey("glowing_mushroom_wasteland_placed"));
        map.put("wasteland_ruins", registerKey("wasteland_ruins"));
        map.put("wasteland_portal", registerKey("wasteland_portal"));
        map.put("wasteland_boss_lair", registerKey("wasteland_boss_lair"));
        map.put("wasteland_surface", registerKey("wasteland_surface"));
        return map;
    }

    public static void bootstrap(BootstapContext<PlacedFeature> context) {
        HolderGetter<ConfiguredFeature<?, ?>> configuredFeatures = context.lookup(Registries.CONFIGURED_FEATURE);

        for(String name: Ores.all().keySet()) {
            NCOre ore = Ores.all().get(name);
            if(ore.dimensions.contains("minecraft:overworld")) {
                register(context, PLACED_FEATURES_KEYS.get(name), configuredFeatures.getOrThrow(CONFIGURED_FEATURES.get(name)),
                        OreGenerator.orePlacement(new OrePlacementModifier(name),
                                HeightRangePlacement.uniform(VerticalAnchor.absolute(ore.config().height[0]), VerticalAnchor.absolute(ore.config().height[1]))));
                register(context, PLACED_FEATURES_KEYS.get(name+"_wasteland"), configuredFeatures.getOrThrow(CONFIGURED_FEATURES.get(name+"_wasteland")),
                        OreGenerator.orePlacement(new OrePlacementModifier(name),
                                HeightRangePlacement.uniform(VerticalAnchor.absolute(ore.config().height[0]), VerticalAnchor.absolute(ore.config().height[1]))));
            }
            if(ore.dimensions.contains("minecraft:nether")) {
                register(context, PLACED_FEATURES_KEYS.get(name), configuredFeatures.getOrThrow(CONFIGURED_FEATURES.get(name)),
                        OreGenerator.orePlacement(new OrePlacementModifier(name),
                                HeightRangePlacement.uniform(VerticalAnchor.absolute(ore.config().height[0]), VerticalAnchor.absolute(ore.config().height[1]))));
            }

            if(ore.dimensions.contains("minecraft:the_end")) {
                register(context, PLACED_FEATURES_KEYS.get(name), configuredFeatures.getOrThrow(CONFIGURED_FEATURES.get(name)),
                        OreGenerator.orePlacement(new OrePlacementModifier(name),
                                HeightRangePlacement.uniform(VerticalAnchor.absolute(ore.config().height[0]), VerticalAnchor.absolute(ore.config().height[1]))));
            }
        }
        for(String name: List.of("uranium", "thorium")) {
            NCOre ore = Ores.all().get(name);
            register(context, PLACED_FEATURES_KEYS.get(name+"_additional_wasteland"), configuredFeatures.getOrThrow(CONFIGURED_FEATURES.get(name+"_additional_wasteland")),
                    OreGenerator.orePlacement(new OrePlacementModifier(name),
                            HeightRangePlacement.uniform(VerticalAnchor.absolute(ore.config().height[0]), VerticalAnchor.absolute(ore.config().height[1]))));
        }

        register(context, PLACED_FEATURES_KEYS.get("glowing_mushroom"),
                configuredFeatures.getOrThrow(CONFIGURED_FEATURES.get("glowing_mushroom")),
                List.of(
                        RarityFilter.onAverageOnceEvery(2), InSquarePlacement.spread(), PlacementUtils.FULL_RANGE, BiomeFilter.biome()
                ));

        register(context, PLACED_FEATURES_KEYS.get("glowing_mushroom_wasteland"),
                configuredFeatures.getOrThrow(CONFIGURED_FEATURES.get("glowing_mushroom_wasteland")),
                List.of(
                        RarityFilter.onAverageOnceEvery(2), InSquarePlacement.spread(), PlacementUtils.FULL_RANGE
                ));

        register(context, PLACED_FEATURES_KEYS.get("wasteland_ruins"),
                configuredFeatures.getOrThrow(CONFIGURED_FEATURES.get("wasteland_ruins")),
                List.of(
                        RarityFilter.onAverageOnceEvery(250), HeightmapPlacement.onHeightmap(Heightmap.Types.WORLD_SURFACE_WG)
                ));

        register(context, PLACED_FEATURES_KEYS.get("wasteland_portal"),
                configuredFeatures.getOrThrow(CONFIGURED_FEATURES.get("wasteland_portal")),
                List.of(
                        RarityFilter.onAverageOnceEvery(400),  HeightmapPlacement.onHeightmap(Heightmap.Types.WORLD_SURFACE_WG)
                ));
                
        register(context, PLACED_FEATURES_KEYS.get("wasteland_boss_lair"),
                configuredFeatures.getOrThrow(CONFIGURED_FEATURES.get("wasteland_boss_lair")),
                List.of(
                        RarityFilter.onAverageOnceEvery(800),  HeightmapPlacement.onHeightmap(Heightmap.Types.WORLD_SURFACE_WG)
                ));

        register(context, PLACED_FEATURES_KEYS.get("wasteland_surface"),
                configuredFeatures.getOrThrow(CONFIGURED_FEATURES.get("wasteland_surface")),
                List.of(
                        HeightmapChunkPlacement.onHeightmap(Heightmap.Types.WORLD_SURFACE_WG)
                ));
    }


    private static ResourceKey<PlacedFeature> registerKey(String name) {
        return ResourceKey.create(Registries.PLACED_FEATURE, rl(name));
    }

    private static void register(BootstapContext<PlacedFeature> context, ResourceKey<PlacedFeature> key, Holder<ConfiguredFeature<?, ?>> configuration,
                                 List<PlacementModifier> modifiers) {
        context.register(key, new PlacedFeature(configuration, List.copyOf(modifiers)));
    }
}
