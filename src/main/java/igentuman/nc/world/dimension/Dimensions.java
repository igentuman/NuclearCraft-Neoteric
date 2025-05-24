package igentuman.nc.world.dimension;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;

import java.util.List;
import java.util.OptionalLong;

import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.setup.registration.WorldGeneration.WASTELAND_BIOME;
import static igentuman.nc.world.biome.NCSurfaceRuleData.CUSTOM_OVERWORLD_NOISE_GEN;

public class Dimensions {

    public static final int WASTELAND_ID = -4848;
    public static final ResourceKey<Level> WASTELAND = ResourceKey.create(Registries.DIMENSION, rl("wasteland"));
    public static final ResourceKey<LevelStem> WASTELAND_KEY = ResourceKey.create(Registries.LEVEL_STEM, rl("wasteland"));
    public static final ResourceKey<DimensionType> WASTELAND_DIM_TYPE = ResourceKey.create(Registries.DIMENSION_TYPE, rl("wasteland_type"));

    public static void bootstrapType(BootstapContext<DimensionType> context) {
        // Register Wasteland dimension type with dynamic time like the Overworld
        context.register(WASTELAND_DIM_TYPE, new DimensionType(
                OptionalLong.empty(),
                true,
                false,
                true,
                false,
                1.0,
                true,
                false,
                0,
                256,
                256,
                BlockTags.INFINIBURN_OVERWORLD,
                BuiltinDimensionTypes.OVERWORLD_EFFECTS,
                0f,
                new DimensionType.MonsterSettings(false, false, UniformInt.of(0, 7), 0)));
    }

    public static void bootstrapStem(BootstapContext<LevelStem> context) {
        HolderGetter<Biome> biomeRegistry = context.lookup(Registries.BIOME);
        HolderGetter<DimensionType> dimTypes = context.lookup(Registries.DIMENSION_TYPE);
        HolderGetter<NoiseGeneratorSettings> noiseGenSettings = context.lookup(Registries.NOISE_SETTINGS);

        // Create Wasteland dimension
        NoiseBasedChunkGenerator wastelandGenerator = new NoiseBasedChunkGenerator(
                MultiNoiseBiomeSource.createFromList(
                        new Climate.ParameterList<>(List.of(Pair.of(
                                        Climate.parameters(1.2F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F), biomeRegistry.getOrThrow(WASTELAND_BIOME)),
                                Pair.of(
                                        Climate.parameters(1.9F, 0.2F, 0.0F, 0.2F, 0.0F, 0.0F, 0.0F), biomeRegistry.getOrThrow(Biomes.DESERT)),
                                Pair.of(
                                        Climate.parameters(1.3F, 0.6F, 0.1F, 0.1F, 0.0F, 0.0F, 0.0F), biomeRegistry.getOrThrow(WASTELAND_BIOME)),
                                Pair.of(
                                        Climate.parameters(2F, 0.3F, 0.2F, 0.1F, 0.0F, 0.0F, 0.0F), biomeRegistry.getOrThrow(WASTELAND_BIOME))
                        ))),
                noiseGenSettings.getOrThrow(CUSTOM_OVERWORLD_NOISE_GEN));
        LevelStem wastelandStem = new LevelStem(dimTypes.getOrThrow(WASTELAND_DIM_TYPE), wastelandGenerator);
        context.register(WASTELAND_KEY, wastelandStem);
    }
}

