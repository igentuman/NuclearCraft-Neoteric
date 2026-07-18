package igentuman.nc.world.ore;

import igentuman.nc.content.materials.Materials;
import igentuman.nc.content.materials.NCMaterial;
import igentuman.nc.setup.registration.NCBlocks;
import igentuman.nc.content.materials.Ores;
import net.minecraft.core.Holder;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.placement.*;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;


public class OreGenerator {

    public static List<PlacementModifier> orePlacement(PlacementModifier modifier, PlacementModifier modifier1) {
        return List.of(modifier, InSquarePlacement.spread(), modifier1, BiomeFilter.biome());
    }

    public static List<PlacementModifier> commonOrePlacement(int pCount, PlacementModifier pHeightRange) {
        return orePlacement(CountPlacement.of(pCount), pHeightRange);
    }

    public static List<PlacementModifier> rareOrePlacement(int pChance, PlacementModifier pHeightRange) {
        return orePlacement(RarityFilter.onAverageOnceEvery(pChance), pHeightRange);
    }

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

    @NotNull
    public static PlacedFeature createOregenForMaterial(String materialName) {
        NCMaterial material = Materials.ores().get(materialName);
        List<OreConfiguration.TargetBlockState> targets = new ArrayList<>();

        if (material.normal_ore && NCBlocks.ORE_BLOCKS.containsKey(materialName)) {
            targets.add(OreConfiguration.target(
                new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES),
                NCBlocks.ORE_BLOCKS.get(materialName).get().defaultBlockState()
            ));
        }
        if (material.deepslate_ore && NCBlocks.ORE_BLOCKS.containsKey(materialName + "_deepslate")) {
            targets.add(OreConfiguration.target(
                new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES),
                NCBlocks.ORE_BLOCKS.get(materialName + "_deepslate").get().defaultBlockState()
            ));
        }
        if (material.nether_ore && NCBlocks.ORE_BLOCKS.containsKey(materialName + "_nether")) {
            targets.add(OreConfiguration.target(
                new TagMatchTest(BlockTags.NETHER_CARVER_REPLACEABLES),
                NCBlocks.ORE_BLOCKS.get(materialName + "_nether").get().defaultBlockState()
            ));
        }

        NCOre ore = Ores.all().get(materialName);
        return new PlacedFeature(
            Holder.hackyErase(Holder.direct(new ConfiguredFeature<>(Feature.ORE, new OreConfiguration(targets, ore.config().veinSize)))),
            List.of(
                CountPlacement.of(ore.config().veinAmount),
                InSquarePlacement.spread(),
                new DimensionBiomeFilter(key -> ore.config().dimensions.contains(key.location().toString())),
                HeightRangePlacement.uniform(
                    VerticalAnchor.absolute(ore.config().height[0]),
                    VerticalAnchor.absolute(ore.config().height[1])
                )
            )
        );
    }

}
