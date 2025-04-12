package igentuman.nc.util;

import net.minecraft.fluid.Fluid;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.block.Block;

import java.util.ArrayList;
import java.util.List;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.NuclearCraft.rl;

public class TagUtil {

    public static ITag.INamedTag<Block> createBlockForgeTag(String name) {
        return BlockTags.bind("forge:" + name);
    }

    public static ITag.INamedTag<Item> createItemForgeTag(String name) {
        return ItemTags.bind("forge:" + name);
    }

    public static ITag.INamedTag<Block> createBlockNCTag(String name) {
        return BlockTags.bind(MODID + ":" +name);
    }

    public static ITag.INamedTag<Item> createItemNCTag(String name) {
        return ItemTags.bind(MODID + ":" +name);
    }

    public static List<Block> getBlocksByTagKey(String key)
    {
        List<Block> tmp = new ArrayList<>();
        ITag<Block> blockTag = BlockTags.getAllTags().getTag(new ResourceLocation("forge", key));
        if (blockTag != null) {
            for (Block block : blockTag.getValues()) {
                tmp.add(block);
            }
        } else {
            System.out.println("Tag not found.");
        }
        return tmp;
    }

    public static List<Block> getBlocksByTagKey(ResourceLocation key)
    {
        List<Block> tmp = new ArrayList<>();
        ITag<Block> blockTag = BlockTags.getAllTags().getTag(key);
        if (blockTag != null) {
            for (Block block : blockTag.getValues()) {
                tmp.add(block);
            }
        } else {
            System.out.println("Tag not found.");
        }
        return tmp;
    }



    public static ITag.INamedTag<Fluid> createFluidTagKey(ResourceLocation resourceLocation) {
        return FluidTags.bind(resourceLocation.toString());
    }

    public static ITag.INamedTag<Fluid> createFluidTagKey(String name) {
        return FluidTags.bind("forge:" + name);
    }

    public static ITag.INamedTag<Item> createItemTag(ResourceLocation resourceLocation) {
        return ItemTags.bind(resourceLocation.toString());
    }
}
