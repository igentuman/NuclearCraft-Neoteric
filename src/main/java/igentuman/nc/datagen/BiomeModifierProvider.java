package igentuman.nc.datagen;

import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.registries.ForgeRegistries;

import static igentuman.nc.NuclearCraft.rl;

public class BiomeModifierProvider
{

	public static void register(BootstapContext<BiomeModifier> context) {
		HolderGetter<PlacedFeature> features = context.lookup(Registries.PLACED_FEATURE);
		HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);
		return;
		/*for(String name : NCBlocks.ORE_BLOCKS.keySet())
		{
			ResourceLocation nameRL = new ResourceLocation(MODID, "nc_ores_"+name);
			ResourceKey<PlacedFeature> key = ResourceKey.create(Registries.PLACED_FEATURE, nameRL);
			Holder<PlacedFeature> featureHolder = features.getOrThrow(key);

			context.register(key("overworld_ores"), new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
					biomes.getOrThrow(BiomeTags.IS_OVERWORLD),
					HolderSet.direct(featureHolder),
					GenerationStep.Decoration.UNDERGROUND_ORES));
		}*/
	}

	private static ResourceKey<BiomeModifier> key(String path)
	{
		return ResourceKey.create(ForgeRegistries.Keys.BIOME_MODIFIERS, rl(path));
	}
}