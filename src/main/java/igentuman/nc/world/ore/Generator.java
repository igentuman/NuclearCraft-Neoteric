package igentuman.nc.world.ore;

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
        ConfiguredFeature<?, ?> glowstoneOre = Feature.ORE.configured(new OreConfiguration(List.of(
                        OreConfiguration.target(OreConfiguration.Predicates.STONE_ORE_REPLACEABLES,
                                Blocks.GLOWSTONE.defaultBlockState()),
                        OreConfiguration.target(OreConfiguration.Predicates.DEEPSLATE_ORE_REPLACEABLES,
                                Blocks.ACACIA_WOOD.defaultBlockState())),
                        11))
                .rangeUniform(VerticalAnchor.bottom(), VerticalAnchor.aboveBottom(20)).squared().count(100);
        return register("glowstone_ore", glowstoneOre);
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
