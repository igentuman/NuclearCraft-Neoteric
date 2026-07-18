package igentuman.nc.datagen;

import net.minecraft.resources.ResourceKey;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.registries.ForgeRegistries;

import static igentuman.nc.NuclearCraft.rl;

public class BiomeModifierProvider
{
	private static ResourceKey<BiomeModifier> key(String path)
	{
		return ResourceKey.create(ForgeRegistries.Keys.BIOME_MODIFIERS, rl(path));
	}
}
