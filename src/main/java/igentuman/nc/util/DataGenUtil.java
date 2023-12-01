package igentuman.nc.util;

import com.google.common.base.Preconditions;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataGenUtil
{
	private static final Pattern MTLLIB = Pattern.compile("^mtllib\\s+(.*)$", Pattern.MULTILINE);
	private static final Pattern USEMTL = Pattern.compile("^usemtl\\s+(.*)$", Pattern.MULTILINE);
	private static final Pattern NEWMTL = Pattern.compile("^newmtl\\s+(.*)$", Pattern.MULTILINE);
	private static final Pattern MAP_KD = Pattern.compile("^map_Kd\\s+(.*)$", Pattern.MULTILINE);

	public static String getTextureFromObj(ResourceLocation obj, ExistingFileHelper helper)
	{
		try
		{
			String prefix = "models";
			if (obj.getPath().startsWith("models/"))
				prefix = "";
			Resource objResource = helper.getResource(obj, PackType.CLIENT_RESOURCES, "", prefix);
			InputStream objStream = objResource.open();
			String fullObj = IOUtils.toString(objStream, StandardCharsets.US_ASCII);
			String libLoc = findFirstOccurrenceGroup(MTLLIB, fullObj);
			String libName = findFirstOccurrenceGroup(USEMTL, fullObj);
			ResourceLocation libRL = relative(obj, libLoc);
			return getMTLTexture(libRL, libName, helper);
		} catch(IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	public static String getMTLTexture(ResourceLocation mtl, String materialName, ExistingFileHelper helper)
	{
		try
		{
			Resource mtlResource = helper.getResource(mtl, PackType.CLIENT_RESOURCES, "", "models");
			String fullMtl = IOUtils.toString(mtlResource.open(), StandardCharsets.US_ASCII);
			Matcher materialMatcher = NEWMTL.matcher(fullMtl);
			while(materialMatcher.find()&&!materialMatcher.group(1).equals(materialName)) ;
			int materialStart = materialMatcher.start();
			int materialEnd;
			if(materialMatcher.find())
				materialEnd = materialMatcher.start();
			else
				materialEnd = fullMtl.length();
			String material = fullMtl.substring(materialStart, materialEnd);
			return findFirstOccurrenceGroup(MAP_KD, material);
		} catch(IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	private static String findFirstOccurrenceGroup(Pattern pattern, String input)
	{
		Matcher matcher = pattern.matcher(input);
		Preconditions.checkArgument(matcher.find());
		return matcher.group(1);
	}

	private static ResourceLocation relative(ResourceLocation base, String relativePath)
	{
		String basePath = base.getPath();
		String lastDir = basePath.substring(0, basePath.lastIndexOf('/')+1);
		return new ResourceLocation(base.getNamespace(), lastDir+relativePath);
	}

	public static TagKey<Item> forgeIngot(String name)
	{
		return TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation("forge:ingots/"+name));
	}

	public static TagKey<Item> forgeGem(String name)
	{
		return TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation("forge:gems/"+name));
	}

	public static TagKey<Item> forgeNugget(String name)
	{
		return TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation("forge:nuggets/"+name));
	}

	public static TagKey<Item> forgeBlock(String name)
	{
		return TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation("forge:storage_blocks/"+name));
	}

	public static TagKey<Item> forgeOre(String name)
	{
		return TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation("forge:ores/"+name));
	}

	public static TagKey<Item> forgeBucket(String name)
	{
		return TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation("forge:buckets/"+name));
	}

	public static TagKey<Item> forgeChunk(String name)
	{
		return TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation("forge:raw_materials/"+name));
	}

	public static TagKey<Item> forgeDust(String name)
	{
		return TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation("forge:dusts/"+name));
	}

	public static TagKey<Item> forgePlate(String name) {
		return TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation("forge:plates/"+name));
	}

	public static TagKey<Item> forgeDye(String name) {
		return TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation("forge:dye/"+name));
	}
}
