package igentuman.nc.world;

import igentuman.nc.content.materials.Ores;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraftforge.registries.RegistryObject;

import java.util.HashMap;
import java.util.List;

import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.setup.registration.Registries.CONFIGURED;
import static igentuman.nc.world.structure.WastelandDecoFeature.WASTELAND_DECO_FEATURE;
import static igentuman.nc.world.structure.WastelandStructureFeature.WASTELAND_RUINS_FEATURE;

public class NCConfiguredFeatures {

    public static final HashMap<String, ResourceKey<ConfiguredFeature<?, ?>>> CONFIGURED_FEATURES = initFeatures();
    public static final RegistryObject<ConfiguredFeature<?, ?>> CONFIGURED_WASTELAND_RUINS = CONFIGURED.register("wasteland_ruins", () -> new ConfiguredFeature<>(WASTELAND_RUINS_FEATURE.get(), NoneFeatureConfiguration.INSTANCE));
    public static final RegistryObject<ConfiguredFeature<?, ?>> CONFIGURED_WASTELAND_DECO = CONFIGURED.register("wasteland_deco", () -> new ConfiguredFeature<>(WASTELAND_DECO_FEATURE.get(), NoneFeatureConfiguration.INSTANCE));

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
        features.put("wasteland_deco", registerKey("wasteland_deco"));
        features.put("wasteland_surface", registerKey("wasteland_surface"));
        features.put("wasteland_portal", registerKey("wasteland_portal"));
        features.put("wasteland_boss_lair", registerKey("wasteland_boss_lair"));
        return features;
    }

    public static void init() {}

    public static ResourceKey<ConfiguredFeature<?, ?>> registerKey(String name) {
        return ResourceKey.create(Registry.CONFIGURED_FEATURE_REGISTRY, rl(name));
    }
}
