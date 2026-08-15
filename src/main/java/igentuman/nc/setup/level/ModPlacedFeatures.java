package igentuman.nc.setup.level;

import igentuman.nc.registration.MaterialEntry;
import igentuman.nc.registration.ModEntry;
import igentuman.nc.setup.ModEntries;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.RarityFilter;

import java.util.List;

import static igentuman.nc.NuclearCraft.rl;

/** Datapack bootstrap that registers placed ore features with config-driven counts and placement. */
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
            register(context, registerKey(mat.name + "_ore"), configuredFeatures.getOrThrow(cfKey), List.of(
                InSquarePlacement.spread(),
                ConfigurableOrePlacement.forMaterial(mat.name),
                BiomeFilter.biome()
            ));
        }

        scatter(context, configuredFeatures, "wasteland_ruins", 250);
        scatter(context, configuredFeatures, "wasteland_deco", 30);
        scatter(context, configuredFeatures, "wasteland_portal", 400);
        scatter(context, configuredFeatures, "wasteland_boss_lair", 800);
    }

    private static void scatter(BootstrapContext<PlacedFeature> context, HolderGetter<ConfiguredFeature<?, ?>> configuredFeatures, String name, int rarity) {
        ResourceKey<ConfiguredFeature<?, ?>> cfKey = ResourceKey.create(Registries.CONFIGURED_FEATURE, rl(name));
        register(context, registerKey(name), configuredFeatures.getOrThrow(cfKey), List.of(
                RarityFilter.onAverageOnceEvery(rarity),
                InSquarePlacement.spread(),
                PlacementUtils.HEIGHTMAP_WORLD_SURFACE,
                BiomeFilter.biome()
        ));
    }
}
