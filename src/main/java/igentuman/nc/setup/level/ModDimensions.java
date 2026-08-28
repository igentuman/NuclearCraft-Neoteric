package igentuman.nc.setup.level;

import com.mojang.datafixers.util.Pair;
import igentuman.nc.setup.ModEntries;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.NoiseSettings;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.placement.CaveSurface;

import java.util.List;
import java.util.OptionalLong;

import static igentuman.nc.NuclearCraft.rl;

/** Datapack bootstrap hooks for the mod's dimension types, level stems, and noise settings. */
public class ModDimensions {

    public static final ResourceKey<Level> WASTELAND = ResourceKey.create(Registries.DIMENSION, rl("wasteland"));
    public static final ResourceKey<LevelStem> WASTELAND_STEM = ResourceKey.create(Registries.LEVEL_STEM, rl("wasteland"));
    public static final ResourceKey<DimensionType> WASTELAND_TYPE = ResourceKey.create(Registries.DIMENSION_TYPE, rl("wasteland_type"));
    public static final ResourceKey<NoiseGeneratorSettings> CUSTOM_OVERWORLD = ResourceKey.create(Registries.NOISE_SETTINGS, rl("custom_overworld"));

    public static void bootstrapType(BootstrapContext<DimensionType> context) {
        context.register(WASTELAND_TYPE, new DimensionType(
                OptionalLong.empty(),
                true,
                false,
                false,
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

    public static void bootstrapStem(BootstrapContext<LevelStem> context) {
        HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);
        HolderGetter<DimensionType> dimTypes = context.lookup(Registries.DIMENSION_TYPE);
        HolderGetter<NoiseGeneratorSettings> noiseGenSettings = context.lookup(Registries.NOISE_SETTINGS);

        NoiseBasedChunkGenerator generator = new NoiseBasedChunkGenerator(
                MultiNoiseBiomeSource.createFromList(new Climate.ParameterList<>(List.of(
                        Pair.of(Climate.parameters(1.2F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F), biomes.getOrThrow(ModBiomes.WASTELAND)),
                        Pair.of(Climate.parameters(1.9F, 0.2F, 0.0F, 0.2F, 0.0F, 0.0F, 0.0F), biomes.getOrThrow(Biomes.DESERT)),
                        Pair.of(Climate.parameters(1.3F, 0.6F, 0.1F, 0.1F, 0.0F, 0.0F, 0.0F), biomes.getOrThrow(ModBiomes.WASTELAND)),
                        Pair.of(Climate.parameters(2.0F, 0.3F, 0.2F, 0.1F, 0.0F, 0.0F, 0.0F), biomes.getOrThrow(ModBiomes.WASTELAND))
                ))),
                noiseGenSettings.getOrThrow(CUSTOM_OVERWORLD));
        context.register(WASTELAND_STEM, new LevelStem(dimTypes.getOrThrow(WASTELAND_TYPE), generator));
    }

    public static void bootstrapNoise(BootstrapContext<NoiseGeneratorSettings> context) {
        NoiseGeneratorSettings overworld = NoiseGeneratorSettings.overworld(context, false, false);
        NoiseSettings noiseSettings = NoiseSettings.create(0, 256, 4, 1);
        context.register(CUSTOM_OVERWORLD, new NoiseGeneratorSettings(
                noiseSettings,
                overworld.defaultBlock(),
                overworld.defaultFluid(),
                overworld.noiseRouter(),
                wastelandSurface(),
                overworld.spawnTarget(),
                35,
                overworld.disableMobGeneration(),
                overworld.aquifersEnabled(),
                true,
                overworld.useLegacyRandomSource()));
    }

    private static SurfaceRules.RuleSource wastelandSurface() {
        BlockState earth = ModEntries.get("wasteland_earth").block().get().defaultBlockState();
        SurfaceRules.RuleSource bedrock = SurfaceRules.ifTrue(
                SurfaceRules.verticalGradient("bedrock_floor", VerticalAnchor.bottom(), VerticalAnchor.aboveBottom(3)),
                SurfaceRules.state(Blocks.BEDROCK.defaultBlockState()));
        SurfaceRules.RuleSource top = SurfaceRules.ifTrue(
                SurfaceRules.ON_FLOOR,
                SurfaceRules.ifTrue(SurfaceRules.waterBlockCheck(-1, 0), SurfaceRules.state(earth)));
        SurfaceRules.RuleSource under = SurfaceRules.ifTrue(
                SurfaceRules.UNDER_FLOOR,
                SurfaceRules.ifTrue(SurfaceRules.stoneDepthCheck(0, false, 3, CaveSurface.FLOOR), SurfaceRules.state(Blocks.COARSE_DIRT.defaultBlockState())));
        return SurfaceRules.sequence(bedrock, top, under);
    }
}
