package igentuman.nc.setup.level;

import igentuman.nc.registration.MaterialEntry;
import igentuman.nc.registration.ModEntry;
import igentuman.nc.setup.ModEntries;
import igentuman.nc.world.ConfigurableOreFeature;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;

import java.util.List;

import static igentuman.nc.NuclearCraft.rl;

/** Datapack bootstrap that registers configured ore features with config-driven vein sizes. */
public class ModConfiguredFeatures {

    private static ResourceKey<ConfiguredFeature<?, ?>> registerKey(String name) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, rl(name));
    }

    public static <FC extends FeatureConfiguration, F extends Feature<FC>> void register(BootstrapContext<ConfiguredFeature<?, ?>> context, ResourceKey<ConfiguredFeature<?, ?>> key, F feature, FC configuration) {
        context.register(key, new ConfiguredFeature<>(feature, configuration));
    }

    public static void bootstrap(BootstrapContext<ConfiguredFeature<?, ?>> context) {
        for (ModEntry entry : ModEntries.ENTRIES.values()) {
            if (entry.materialEntry() == null || !entry.materialEntry().hasWorldgenConfig()) continue;
            MaterialEntry mat = entry.materialEntry();
            List<OreConfiguration.TargetBlockState> targets = List.of(
                OreConfiguration.target(new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES), mat.oreBlock().get().defaultBlockState()),
                OreConfiguration.target(new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES), mat.oreBlock().get().defaultBlockState())
            );
            register(context, registerKey(mat.name + "_ore"), ModFeatures.CONFIGURABLE_ORE.get(),
                new ConfigurableOreFeature.Config(mat.name, targets, mat.worldgenQty));
        }

        registerNone(context, "wasteland_ruins", ModFeatures.WASTELAND_RUINS.get());
        registerNone(context, "wasteland_deco", ModFeatures.WASTELAND_DECO.get());
        registerNone(context, "wasteland_portal", ModFeatures.WASTELAND_PORTAL.get());
        registerNone(context, "wasteland_boss_lair", ModFeatures.WASTELAND_BOSS_LAIR.get());
    }

    private static void registerNone(BootstrapContext<ConfiguredFeature<?, ?>> context, String name, Feature<NoneFeatureConfiguration> feature) {
        context.register(registerKey(name), new ConfiguredFeature<>(feature, NoneFeatureConfiguration.INSTANCE));
    }
}
