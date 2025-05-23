package igentuman.nc.world.biome;

import igentuman.nc.content.materials.Ores;
import igentuman.nc.world.ore.NCOre;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ForgeBiomeModifiers;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.List;

import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.setup.registration.WorldGeneration.WASTELAND;
import static igentuman.nc.world.NCPlacedFeatures.PLACED_FEATURES_KEYS;

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
        map.put("glowing_mushroom", registerKey("glowing_mushroom_biome_modifier"));
        map.put("glowing_mushroom_wasteland", registerKey("glowing_mushroom_wasteland_biome_modifier"));
        return map;
    }

    public static void bootstrap(BootstapContext<BiomeModifier> context) {
        var placedFeatures = context.lookup(Registries.PLACED_FEATURE);
        var biomes = context.lookup(Registries.BIOME);

        for(String name: Ores.registered().keySet()) {
            NCOre ore = Ores.all().get(name);
            if(ore.config().dimensions.contains(0)) {
                context.register(BIOME_MODIFIERS.get(name), new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                        biomes.getOrThrow(BiomeTags.IS_OVERWORLD),
                        HolderSet.direct(placedFeatures.getOrThrow(PLACED_FEATURES_KEYS.get(name))),
                        GenerationStep.Decoration.UNDERGROUND_ORES));
                context.register(BIOME_MODIFIERS.get(name+"_wasteland"), new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                        biomes.getOrThrow(WASTELAND),
                        HolderSet.direct(placedFeatures.getOrThrow(PLACED_FEATURES_KEYS.get(name+"_wasteland"))),
                        GenerationStep.Decoration.UNDERGROUND_ORES));
            }
            if(ore.config().dimensions.contains(-1)) {
                context.register(BIOME_MODIFIERS.get(name), new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                        biomes.getOrThrow(BiomeTags.IS_NETHER),
                        HolderSet.direct(placedFeatures.getOrThrow(PLACED_FEATURES_KEYS.get(name))),
                        GenerationStep.Decoration.UNDERGROUND_ORES));
            }

            if(ore.config().dimensions.contains(1)) {
                context.register(BIOME_MODIFIERS.get(name), new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                        biomes.getOrThrow(BiomeTags.IS_END),
                        HolderSet.direct(placedFeatures.getOrThrow(PLACED_FEATURES_KEYS.get(name))),
                        GenerationStep.Decoration.UNDERGROUND_ORES));
            }
        }

        for(String name: List.of("uranium", "thorium")) {
            context.register(BIOME_MODIFIERS.get(name+"_additional_wasteland"), new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                    biomes.getOrThrow(WASTELAND),
                    HolderSet.direct(placedFeatures.getOrThrow(PLACED_FEATURES_KEYS.get(name+"_additional_wasteland"))),
                    GenerationStep.Decoration.UNDERGROUND_ORES));
        }

        context.register(BIOME_MODIFIERS.get("glowing_mushroom"), new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                biomes.getOrThrow(BiomeTags.IS_NETHER),
                HolderSet.direct(placedFeatures.getOrThrow(PLACED_FEATURES_KEYS.get("glowing_mushroom"))),
                GenerationStep.Decoration.UNDERGROUND_DECORATION));

        context.register(BIOME_MODIFIERS.get("glowing_mushroom_wasteland"), new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                biomes.getOrThrow(WASTELAND),
                HolderSet.direct(placedFeatures.getOrThrow(PLACED_FEATURES_KEYS.get("glowing_mushroom_wasteland"))),
                GenerationStep.Decoration.VEGETAL_DECORATION));
    }

    private static ResourceKey<BiomeModifier> registerKey(String name) {
        return ResourceKey.create(ForgeRegistries.Keys.BIOME_MODIFIERS, rl(name));
    }
}
