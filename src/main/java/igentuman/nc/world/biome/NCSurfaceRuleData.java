package igentuman.nc.world.biome;

import igentuman.nc.setup.registration.NCBlocks;
import igentuman.nc.setup.registration.WorldGeneration;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.placement.CaveSurface;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.setup.registration.NCBlocks.WASTELAND_EARTH;
import static igentuman.nc.setup.registration.WorldGeneration.WASTELAND_BIOME;
import static igentuman.nc.world.biome.NCDensityFunction.WASTELAND_TERRAIN;
import static net.minecraft.world.level.block.Blocks.STONE;
import static net.minecraft.world.level.levelgen.NoiseGeneratorSettings.OVERWORLD;

/**
 * Handles registration of surface rules for the Wasteland biome
 */
public class NCSurfaceRuleData {

    private static final SurfaceRules.RuleSource BEDROCK = makeStateRule(Blocks.BEDROCK);
    private static final SurfaceRules.RuleSource GRASS_BLOCK = makeStateRule(WASTELAND_EARTH.get());
    private static final SurfaceRules.RuleSource DIRT = makeStateRule(Blocks.ROOTED_DIRT);
    private static final SurfaceRules.RuleSource PODZOL = makeStateRule(Blocks.PODZOL);
    private static final SurfaceRules.RuleSource COARSE_DIRT = makeStateRule(Blocks.COARSE_DIRT);

    private static SurfaceRules.RuleSource makeStateRule(Block block) {
        return SurfaceRules.state(block.defaultBlockState());
    }

    public static final ResourceKey<NoiseGeneratorSettings> WASTELAND_NOISE_GEN = ResourceKey.create(Registries.NOISE_SETTINGS, rl("wasteland"));

    public static void bootstrap(BootstapContext<NoiseGeneratorSettings> context) {
        context.register(WASTELAND_NOISE_GEN, makeNoiseSettings(context));
    }

    public static NoiseGeneratorSettings makeNoiseSettings(BootstapContext<NoiseGeneratorSettings> context) {
        HolderGetter<DensityFunction> densityFunctions = context.lookup(Registries.DENSITY_FUNCTION);
        DensityFunction finalDensity = new DensityFunctions.HolderHolder(densityFunctions.getOrThrow(WASTELAND_TERRAIN));

        NoiseSettings tfNoise = NoiseSettings.create(
                -64,
                384,
                2,
                2
        );

        // Create a more varied noise router using our custom density function
        return new NoiseGeneratorSettings(
                tfNoise,
                STONE.defaultBlockState(),
                Blocks.AIR.defaultBlockState(),
                new NoiseRouter(
                        // Initial density
                        finalDensity,
                        // Final density
                        finalDensity,
                        // Continents
                        finalDensity,
                        // Erosion
                        finalDensity,
                        finalDensity,
                        finalDensity,
                        // Depth
                        finalDensity,
                        // Ridges
                        finalDensity,
                        // Initial density without jaggedness
                        finalDensity,
                        // Final density without jaggedness
                        finalDensity,
                        // Jagged features
                        DensityFunctions.zero(),
                        // Offset
                        DensityFunctions.constant(0.0D),
                        // Factor
                        DensityFunctions.constant(1.0D),
                        // Jaggedness
                        DensityFunctions.zero(),
                        // Depth
                        DensityFunctions.zero()
                ),
                tfSurface(),
                List.of(),
                60,
                false,
                false,
                true,
                false
        );
    }

    public static SurfaceRules.RuleSource tfSurface() {
        SurfaceRules.RuleSource bedrockLayer = SurfaceRules.ifTrue(SurfaceRules.verticalGradient("bedrock_floor", VerticalAnchor.bottom(), VerticalAnchor.aboveBottom(5)), BEDROCK);

        return SurfaceRules.sequence(
                bedrockLayer,
                highlandsSurface(),
                overworldLikeFloor(),
                wastelandSurface()
        );
    }

    @NotNull
    private static SurfaceRules.RuleSource highlandsSurface() {
        SurfaceRules.RuleSource podzolFloor = SurfaceRules.sequence(
                SurfaceRules.ifTrue(SurfaceRules.waterBlockCheck(-1, 0), PODZOL),
                DIRT
        );

        SurfaceRules.RuleSource highlandsSoil = SurfaceRules.ifTrue(SurfaceRules.ON_FLOOR, SurfaceRules.sequence(
                SurfaceRules.ifTrue(surfaceNoiseAbove(2.25D), COARSE_DIRT),
                SurfaceRules.ifTrue(surfaceNoiseAbove(-2.25D), podzolFloor)
        ));

        return SurfaceRules.ifTrue(SurfaceRules.isBiome(WASTELAND_BIOME), highlandsSoil);
    }

    @NotNull
    private static SurfaceRules.RuleSource overworldLikeFloor() {
        SurfaceRules.RuleSource grassAboveSeaLevel = SurfaceRules.ifTrue(SurfaceRules.yStartCheck(VerticalAnchor.absolute(-4), 1), GRASS_BLOCK);
        SurfaceRules.RuleSource grassSurface = SurfaceRules.ifTrue(SurfaceRules.waterBlockCheck(-1, 0), grassAboveSeaLevel);

        SurfaceRules.RuleSource underwaterSurface = SurfaceRules.ifTrue(
                SurfaceRules.not(SurfaceRules.yStartCheck(VerticalAnchor.absolute(-4), 1)),
                SurfaceRules.ifTrue(
                        SurfaceRules.not(SurfaceRules.waterBlockCheck(-1, 0)),
                        DIRT
                )
        );

        SurfaceRules.RuleSource onFloor = SurfaceRules.ifTrue(SurfaceRules.ON_FLOOR, SurfaceRules.sequence(
                grassSurface,
                underwaterSurface
        ));

        SurfaceRules.RuleSource underFloor = SurfaceRules.ifTrue(
                SurfaceRules.waterStartCheck(-6, -1),
                SurfaceRules.ifTrue(
                        SurfaceRules.yStartCheck(VerticalAnchor.absolute(-4), 1),
                        SurfaceRules.ifTrue(SurfaceRules.UNDER_FLOOR, DIRT)
                )
        );

        return SurfaceRules.sequence(onFloor, underFloor);
    }

    private static SurfaceRules.ConditionSource surfaceNoiseAbove(double p_194809_) {
        return SurfaceRules.noiseCondition(Noises.SURFACE, p_194809_ / 8.25D, Double.MAX_VALUE);
    }

    public static SurfaceRules.RuleSource wastelandSurface() {
        SurfaceRules.RuleSource wastelandEarthLayer = SurfaceRules.state(NCBlocks.WASTELAND_EARTH.get().defaultBlockState());
        SurfaceRules.RuleSource rootedDirtLayer = SurfaceRules.state(Blocks.ROOTED_DIRT.defaultBlockState());

        return SurfaceRules.sequence(
                SurfaceRules.ifTrue(
                        SurfaceRules.isBiome(WorldGeneration.WASTELAND_BIOME),
                        SurfaceRules.ifTrue(
                                SurfaceRules.ON_FLOOR,
                                wastelandEarthLayer
                        )
                ),
                SurfaceRules.ifTrue(
                        SurfaceRules.isBiome(WorldGeneration.WASTELAND_BIOME),
                        SurfaceRules.ifTrue(
                                SurfaceRules.UNDER_FLOOR,
                                SurfaceRules.ifTrue(
                                        SurfaceRules.stoneDepthCheck(0, false, 3, CaveSurface.FLOOR),
                                        rootedDirtLayer
                                )
                        )
                )
        );
    }
}
