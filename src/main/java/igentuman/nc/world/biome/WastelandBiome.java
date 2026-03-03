package igentuman.nc.world.biome;

import net.minecraft.core.HolderGetter;
import net.minecraft.data.worldgen.BiomeDefaultFeatures;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.placement.VegetationPlacements;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import static igentuman.nc.setup.registration.Entities.FERAL_GHOUL;
import static igentuman.nc.setup.registration.WorldGeneration.WASTELAND_BIOME;
import static igentuman.nc.world.NCPlacedFeatures.PLACED_FEATURES_KEYS;

public class WastelandBiome {

    public static void bootstrap(BootstrapContext<Biome> context) {
        HolderGetter<PlacedFeature> placedFeatures = context.lookup(net.minecraft.core.registries.Registries.PLACED_FEATURE);
        HolderGetter<ConfiguredWorldCarver<?>> worldCarvers = context.lookup(net.minecraft.core.registries.Registries.CONFIGURED_CARVER);
        context.register(WASTELAND_BIOME, createWastelandBiome(placedFeatures, worldCarvers));
    }

    private static Biome createWastelandBiome(HolderGetter<PlacedFeature> placedFeatures, HolderGetter<ConfiguredWorldCarver<?>> worldCarvers) {
        BiomeGenerationSettings.Builder biomeBuilder = new BiomeGenerationSettings.Builder(placedFeatures, worldCarvers);
        BiomeDefaultFeatures.addDefaultCarversAndLakes(biomeBuilder);
        BiomeDefaultFeatures.addDefaultCrystalFormations(biomeBuilder);
        BiomeDefaultFeatures.addDefaultMonsterRoom(biomeBuilder);
        BiomeDefaultFeatures.addDefaultUndergroundVariety(biomeBuilder);

        BiomeDefaultFeatures.addDefaultOres(biomeBuilder);
        BiomeDefaultFeatures.addDefaultSoftDisks(biomeBuilder);

        biomeBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_DEAD_BUSH);
        biomeBuilder.addFeature(GenerationStep.Decoration.TOP_LAYER_MODIFICATION, PLACED_FEATURES_KEYS.get("wasteland_surface"));

        MobSpawnSettings.Builder spawnBuilder = new MobSpawnSettings.Builder();
        MobSpawnSettings.SpawnerData ghoul = new MobSpawnSettings.SpawnerData(FERAL_GHOUL.get(), 100, 1, 7);
        MobSpawnSettings.SpawnerData spider = new MobSpawnSettings.SpawnerData(EntityType.SPIDER, 50, 1, 4);
        spawnBuilder.addSpawn(MobCategory.MONSTER, ghoul);
        spawnBuilder.addSpawn(MobCategory.MONSTER, spider);
        return new Biome.BiomeBuilder()
                .temperature(1.5F)  // Hot temperature
                .downfall(0.2F)     // No rainfall
                .hasPrecipitation(false)
                .specialEffects(new BiomeSpecialEffects.Builder()
                        .waterColor(9145959)
                        .waterFogColor(6777172)
                        .fogColor(6387011)
                        .skyColor(11252336)
                        .grassColorOverride(10724473)
                        .foliageColorOverride(9609331)
                        .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                        .build())
                .mobSpawnSettings(spawnBuilder.build())
                .generationSettings(biomeBuilder.build())
                .build();
    }

    public static Biome create() {
        BiomeGenerationSettings.PlainBuilder builder = new BiomeGenerationSettings.PlainBuilder();
        MobSpawnSettings.Builder spawnBuilder = new MobSpawnSettings.Builder();
        MobSpawnSettings.SpawnerData spawner = new MobSpawnSettings.SpawnerData(FERAL_GHOUL.get(), 100, 1, 7);
        spawnBuilder.addSpawn(MobCategory.MONSTER, spawner);
        return new Biome.BiomeBuilder()
                .temperature(1.5F)  // Hot temperature
                .downfall(0.2F)     // No rainfall
                .hasPrecipitation(false)
                .specialEffects(new BiomeSpecialEffects.Builder()
                        .waterColor(9145959)
                        .waterFogColor(6777172)
                        .fogColor(6387011)
                        .skyColor(11252336)
                        .grassColorOverride(10724473)
                        .foliageColorOverride(9609331)
                        .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                        .build())
                .mobSpawnSettings(spawnBuilder.build())
                .generationSettings(builder.build())
                .build();
    }
}
