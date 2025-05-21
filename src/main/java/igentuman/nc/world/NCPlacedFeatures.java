package igentuman.nc.world;

import igentuman.nc.content.materials.Ores;
import igentuman.nc.world.ore.NCOre;
import igentuman.nc.world.ore.OreGenerator;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.*;

import java.util.HashMap;
import java.util.List;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.world.NCConfiguredFeatures.ORE_CONFIGURED_FEATURES;

public class NCPlacedFeatures {
    public static final HashMap<String, ResourceKey<PlacedFeature>> PLACED_FEATURES = initPlaceFeatures();

    private static HashMap<String, ResourceKey<PlacedFeature>> initPlaceFeatures() {
        HashMap<String, ResourceKey<PlacedFeature>> map = new HashMap<>();
        for(String name: Ores.all().keySet()) {
            map.put(name, registerKey(name + "_placed"));
        }
        map.put("glowing_mushroom", registerKey("glowing_mushroom_placed"));
        map.put("glowing_mushroom_wasteland", registerKey("glowing_mushroom_wasteland_placed"));
        return map;
    }

    public static void bootstrap(BootstapContext<PlacedFeature> context) {
        HolderGetter<ConfiguredFeature<?, ?>> configuredFeatures = context.lookup(Registries.CONFIGURED_FEATURE);

        for(String name: Ores.registered().keySet()) {
            NCOre ore = Ores.all().get(name);
            if(ore.dimensions.contains(0)) {
                register(context, PLACED_FEATURES.get(name), configuredFeatures.getOrThrow(ORE_CONFIGURED_FEATURES.get(name)),
                        OreGenerator.orePlacement(new OrePlacementModifier(name),
                                HeightRangePlacement.uniform(VerticalAnchor.absolute(ore.config().height[0]), VerticalAnchor.absolute(ore.config().height[1]))));
            }
            if(ore.dimensions.contains(-1)) {
                register(context, PLACED_FEATURES.get(name), configuredFeatures.getOrThrow(ORE_CONFIGURED_FEATURES.get(name)),
                        OreGenerator.orePlacement(new OrePlacementModifier(name),
                                HeightRangePlacement.uniform(VerticalAnchor.absolute(ore.config().config().height[0]), VerticalAnchor.absolute(ore.height[1]))));
            }

            if(ore.dimensions.contains(1)) {
                register(context, PLACED_FEATURES.get(name), configuredFeatures.getOrThrow(ORE_CONFIGURED_FEATURES.get(name)),
                        OreGenerator.orePlacement(new OrePlacementModifier(name),
                                HeightRangePlacement.uniform(VerticalAnchor.absolute(ore.config().config().height[0]), VerticalAnchor.absolute(ore.config().height[1]))));
            }
        }

        register(context, PLACED_FEATURES.get("glowing_mushroom"),
                configuredFeatures.getOrThrow(ORE_CONFIGURED_FEATURES.get("glowing_mushroom")),
                List.of(
                        RarityFilter.onAverageOnceEvery(2), InSquarePlacement.spread(), PlacementUtils.FULL_RANGE, BiomeFilter.biome()
                ));
        register(context, PLACED_FEATURES.get("glowing_mushroom_wasteland"),
                configuredFeatures.getOrThrow(ORE_CONFIGURED_FEATURES.get("glowing_mushroom_wasteland")),
                List.of(
                        RarityFilter.onAverageOnceEvery(3), InSquarePlacement.spread(), PlacementUtils.FULL_RANGE
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
