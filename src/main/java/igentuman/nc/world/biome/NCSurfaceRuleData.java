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
    private static final SurfaceRules.RuleSource COARSE_DIRT = makeStateRule(Blocks.COARSE_DIRT);

    private static SurfaceRules.RuleSource makeStateRule(Block block) {
        return SurfaceRules.state(block.defaultBlockState());
    }

    public static final ResourceKey<NoiseGeneratorSettings> CUSTOM_OVERWORLD_NOISE_GEN = ResourceKey.create(Registries.NOISE_SETTINGS, rl("custom_overworld"));

    public static void bootstrap(BootstapContext<NoiseGeneratorSettings> context) {
        context.register(CUSTOM_OVERWORLD_NOISE_GEN, makeCustomOverworldNoiseSettings(context));
    }
    
    /**
     * Creates a custom NoiseGeneratorSettings that inherits from OVERWORLD but overrides specific properties
     */
    public static NoiseGeneratorSettings makeCustomOverworldNoiseSettings(BootstapContext<NoiseGeneratorSettings> context) {
        // Get the OVERWORLD NoiseGeneratorSettings
        HolderGetter<NoiseGeneratorSettings> noiseGenSettings = context.lookup(Registries.NOISE_SETTINGS);
        NoiseGeneratorSettings overworldSettings = NoiseGeneratorSettings.overworld(context, false, false);
        
        // Create a custom NoiseSettings with different height values
        NoiseSettings customNoiseSettings = NoiseSettings.create(
                0,
                256,
                4,
                1
        );
        
        // Create a custom NoiseRouter with modified values
        NoiseRouter overworldRouter = overworldSettings.noiseRouter();
        NoiseRouter customRouter = new NoiseRouter(
                overworldRouter.barrierNoise(),
                overworldRouter.fluidLevelFloodednessNoise(),
                overworldRouter.fluidLevelSpreadNoise(),
                overworldRouter.lavaNoise(),
                overworldRouter.temperature(),
                overworldRouter.vegetation(),
                overworldRouter.continents(),
                overworldRouter.erosion(),
                overworldRouter.depth(),
                overworldRouter.ridges(),
                overworldRouter.initialDensityWithoutJaggedness(),
                overworldRouter.initialDensityWithoutJaggedness(),
                overworldRouter.veinToggle(),
                overworldRouter.veinRidged(),
                overworldRouter.veinGap()
        );
        
        // Create the custom NoiseGeneratorSettings
        return new NoiseGeneratorSettings(
                customNoiseSettings,
                overworldSettings.defaultBlock(),
                overworldSettings.defaultFluid(),
                overworldSettings.noiseRouter(),
                customOverworldSurface(),
                overworldSettings.spawnTarget(),
                35,
                overworldSettings.disableMobGeneration(),
                overworldSettings.aquifersEnabled(),
                overworldSettings.oreVeinsEnabled(),
                overworldSettings.useLegacyRandomSource()
        );
    }
    
    /**
     * Creates a custom surface rule for the custom overworld that uses wasteland_earth as the top surface
     */
    public static SurfaceRules.RuleSource customOverworldSurface() {
        SurfaceRules.RuleSource bedrockLayer = SurfaceRules.ifTrue(
            SurfaceRules.verticalGradient("bedrock_floor", VerticalAnchor.bottom(), VerticalAnchor.aboveBottom(3)),
            BEDROCK
        );
        
        // Use wasteland_earth for the top surface
        SurfaceRules.RuleSource wastelandEarthLayer = SurfaceRules.state(WASTELAND_EARTH.get().defaultBlockState());
        
        // Create a surface rule that uses wasteland_earth for the top layer
        SurfaceRules.RuleSource topSurface = SurfaceRules.ifTrue(
            SurfaceRules.ON_FLOOR,
            SurfaceRules.ifTrue(
                SurfaceRules.waterBlockCheck(-1, 0),
                wastelandEarthLayer
            )
        );
        
        // Use rooted dirt for the layer beneath
        SurfaceRules.RuleSource underSurface = SurfaceRules.ifTrue(
            SurfaceRules.UNDER_FLOOR,
            SurfaceRules.ifTrue(
                SurfaceRules.stoneDepthCheck(0, false, 3, CaveSurface.FLOOR),
                    COARSE_DIRT
            )
        );
        
        return SurfaceRules.sequence(
            bedrockLayer,
            topSurface,
            underSurface
        );
    }
}
