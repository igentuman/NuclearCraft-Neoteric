package igentuman.nc.world.biome;

import net.minecraft.sounds.Music;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.*;

import static igentuman.nc.setup.registration.Entities.FERAL_GHOUL;
import static igentuman.nc.setup.registration.NCSounds.MUSIC_WASTELAND;

public class WastelandBiome {

    public static Biome create() {
        BiomeGenerationSettings.Builder builder = new BiomeGenerationSettings.Builder();
        MobSpawnSettings.Builder spawnBuilder = new MobSpawnSettings.Builder();
        MobSpawnSettings.SpawnerData spawner = new MobSpawnSettings.SpawnerData(FERAL_GHOUL.get(), 100, 1, 7);
        spawnBuilder.addSpawn(MobCategory.MONSTER, spawner);
        return new Biome.BiomeBuilder()
                .temperature(1.5F)  // Hot temperature
                .downfall(0.2F)     // No rainfall
                .precipitation(Biome.Precipitation.NONE)
                .specialEffects(new BiomeSpecialEffects.Builder()
                        .waterColor(9145959)
                        .waterFogColor(6777172)
                        .fogColor(6387011)
                        .skyColor(11252336)
                        .grassColorOverride(10724473)
                        .foliageColorOverride(9609331)
                        .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                        .backgroundMusic(new Music(MUSIC_WASTELAND.get(), 12000, 24000, false))
                        .build())
                .mobSpawnSettings(spawnBuilder.build())
                .generationSettings(builder.build())
                .build();
    }
}
