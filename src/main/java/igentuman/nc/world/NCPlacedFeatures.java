package igentuman.nc.world;

import igentuman.nc.content.materials.Ores;
import igentuman.nc.world.ore.OreGenerator;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraftforge.registries.RegistryObject;

import java.util.HashMap;
import java.util.List;

import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.setup.registration.Registries.PLACED_FEATURES;
import static igentuman.nc.world.NCConfiguredFeatures.CONFIGURED_WASTELAND_DECO;
import static igentuman.nc.world.NCConfiguredFeatures.CONFIGURED_WASTELAND_RUINS;


public class NCPlacedFeatures {

    public static final HashMap<String, ResourceKey<PlacedFeature>> PLACED_FEATURES_KEYS = initPlaceFeatures();
    public static final RegistryObject<PlacedFeature> WASTELAND_RUINS_PLACED_FEATURE = PLACED_FEATURES.register("wasteland_ruins", () -> new PlacedFeature(CONFIGURED_WASTELAND_RUINS.getHolder().get(), List.of()));
    public static final RegistryObject<PlacedFeature> WASTELAND_DECO_PLACED_FEATURE = PLACED_FEATURES.register("wasteland_deco", () -> new PlacedFeature(CONFIGURED_WASTELAND_DECO.getHolder().get(), List.of()));

    private static HashMap<String, ResourceKey<PlacedFeature>> initPlaceFeatures() {
        HashMap<String, ResourceKey<PlacedFeature>> map = new HashMap<>();
        for(String name: Ores.all().keySet()) {
            map.put(name, registerKey(name + "_placed"));
            map.put(name+"_wasteland", registerKey(name + "_wasteland_placed"));
        }
        for(String name: List.of("uranium", "thorium")) {
            map.put(name+"_additional_wasteland", registerKey(name + "_additional_wasteland_placed"));
        }
        map.put("glowing_mushroom", registerKey("glowing_mushroom_placed"));
        map.put("glowing_mushroom_wasteland", registerKey("glowing_mushroom_wasteland_placed"));
        map.put("wasteland_ruins", registerKey("wasteland_ruins"));
        map.put("wasteland_deco", registerKey("wasteland_deco"));
        map.put("wasteland_portal", registerKey("wasteland_portal"));
        map.put("wasteland_boss_lair", registerKey("wasteland_boss_lair"));
        map.put("wasteland_surface", registerKey("wasteland_surface"));
        return map;
    }

    public static final HashMap<String, RegistryObject<PlacedFeature>> ORE_PLACED_FEATURES = new HashMap<>();

    public static void init() {
        for (String name : Ores.all().keySet()) {
            ORE_PLACED_FEATURES.put(name, PLACED_FEATURES.register(name + "_placed", () -> OreGenerator.createOregenForMaterial(name)));
        }
    }

    private static ResourceKey<PlacedFeature> registerKey(String name) {
        return ResourceKey.create(Registry.PLACED_FEATURE_REGISTRY, rl(name));
    }
}
