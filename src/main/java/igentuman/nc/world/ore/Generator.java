package igentuman.nc.world.ore;

import igentuman.nc.setup.registration.NCBlocks;
import igentuman.nc.content.materials.Ores;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.data.worldgen.features.OreFeatures;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.placement.*;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static igentuman.nc.NuclearCraft.MODID;

public class Generator {
    @NotNull
    public static PlacedFeature createOregen(String ore) {
            String materialName = ore.replaceAll("_deepslate|_nether|_end", "");
            RuleTest test = new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES);
            if(ore.contains("deepslate")) {
                test = new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES);
            } else if(ore.contains("nether")) {
                test = new TagMatchTest(BlockTags.NETHER_CARVER_REPLACEABLES);
            }
            OreConfiguration config = new OreConfiguration(
                    test,
                    NCBlocks.ORE_BLOCKS.get(ore).get().defaultBlockState(),
                    Ores.all().get(materialName).config().veinSize
            );
            return createPlacedFeature(new ConfiguredFeature<>(Feature.ORE, config),
                    CountPlacement.of(Ores.all().get(materialName).config().veinAmount),
                    InSquarePlacement.spread(),
                    new DimensionBiomeFilter(key -> Ores.all().get(materialName).config().dimensions.contains(key)),
                    HeightRangePlacement.uniform(
                            VerticalAnchor.absolute(Ores.all().get(materialName).config().height[0]),
                            VerticalAnchor.absolute(Ores.all().get(materialName).config().height[1])));

    }

    private static PlacedFeature createPlacedFeature(ConfiguredFeature<OreConfiguration, Feature<OreConfiguration>> oreConfigurationFeatureConfiguredFeature, CountPlacement of, InSquarePlacement spread, DimensionBiomeFilter dimensionBiomeFilter, HeightRangePlacement uniform) {
        return new PlacedFeature(Holder.direct(oreConfigurationFeatureConfiguredFeature), List.of(of, spread, dimensionBiomeFilter, uniform));
    }

    private static ResourceKey<PlacedFeature> registerKey(String name) {
        return ResourceKey.create(Registries.PLACED_FEATURE, new ResourceLocation(MODID, name));
    }

    private static void register(BootstapContext<PlacedFeature> context, ResourceKey<PlacedFeature> key, Holder<ConfiguredFeature<?, ?>> configuration,
                                 List<PlacementModifier> modifiers) {
        context.register(key, new PlacedFeature(configuration, List.copyOf(modifiers)));
    }
}
