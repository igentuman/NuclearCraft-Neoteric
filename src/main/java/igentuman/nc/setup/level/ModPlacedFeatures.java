package igentuman.nc.setup.level;

import igentuman.nc.config.WorldGen;
import igentuman.nc.registration.MaterialEntry;
import igentuman.nc.registration.ModEntry;
import igentuman.nc.setup.ModEntries;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;

import java.util.List;

import static igentuman.nc.Main.rl;

public class ModPlacedFeatures {

    private static ResourceKey<PlacedFeature> registerKey(String name) {
        return ResourceKey.create(Registries.PLACED_FEATURE, rl(name));
    }

    public static void register(BootstrapContext<PlacedFeature> context, ResourceKey<PlacedFeature> key, Holder<ConfiguredFeature<?, ?>> configuration, List<PlacementModifier> modifiers) {
        context.register(key, new PlacedFeature(configuration, modifiers));
    }

    public static void bootstrap(BootstrapContext<PlacedFeature> context) {
        HolderGetter<ConfiguredFeature<?, ?>> configuredFeatures = context.lookup(Registries.CONFIGURED_FEATURE);

        for (ModEntry entry : ModEntries.ENTRIES.values()) {
            if (entry.materialEntry() == null || !entry.materialEntry().hasWorldgenConfig()) continue;
            MaterialEntry mat = entry.materialEntry();
            ResourceKey<ConfiguredFeature<?, ?>> cfKey = ResourceKey.create(Registries.CONFIGURED_FEATURE, rl(mat.name + "_ore"));
            WorldGen.OreGenConfig cfg = WorldGen.ORE_CONFIGS.get(mat.name);
            int veinsPerChunk = (cfg != null && WorldGen.SPEC != null && WorldGen.SPEC.isLoaded())
                ? cfg.veinsPerChunk().get() : 4;
            register(context, registerKey(mat.name + "_ore"), configuredFeatures.getOrThrow(cfKey), List.of(
                CountPlacement.of(veinsPerChunk),
                InSquarePlacement.spread(),
                ConfigurableOrePlacement.forMaterial(mat.name),
                BiomeFilter.biome()
            ));
        }
    }
}
