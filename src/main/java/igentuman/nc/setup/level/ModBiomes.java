package igentuman.nc.setup.level;

import com.mojang.datafixers.util.Pair;
import igentuman.nc.mixin.MultiNoiseBiomeSourceParameterListAccessor;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BiomeDefaultFeatures;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.placement.VegetationPlacements;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.Music;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.util.ARGB;
import net.minecraft.world.attribute.AmbientSounds;
import net.minecraft.world.attribute.BackgroundMusic;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterLists;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;

import igentuman.nc.setup.NCSounds;
import igentuman.nc.setup.entries.Ghouls;

import static igentuman.nc.NuclearCraft.rl;

/** Datapack bootstrap hook for the mod's custom biomes. */
public class ModBiomes {

    public static final ResourceKey<Biome> WASTELAND = ResourceKey.create(Registries.BIOME, rl("wasteland"));
    public static final TagKey<Biome> WASTELAND_TAG = TagKey.create(Registries.BIOME, rl("wasteland"));
    /** Overworld plus Wasteland biomes that receive ore features. */
    public static final TagKey<Biome> ORE_BIOMES_TAG = TagKey.create(Registries.BIOME, rl("has_ores"));

    public static void addToOverworld(ServerAboutToStartEvent event) {
        var registries = event.getServer().registryAccess();
        Holder<Biome> wasteland = registries.lookupOrThrow(Registries.BIOME).get(WASTELAND).orElse(null);
        var overworld = registries.lookupOrThrow(Registries.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST)
                .get(MultiNoiseBiomeSourceParameterLists.OVERWORLD).orElse(null);
        if (wasteland == null || overworld == null) return;

        // The vanilla builder's driest humidity band and the coast/inland boundary.
        long dryHumidity = Climate.quantizeCoord(-0.35F);
        long inlandContinentalness = Climate.quantizeCoord(-0.11F);
        var parameters = overworld.value().parameters().values().stream().map(entry -> {
            Climate.ParameterPoint climate = entry.getFirst();
            if (entry.getSecond().is(Biomes.DESERT) && climate.humidity().max() <= dryHumidity
                    && climate.continentalness().min() >= inlandContinentalness) {
                return Pair.of(climate, wasteland);
            }
            return entry;
        }).toList();
        // Both server types fire the event before creating levels or generating spawn chunks.
        ((MultiNoiseBiomeSourceParameterListAccessor) overworld.value())
                .nc$setParameters(new Climate.ParameterList<>(parameters));
    }

    public static void bootstrap(BootstrapContext<Biome> context) {
        HolderGetter<PlacedFeature> placedFeatures = context.lookup(Registries.PLACED_FEATURE);
        HolderGetter<ConfiguredWorldCarver<?>> worldCarvers = context.lookup(Registries.CONFIGURED_CARVER);
        context.register(WASTELAND, wasteland(placedFeatures, worldCarvers));
    }

    private static Biome wasteland(HolderGetter<PlacedFeature> placedFeatures, HolderGetter<ConfiguredWorldCarver<?>> worldCarvers) {
        BiomeGenerationSettings.Builder generation = new BiomeGenerationSettings.Builder(placedFeatures, worldCarvers);
        BiomeDefaultFeatures.addDefaultCarversAndLakes(generation);
        BiomeDefaultFeatures.addDefaultCrystalFormations(generation);
        BiomeDefaultFeatures.addDefaultMonsterRoom(generation);
        BiomeDefaultFeatures.addDefaultUndergroundVariety(generation);
        BiomeDefaultFeatures.addDefaultOres(generation);
        BiomeDefaultFeatures.addDefaultSoftDisks(generation);
        generation.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_DEAD_BUSH);

        MobSpawnSettings.Builder spawns = new MobSpawnSettings.Builder();
        spawns.addSpawn(MobCategory.MONSTER, 50, new MobSpawnSettings.SpawnerData(EntityType.SPIDER, 1, 4));
        spawns.addSpawn(MobCategory.MONSTER, 80, new MobSpawnSettings.SpawnerData(Ghouls.FERAL_GHOUL.get(), 1, 3));

        return new Biome.BiomeBuilder()
                .temperature(1.5F)
                .downfall(0.2F)
                .hasPrecipitation(false)
                .specialEffects(new BiomeSpecialEffects.Builder()
                        .waterColor(9145959)
                        .grassColorOverride(10724473)
                        .foliageColorOverride(9609331)
                        .build())
                .setAttribute(EnvironmentAttributes.FOG_COLOR, ARGB.opaque(6387011))
                .setAttribute(EnvironmentAttributes.SKY_COLOR, ARGB.opaque(11252336))
                .setAttribute(EnvironmentAttributes.WATER_FOG_COLOR, ARGB.opaque(6777172))
                .setAttribute(EnvironmentAttributes.AMBIENT_SOUNDS, AmbientSounds.LEGACY_CAVE_SETTINGS)
                .setAttribute(EnvironmentAttributes.BACKGROUND_MUSIC,
                        new BackgroundMusic(new Music(NCSounds.MUSIC_WASTELAND, 12000, 24000, false)))
                .mobSpawnSettings(spawns.build())
                .generationSettings(generation.build())
                .build();
    }
}
