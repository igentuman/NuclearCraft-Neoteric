package igentuman.nc.setup.level;

import igentuman.nc.setup.Registers;
import igentuman.nc.world.ConfigurableOreFeature;
import igentuman.nc.world.structure.WastelandScatterFeature;
import igentuman.nc.world.structure.WastelandTemplateFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.neoforged.neoforge.registries.DeferredHolder;

import static igentuman.nc.NuclearCraft.rl;

public final class ModFeatures {

    private ModFeatures() {
    }

    public static final DeferredHolder<Feature<?>, ConfigurableOreFeature> CONFIGURABLE_ORE =
            Registers.FEATURES.register("configurable_ore", ConfigurableOreFeature::new);

    public static final DeferredHolder<Feature<?>, WastelandScatterFeature> WASTELAND_RUINS =
            Registers.FEATURES.register("wasteland_ruins", () -> new WastelandScatterFeature(NoneFeatureConfiguration.CODEC, "wasteland/"));
    public static final DeferredHolder<Feature<?>, WastelandScatterFeature> WASTELAND_DECO =
            Registers.FEATURES.register("wasteland_deco", () -> new WastelandScatterFeature(NoneFeatureConfiguration.CODEC, "wasteland_deco/"));
    public static final DeferredHolder<Feature<?>, WastelandTemplateFeature> WASTELAND_PORTAL =
            Registers.FEATURES.register("wasteland_portal", () -> new WastelandTemplateFeature(NoneFeatureConfiguration.CODEC, rl("wasteland/portal_10")));
    public static final DeferredHolder<Feature<?>, WastelandTemplateFeature> WASTELAND_BOSS_LAIR =
            Registers.FEATURES.register("wasteland_boss_lair", () -> new WastelandTemplateFeature(NoneFeatureConfiguration.CODEC, rl("wasteland_boss_lair")));

    public static void init() {
    }
}
