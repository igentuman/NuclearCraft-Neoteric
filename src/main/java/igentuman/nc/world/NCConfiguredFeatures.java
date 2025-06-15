package igentuman.nc.world;

import igentuman.nc.content.materials.Ores;
import igentuman.nc.world.ore.NCOre;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.data.worldgen.features.FeatureUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.*;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockMatchTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.setup.registration.NCBlocks.MUSHROOM_BLOCK;
import static igentuman.nc.setup.registration.NCBlocks.WASTELAND_EARTH;
import static igentuman.nc.setup.registration.Registries.CONFIGURED;
import static igentuman.nc.world.structure.WastelandBossLairFeature.WASTELAND_BOSS_LAIR_FEATURE;
import static igentuman.nc.world.structure.WastelandPortalFeature.WASTELAND_PORTAL_FEATURE;
import static igentuman.nc.world.structure.WastelandStructureFeature.WASTELAND_RUINS_FEATURE;
import static net.minecraft.world.level.block.Blocks.*;

public class NCConfiguredFeatures {

    public static final HashMap<String, ResourceKey<ConfiguredFeature<?, ?>>> CONFIGURED_FEATURES = initFeatures();
    public static final RegistryObject<ConfiguredFeature<?, ?>> CONFIGURED_WASTELAND_RUINS = CONFIGURED.register("wasteland_ruins", () -> new ConfiguredFeature<>(WASTELAND_RUINS_FEATURE.get(), NoneFeatureConfiguration.INSTANCE));

    private static HashMap<String, ResourceKey<ConfiguredFeature<?,?>>> initFeatures() {
        HashMap<String, ResourceKey<ConfiguredFeature<?,?>>> features = new HashMap<>();
        for(String name: Ores.all().keySet()) {
            features.put(name, registerKey(name + "_ore"));
            features.put(name+"_wasteland", registerKey(name + "_wasteland_ore"));
        }
        for(String name: List.of("uranium", "thorium")) {
            features.put(name+"_additional_wasteland", registerKey(name + "_additional_wasteland_ore"));
        }
        features.put("glowing_mushroom", registerKey("glowing_mushroom_feature"));
        features.put("glowing_mushroom_wasteland", registerKey("glowing_mushroom_wasteland_feature"));
        features.put("wasteland_ruins", registerKey("wasteland_ruins"));
        features.put("wasteland_surface", registerKey("wasteland_surface"));
        features.put("wasteland_portal", registerKey("wasteland_portal"));
        features.put("wasteland_boss_lair", registerKey("wasteland_boss_lair"));
        return features;
    }

    public static void bootstrap(BootstapContext<ConfiguredFeature<?, ?>> context) {
        RuleTest stoneReplaceable = new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES);
        RuleTest deepslateReplaceables = new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES);
        RuleTest netherrackReplacables = new BlockMatchTest(Blocks.NETHERRACK);
        RuleTest endReplaceables = new BlockMatchTest(Blocks.END_STONE);

        for(String name: Ores.registered().keySet()) {
            NCOre ore = Ores.all().get(name);

            if(ore.dimensions.contains("minecraft:overworld")) {
                List<OreConfiguration.TargetBlockState> overworld = new ArrayList<>();
                if(ore.config().height[1] > 0) {
                    overworld.add(OreConfiguration.target(stoneReplaceable,
                            Ores.all().get(name).block().defaultBlockState()));
                }
                if(ore.config().height[0] < 0) {
                    overworld.add(OreConfiguration.target(deepslateReplaceables, ore.block("_deepslate").defaultBlockState()));
                }

                register(context, CONFIGURED_FEATURES.get(name), Feature.ORE, new OreConfiguration(overworld, 9));
                if(ore.config().dimensions.contains("nuclearcraft:wasteland")) {
                    register(context, CONFIGURED_FEATURES.get(name+"_wasteland"), Feature.ORE, new OreConfiguration(overworld, 2));
                }
            }

            if(ore.config().dimensions.contains("minecraft:nether")) {
                register(context, CONFIGURED_FEATURES.get(name), Feature.ORE, new OreConfiguration(netherrackReplacables,
                        ore.block().defaultBlockState(), 9));
            }

            if(ore.config().dimensions.contains("minecraft:the_end")) {
                register(context, CONFIGURED_FEATURES.get(name), Feature.ORE, new OreConfiguration(endReplaceables,
                        ore.block().defaultBlockState(), 9));
            }
        }
        for(String name: List.of("uranium", "thorium")) {
            NCOre ore = Ores.all().get(name);
            register(context, CONFIGURED_FEATURES.get(name+"_additional_wasteland"), Feature.ORE, new OreConfiguration(stoneReplaceable,
                    ore.block().defaultBlockState(), 9));
        }
        register(context, CONFIGURED_FEATURES.get("glowing_mushroom"), Feature.RANDOM_PATCH,
                FeatureUtils.simplePatchConfiguration(Feature.SIMPLE_BLOCK,
                        new SimpleBlockConfiguration(BlockStateProvider.simple(MUSHROOM_BLOCK.get())),
                        List.of(SOUL_SOIL, SOUL_SAND, GLOWSTONE)
                        )
        );
        register(context, CONFIGURED_FEATURES.get("glowing_mushroom_wasteland"), Feature.RANDOM_PATCH,
                FeatureUtils.simplePatchConfiguration(Feature.SIMPLE_BLOCK,
                        new SimpleBlockConfiguration(BlockStateProvider.simple(MUSHROOM_BLOCK.get())),
                        List.of(WASTELAND_EARTH.get())
                )
        );

        register(context, CONFIGURED_FEATURES.get("wasteland_ruins"), WASTELAND_RUINS_FEATURE.get(),
                new NoneFeatureConfiguration()
        );
        register(context, CONFIGURED_FEATURES.get("wasteland_portal"), WASTELAND_PORTAL_FEATURE.get(),
                new NoneFeatureConfiguration()
        );
        
        register(context, CONFIGURED_FEATURES.get("wasteland_boss_lair"), WASTELAND_BOSS_LAIR_FEATURE.get(),
                new NoneFeatureConfiguration()
        );

        register(context, CONFIGURED_FEATURES.get("wasteland_surface"), Feature.SIMPLE_BLOCK,
                new SimpleBlockConfiguration(BlockStateProvider.simple(WASTELAND_EARTH.get()))
        );
    }


    public static ResourceKey<ConfiguredFeature<?, ?>> registerKey(String name) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, rl(name));
    }

    private static <FC extends FeatureConfiguration, F extends Feature<FC>> void register(BootstapContext<ConfiguredFeature<?, ?>> context,
                                                                                          ResourceKey<ConfiguredFeature<?, ?>> key, F feature, FC configuration) {
        context.register(key, new ConfiguredFeature<>(feature, configuration));
    }
}
