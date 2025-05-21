package igentuman.nc.world.biome;

import net.minecraft.core.HolderGetter;
import net.minecraft.data.worldgen.BiomeDefaultFeatures;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.data.worldgen.placement.VegetationPlacements;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import static igentuman.nc.setup.registration.WorldGeneration.WASTELAND_BIOME;

public class WastelandBiome {

    public static void bootstrap(BootstapContext<Biome> context) {
        HolderGetter<PlacedFeature> placedFeatures = context.lookup(net.minecraft.core.registries.Registries.PLACED_FEATURE);
        HolderGetter<ConfiguredWorldCarver<?>> worldCarvers = context.lookup(net.minecraft.core.registries.Registries.CONFIGURED_CARVER);
        // Register the wasteland biome
        context.register(WASTELAND_BIOME, createWastelandBiome(placedFeatures, worldCarvers));
    }

    private static Biome createWastelandBiome(HolderGetter<PlacedFeature> placedFeatures, HolderGetter<ConfiguredWorldCarver<?>> worldCarvers) {
        // Create biome generation settings builder
        BiomeGenerationSettings.Builder biomeBuilder = new BiomeGenerationSettings.Builder(placedFeatures, worldCarvers);
        //biomeBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, PLACED_FEATURES.get("glowing_mushroom_wasteland"));
        BiomeDefaultFeatures.addDefaultCarversAndLakes(biomeBuilder);
        BiomeDefaultFeatures.addDefaultCrystalFormations(biomeBuilder);
        BiomeDefaultFeatures.addDefaultMonsterRoom(biomeBuilder);
        BiomeDefaultFeatures.addDefaultUndergroundVariety(biomeBuilder);

        BiomeDefaultFeatures.addDefaultOres(biomeBuilder);
        BiomeDefaultFeatures.addDefaultSoftDisks(biomeBuilder);

        biomeBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_DEAD_BUSH);

        MobSpawnSettings.Builder spawnBuilder = new MobSpawnSettings.Builder();

        return new Biome.BiomeBuilder()
                .temperature(1.5F)  // Hot temperature
                .downfall(0.2F)     // No rainfall
                .hasPrecipitation(false)
                .specialEffects(new BiomeSpecialEffects.Builder()
                        .waterColor(9145959)
                        .waterFogColor(4014888)
                        .fogColor(6387011)
                        .skyColor(11252336)
                        .grassColorOverride(2171424)
                        .foliageColorOverride(3882286)
                        .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                        .build())
                .mobSpawnSettings(spawnBuilder.build())
                .generationSettings(biomeBuilder.build())
                .build();
    }

    public static Biome create() {
        // This method is a simplified version for biome registration
        BiomeGenerationSettings.PlainBuilder builder = new BiomeGenerationSettings.PlainBuilder();
        return new Biome.BiomeBuilder()
                .temperature(1.5F)  // Hot temperature
                .downfall(0.2F)     // No rainfall
                .hasPrecipitation(false)
                .specialEffects(new BiomeSpecialEffects.Builder()
                        .waterColor(9145959)
                        .waterFogColor(4014888)
                        .fogColor(6387011)
                        .skyColor(11252336)
                        .grassColorOverride(2171424)
                        .foliageColorOverride(3882286)
                        .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                        .build())
                .mobSpawnSettings(new MobSpawnSettings.Builder().build())
                .generationSettings(
                        builder
                        .build())
                .build();
    }
}
