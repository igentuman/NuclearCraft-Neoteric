package igentuman.nc.setup.level;

import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;

/** Datapack bootstrap hooks for the mod's dimension types, level stems, and noise settings. */
public class ModDimensions {

    public static void bootstrapType(BootstrapContext<DimensionType> context) {

    }

    public static void bootstrapStem(BootstrapContext<LevelStem> context) {
        HolderGetter<Biome> biomeRegistry = context.lookup(Registries.BIOME);
        HolderGetter<DimensionType> dimTypes = context.lookup(Registries.DIMENSION_TYPE);
        HolderGetter<NoiseGeneratorSettings> noiseGenSettings = context.lookup(Registries.NOISE_SETTINGS);

    }

    public static void bootstrapNoise(BootstrapContext<NoiseGeneratorSettings> context) {
        NoiseGeneratorSettings overworld = NoiseGeneratorSettings.overworld(context, false, false);

    }
}
