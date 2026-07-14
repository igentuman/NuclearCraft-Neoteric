package igentuman.nc.setup.level;

import igentuman.nc.registration.ModEntry;
import igentuman.nc.setup.ModEntries;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.BiomeModifiers;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import static igentuman.nc.NuclearCraft.rl;

/** Datapack bootstrap that adds each material's ore placed feature to overworld biomes. */
public class ModBiomeModifiers {

    public static ResourceKey<BiomeModifier> registerKey(String name) {
        return ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, rl(name));
    }

    public static void bootstrap(BootstrapContext<BiomeModifier> context) {
        HolderGetter<PlacedFeature> placedFeatures = context.lookup(Registries.PLACED_FEATURE);
        HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);

        for (ModEntry entry : ModEntries.ENTRIES.values()) {
            if (entry.materialEntry() == null || !entry.materialEntry().hasWorldgenConfig()) continue;
            String name = entry.materialEntry().name;
            ResourceKey<PlacedFeature> pfKey = ResourceKey.create(Registries.PLACED_FEATURE, rl(name + "_ore"));
            context.register(registerKey(name + "_ore"), new BiomeModifiers.AddFeaturesBiomeModifier(
                biomes.getOrThrow(BiomeTags.IS_OVERWORLD),
                HolderSet.direct(placedFeatures.getOrThrow(pfKey)),
                GenerationStep.Decoration.UNDERGROUND_ORES
            ));
        }
    }
}
