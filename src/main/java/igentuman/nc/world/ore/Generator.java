package igentuman.nc.world.ore;

import igentuman.nc.setup.registration.NCBlocks;
import igentuman.nc.content.materials.Ores;
import net.minecraft.core.Holder;
import net.minecraft.data.worldgen.features.OreFeatures;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.placement.*;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;
import net.minecraftforge.common.Tags;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static igentuman.nc.setup.Registration.ORE_GENERATION;
import static igentuman.nc.setup.Registration.PLACED_FEATURES;

public class Generator {
    public static final RuleTest IN_ENDSTONE = new TagMatchTest(Tags.Blocks.END_STONES);
    @NotNull
    public static Holder<PlacedFeature> createOregen(String ore) {
            String materialName = ore.replaceAll("_deepslate|_nether|_end", "");
            RuleTest test = OreFeatures.STONE_ORE_REPLACEABLES;
            if(ore.contains("end")) {
                test = IN_ENDSTONE;
            }
            if(ore.contains("deepslate")) {
                test = OreFeatures.DEEPSLATE_ORE_REPLACEABLES;
            } else if(ore.contains("nether")) {
                test = OreFeatures.NETHER_ORE_REPLACEABLES;
            }
            OreConfiguration config = new OreConfiguration(
                    test,
                    NCBlocks.ORE_BLOCKS.get(ore).get().defaultBlockState(),
                    Ores.all().get(materialName).config().veinSize
            );
            return createPlacedFeature(ore, new ConfiguredFeature<>(Feature.ORE, config),
                    CountPlacement.of(Ores.all().get(materialName).config().veinAmount),
                    InSquarePlacement.spread(),
                    BiomeFilter.biome(),
                    //new DimensionBiomeFilter(key -> Ores.all().get(materialName).config().dimensions.contains(key)),
                    HeightRangePlacement.uniform(
                            VerticalAnchor.absolute(Ores.all().get(materialName).config().height[0]),
                            VerticalAnchor.absolute(Ores.all().get(materialName).config().height[1])));

    }

    private static Holder<PlacedFeature> createPlacedFeature(String name, ConfiguredFeature<?, ?> feature, PlacementModifier... placementModifiers) {
        return PlacementUtils.register(name, Holder.hackyErase(Holder.direct(feature)), List.copyOf(List.of(placementModifiers)));
    }

    public static void onBiomeLoadingEvent(BiomeLoadingEvent event) {
        if (event.getCategory() == Biome.BiomeCategory.NETHER) {
            for(Holder<PlacedFeature> feature: ORE_GENERATION) {
                event.getGeneration().addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, feature);
            }
        } else if (event.getCategory() == Biome.BiomeCategory.THEEND) {
            for(Holder<PlacedFeature> feature: ORE_GENERATION) {
                event.getGeneration().addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, feature);
            }
        } else {
            for(Holder<PlacedFeature> feature: ORE_GENERATION) {
                event.getGeneration().addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, feature);
            }
        }
    }
}
