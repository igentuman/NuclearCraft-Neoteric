package igentuman.nc.world.biome;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.placement.CaveSurface;

import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.setup.registration.NCBlocks.WASTELAND_EARTH;

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

    public static final ResourceKey<NoiseGeneratorSettings> CUSTOM_OVERWORLD_NOISE_GEN = ResourceKey.create(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY, rl("custom_overworld"));


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
