package igentuman.nc.world.ore;

import igentuman.nc.setup.NCBlocks;
import igentuman.nc.setup.materials.Ores;
import net.minecraft.core.Holder;
import net.minecraft.data.worldgen.features.OreFeatures;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.placement.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class Generator {
    @NotNull
    public static PlacedFeature createOregen(String ore) {

            OreConfiguration config = new OreConfiguration(OreFeatures.STONE_ORE_REPLACEABLES, NCBlocks.ORE_BLOCKS.get(ore).get().defaultBlockState(), Ores.registered().get(ore).config().veinSize);
            return createPlacedFeature(new ConfiguredFeature<>(Feature.ORE, config),
                    CountPlacement.of(Ores.registered().get(ore).config().veinAmount),
                    InSquarePlacement.spread(),
                    new DimensionBiomeFilter(key -> Ores.registered().get(ore).config().dimensions.contains(key)),
                    HeightRangePlacement.uniform(
                            VerticalAnchor.absolute(Ores.registered().get(ore).config().height[0]),
                            VerticalAnchor.absolute(Ores.registered().get(ore).config().height[1])));

    }

    private static <C extends FeatureConfiguration, F extends Feature<C>> PlacedFeature createPlacedFeature(ConfiguredFeature<C, F> feature, PlacementModifier... placementModifiers) {
        return new PlacedFeature(Holder.hackyErase(Holder.direct(feature)), List.copyOf(List.of(placementModifiers)));
    }
}
