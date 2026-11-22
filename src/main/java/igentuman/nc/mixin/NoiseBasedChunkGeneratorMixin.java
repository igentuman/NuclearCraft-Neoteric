package igentuman.nc.mixin;

import com.google.common.base.Suppliers;
import igentuman.nc.setup.level.ModBiomes;
import igentuman.nc.setup.level.ModDimensions;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.SurfaceSystem;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.blending.Blender;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Desc;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.function.Supplier;

/** Applies Wasteland soil to its biome in the vanilla Overworld terrain presets. */
@Mixin(NoiseBasedChunkGenerator.class)
public class NoiseBasedChunkGeneratorMixin {
    @Shadow
    @Final
    private Holder<NoiseGeneratorSettings> settings;

    @Unique
    private static final Supplier<SurfaceRules.RuleSource> NC_WASTELAND_SURFACE = Suppliers.memoize(() ->
            SurfaceRules.ifTrue(SurfaceRules.abovePreliminarySurface(),
                    SurfaceRules.ifTrue(SurfaceRules.isBiome(ModBiomes.WASTELAND), ModDimensions.wastelandSoil())));

    @ModifyArg(
            target = @Desc(value = "buildSurface", args = {ChunkAccess.class, WorldGenerationContext.class,
                    RandomState.class, StructureManager.class, BiomeManager.class, Registry.class, Blender.class}),
            at = @At(value = "INVOKE", desc = @Desc(owner = SurfaceSystem.class, value = "buildSurface",
                    args = {RandomState.class, BiomeManager.class, Registry.class, boolean.class,
                            WorldGenerationContext.class, ChunkAccess.class, NoiseChunk.class, SurfaceRules.RuleSource.class})),
            index = 7)
    private SurfaceRules.RuleSource nc$addWastelandSoil(SurfaceRules.RuleSource original) {
        if (settings.is(NoiseGeneratorSettings.OVERWORLD) || settings.is(NoiseGeneratorSettings.LARGE_BIOMES)
                || settings.is(NoiseGeneratorSettings.AMPLIFIED)) {
            return SurfaceRules.sequence(NC_WASTELAND_SURFACE.get(), original);
        }
        return original;
    }
}
