package igentuman.nc.world;

import igentuman.nc.content.materials.Ores;
import igentuman.nc.world.ore.NCOre;
import igentuman.nc.world.ore.OreGenerator;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ForgeBiomeModifiers;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.NuclearCraft.rl;

public class NCBiomeModifier {

    public static final HashMap<String, ResourceKey<BiomeModifier>> BIOME_MODIFIERS = initBiomeModifiers();

    private static HashMap<String, ResourceKey<BiomeModifier>> initBiomeModifiers() {
        HashMap<String, ResourceKey<BiomeModifier>> map = new HashMap<>();
        for(String name: Ores.all().keySet()) {
            map.put(name, registerKey(name + "_biome_modifier"));
        }
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
                        HolderSet.direct(placedFeatures.getOrThrow(NCPlacedFeatures.PLACED_FEATURES.get(name))),
                        GenerationStep.Decoration.UNDERGROUND_ORES));
            }
            if(ore.config().dimensions.contains(-1)) {
                context.register(BIOME_MODIFIERS.get(name), new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                        biomes.getOrThrow(BiomeTags.IS_NETHER),
                        HolderSet.direct(placedFeatures.getOrThrow(NCPlacedFeatures.PLACED_FEATURES.get(name))),
                        GenerationStep.Decoration.UNDERGROUND_ORES));
            }

            if(ore.config().dimensions.contains(1)) {
                context.register(BIOME_MODIFIERS.get(name), new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                        biomes.getOrThrow(BiomeTags.IS_END),
                        HolderSet.direct(placedFeatures.getOrThrow(NCPlacedFeatures.PLACED_FEATURES.get(name))),
                        GenerationStep.Decoration.UNDERGROUND_ORES));
            }
        }
    }
    private static ResourceKey<BiomeModifier> registerKey(String name) {
        return ResourceKey.create(ForgeRegistries.Keys.BIOME_MODIFIERS, rl(name));
    }
}
