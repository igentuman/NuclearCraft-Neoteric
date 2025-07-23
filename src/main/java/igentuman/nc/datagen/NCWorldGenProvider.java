package igentuman.nc.datagen;

import igentuman.nc.world.NCConfiguredFeatures;
import igentuman.nc.world.NCPlacedFeatures;
import igentuman.nc.world.biome.NCBiomeModifier;
import igentuman.nc.world.biome.NCDensityFunction;
import igentuman.nc.world.biome.NCSurfaceRuleData;
import igentuman.nc.world.biome.WastelandBiome;
import igentuman.nc.world.dimension.Dimensions;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.DatapackBuiltinEntriesProvider;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Set;

import static igentuman.nc.NuclearCraft.MODID;

public class NCWorldGenProvider extends DatapackBuiltinEntriesProvider {
    public static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
            .add(Registries.CONFIGURED_FEATURE, NCConfiguredFeatures::bootstrap)
            .add(Registries.PLACED_FEATURE, NCPlacedFeatures::bootstrap)
            .add(Registries.BIOME, WastelandBiome::bootstrap)
            //.add(Registries.DIMENSION_TYPE, Dimensions::bootstrapType)
            //.add(Registries.LEVEL_STEM, Dimensions::bootstrapStem)
            .add(Registries.DENSITY_FUNCTION, NCDensityFunction::bootstrap)
            //.add(Registries.NOISE_SETTINGS, NCSurfaceRuleData::bootstrap)
            .add(ForgeRegistries.Keys.BIOME_MODIFIERS, NCBiomeModifier::bootstrap);


    public NCWorldGenProvider(DataGenerator generator, GatherDataEvent event) {
        super(generator.getPackOutput(), event.getLookupProvider(), BUILDER, Set.of(MODID));
    }
}

