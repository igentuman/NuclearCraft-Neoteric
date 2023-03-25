package igentuman.nc.datagen;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import igentuman.nc.setup.registration.NCBlocks;
import igentuman.nc.setup.Registration;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep.Decoration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.JsonCodecProvider;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ForgeBiomeModifiers.AddFeaturesBiomeModifier;
import net.minecraftforge.registries.ForgeRegistries.Keys;
import net.minecraftforge.registries.holdersets.AnyHolderSet;

import java.util.function.Consumer;

import static igentuman.nc.NuclearCraft.MODID;

public class BiomeModifierProvider
{
	public static void addTo(
			DataGenerator dataGenerator, ExistingFileHelper existingFileHelper, Consumer<DataProvider> add
	)
	{
		Registration.initLate();

		RegistryAccess registryAccess = RegistryAccess.builtinCopy();
		RegistryOps<JsonElement> jsonOps = RegistryOps.create(JsonOps.INSTANCE, registryAccess);
		ImmutableMap.Builder<ResourceLocation, BiomeModifier> modifiers = ImmutableMap.builder();
		Registry<Biome> biomeReg = registryAccess.registryOrThrow(Registry.BIOME_REGISTRY);
		Registry<PlacedFeature> featureReg = registryAccess.registryOrThrow(Registry.PLACED_FEATURE_REGISTRY);
		for(String name : NCBlocks.ORE_BLOCKS.keySet())
		{
			ResourceLocation nameRL = new ResourceLocation(MODID, "nc_ores_"+name);
			ResourceKey<PlacedFeature> key = ResourceKey.create(Registry.PLACED_FEATURE_REGISTRY, nameRL);
			Holder<PlacedFeature> featureHolder = featureReg.getHolderOrThrow(key);
			HolderSet<Biome> biomes;
			biomes = new AnyHolderSet<>(biomeReg);
			AddFeaturesBiomeModifier modifier = new AddFeaturesBiomeModifier(
					biomes, HolderSet.direct(featureHolder), Decoration.UNDERGROUND_ORES
			);
			modifiers.put(nameRL, modifier);
		}
		add.accept(JsonCodecProvider.forDatapackRegistry(
				dataGenerator, existingFileHelper, MODID, jsonOps, Keys.BIOME_MODIFIERS, modifiers.build()
		));
	}
}