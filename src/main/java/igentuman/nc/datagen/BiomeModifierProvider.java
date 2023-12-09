package igentuman.nc.datagen;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import igentuman.nc.setup.registration.NCBlocks;
import igentuman.nc.setup.Registration;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.GenerationStep.Decoration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.JsonCodecProvider;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ForgeBiomeModifiers;
import net.minecraftforge.common.world.ForgeBiomeModifiers.AddFeaturesBiomeModifier;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistries.Keys;
import net.minecraftforge.registries.holdersets.AnyHolderSet;

import java.util.function.Consumer;

import static igentuman.nc.NuclearCraft.MODID;

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
		return ResourceKey.create(ForgeRegistries.Keys.BIOME_MODIFIERS, new ResourceLocation(MODID, path));
	}
}