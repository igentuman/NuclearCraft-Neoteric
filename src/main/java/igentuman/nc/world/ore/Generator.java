package igentuman.nc.world.ore;

import igentuman.nc.content.materials.Ores;
import igentuman.nc.setup.registration.NCBlocks;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.OreFeature;
import net.minecraft.world.gen.feature.OreFeatureConfig;
import net.minecraft.world.gen.placement.Placement;
import net.minecraft.world.gen.placement.TopSolidRangeConfig;
import net.minecraftforge.common.Tags;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import org.antlr.v4.runtime.misc.NotNull;

import java.util.Arrays;
import java.util.List;

import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.setup.Registration.ORE_GENERATION;
import static net.minecraft.util.registry.WorldGenRegistries.CONFIGURED_FEATURE;
import static net.minecraft.world.biome.Biome.Category.NETHER;
import static net.minecraft.world.biome.Biome.Category.THEEND;
import static net.minecraft.world.gen.GenerationStage.Decoration.UNDERGROUND_ORES;

public class Generator {
    @NotNull
    public static ConfiguredFeature<?, ?> createOregen(String ore) {
            String materialName = ore.replaceAll("_deepslate|_nether|_end", "");
        ConfiguredFeature<?, ?> oreGen = Feature.ORE.configured(
                new OreFeatureConfig(
                        OreFeatureConfig.FillerBlockType.NATURAL_STONE,
                                NCBlocks.ORE_BLOCKS.get(ore).get().defaultBlockState(),
                        Ores.all().get(materialName).config().veinSize))
                .decorated(Placement.RANGE.configured(new TopSolidRangeConfig(Ores.all().get(materialName).config().height[0],
                        11, Ores.all().get(materialName).config().height[1])))
                .squared().count(Ores.all().get(materialName).config().veinAmount);
        return oreGen;
    }


    public static void onBiomeLoadingEvent(BiomeLoadingEvent event) {
        if (event.getCategory() == NETHER) {
            for(ConfiguredFeature<?, ?> feature: ORE_GENERATION) {
                event.getGeneration().addFeature(UNDERGROUND_ORES, feature);
            }
        } else if (event.getCategory() == THEEND) {
            for(ConfiguredFeature<?, ?> feature: ORE_GENERATION) {
                event.getGeneration().addFeature(UNDERGROUND_ORES, feature);
            }
        } else {
            for(ConfiguredFeature<?, ?> feature: ORE_GENERATION) {
                event.getGeneration().addFeature(GenerationStage.Decoration.UNDERGROUND_ORES, feature);
            }
        }
    }
}
