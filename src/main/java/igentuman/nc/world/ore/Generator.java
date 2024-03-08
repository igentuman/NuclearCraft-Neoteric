package igentuman.nc.world.ore;

import igentuman.nc.content.materials.Ores;
import igentuman.nc.setup.registration.NCBlocks;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;
import net.minecraftforge.common.Tags;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import org.antlr.v4.runtime.misc.NotNull;;

import java.util.List;

import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.setup.Registration.ORE_GENERATION;

public class Generator {
    public static final RuleTest IN_ENDSTONE = new TagMatchTest(Tags.Blocks.END_STONES);
    @NotNull
    public static ConfiguredFeature<?, ?> createOregen(String ore) {
            String materialName = ore.replaceAll("_deepslate|_nether|_end", "");
        ConfiguredFeature<?, ?> oreGen = Feature.ORE.configured(new OreConfiguration(List.of(
                        OreConfiguration.target(OreConfiguration.Predicates.STONE_ORE_REPLACEABLES,
                                NCBlocks.ORE_BLOCKS.get(ore).get().defaultBlockState())),
                        Ores.all().get(materialName).config().veinSize))
                .rangeUniform(VerticalAnchor.absolute(Ores.all().get(materialName).config().height[0]),
                        VerticalAnchor.absolute(Ores.all().get(materialName).config().height[1]))
                .squared().count(Ores.all().get(materialName).config().veinAmount);
        return register(ore, oreGen);
    }

    private static <Config extends FeatureConfiguration> ConfiguredFeature<Config, ?> register(String name,
                                                                                               ConfiguredFeature<Config, ?> configuredFeature) {
        return Registry.register(BuiltinRegistries.CONFIGURED_FEATURE, rl(name),
                configuredFeature);
    }

    public static void onBiomeLoadingEvent(BiomeLoadingEvent event) {
        if (event.getCategory() == Biome.BiomeCategory.NETHER) {
            for(ConfiguredFeature<?, ?> feature: ORE_GENERATION) {
                event.getGeneration().addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, feature);
            }
        } else if (event.getCategory() == Biome.BiomeCategory.THEEND) {
            for(ConfiguredFeature<?, ?> feature: ORE_GENERATION) {
                event.getGeneration().addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, feature);
            }
        } else {
            for(ConfiguredFeature<?, ?> feature: ORE_GENERATION) {
                event.getGeneration().addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, feature);
            }
        }
    }
}
