package igentuman.nc.util;

import com.google.common.base.Preconditions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.tags.Tag;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static igentuman.nc.util.TagUtil.createItemForgeTag;

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
			InputStream objStream = objResource.getInputStream();
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
			String fullMtl = IOUtils.toString(mtlResource.getInputStream(), StandardCharsets.US_ASCII);
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

	public static Tag.Named<Item> forgeIngot(String name)
	{
		return createItemForgeTag("ingots/"+name);
	}

	public static Tag.Named<Item> forgeGem(String name)
	{
		return createItemForgeTag("gems/"+name);
	}

	public static Tag.Named<Item> forgeNugget(String name)
	{
		return createItemForgeTag("nuggets/"+name);
	}

	public static Tag.Named<Item> forgeBlock(String name)
	{
		return createItemForgeTag("storage_blocks/"+name);
	}

	public static Tag.Named<Item> forgeOre(String name)
	{
		return createItemForgeTag("ores/"+name);
	}

	public static Tag.Named<Item> forgeBucket(String name)
	{
		return createItemForgeTag("buckets/"+name);
	}

	public static Tag.Named<Item> forgeChunk(String name)
	{
		return createItemForgeTag("raw_materials/"+name);
	}

	public static Tag.Named<Item> forgeDust(String name)
	{
		return createItemForgeTag("dusts/"+name);
	}

	public static Tag.Named<Item> forgePlate(String name) {
		return createItemForgeTag("plates/"+name);
	}

	public static Tag.Named<Item> forgeDye(String name) {
		return createItemForgeTag("dye/"+name);
	}
}
