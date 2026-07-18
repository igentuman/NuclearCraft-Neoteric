package igentuman.nc.world.biome;

import igentuman.nc.content.materials.Ores;
import net.minecraft.resources.ResourceKey;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.List;

import static igentuman.nc.NuclearCraft.rl;

public class NCBiomeModifier {

    public static final HashMap<String, ResourceKey<BiomeModifier>> BIOME_MODIFIERS = initBiomeModifiers();

    private static HashMap<String, ResourceKey<BiomeModifier>> initBiomeModifiers() {
        HashMap<String, ResourceKey<BiomeModifier>> map = new HashMap<>();
        for(String name: Ores.all().keySet()) {
            map.put(name, registerKey(name + "_biome_modifier"));
            map.put(name + "_wasteland", registerKey(name + "wasteland_biome_modifier"));
        }
        for(String name: List.of("uranium", "thorium")) {
            map.put(name + "_additional_wasteland", registerKey(name + "_additional_wasteland_biome_modifier"));
        }
        map.put("wasteland_portal", registerKey("wasteland_portal"));
        map.put("wasteland_boss_lair", registerKey("wasteland_boss_lair"));
        map.put("glowing_mushroom", registerKey("glowing_mushroom_biome_modifier"));
        map.put("glowing_mushroom_wasteland", registerKey("glowing_mushroom_wasteland_biome_modifier"));
        return map;
    }

    private static ResourceKey<BiomeModifier> registerKey(String name) {
        return ResourceKey.create(ForgeRegistries.Keys.BIOME_MODIFIERS, rl(name));
    }
}
