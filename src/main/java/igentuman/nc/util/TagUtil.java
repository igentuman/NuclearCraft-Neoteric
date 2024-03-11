package igentuman.nc.util;

import net.minecraft.fluid.Fluid;
import net.minecraft.util.ResourceLocation;
import net.minecraft.tags.*;
import net.minecraft.item.Item;
import net.minecraft.block.Block;
import net.minecraftforge.common.Tags;

import java.util.ArrayList;
import java.util.List;

import static igentuman.nc.NuclearCraft.rl;

public class TagUtil {

    public static Tags.IOptionalNamedTag<Block> createBlockForgeTag(String name) {
        return BlockTags.createOptional(new ResourceLocation("forge", name));
    }

    public static Tags.IOptionalNamedTag<Item> createItemForgeTag(String name) {
        return ItemTags.createOptional(new ResourceLocation("forge", name));
    }

    public static Tags.IOptionalNamedTag<Block> createBlockNCTag(String name) {
        return BlockTags.createOptional(rl(name));
    }

    public static Tags.IOptionalNamedTag<Item> createItemNCTag(String name) {
        return ItemTags.createOptional(rl(name));
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



    public static Tags.IOptionalNamedTag<Fluid> createFluidTagKey(ResourceLocation resourceLocation) {
        return FluidTags.createOptional(resourceLocation);
    }

    public static Tags.IOptionalNamedTag<Fluid> createFluidTagKey(String resourceLocation) {
        return FluidTags.createOptional(new ResourceLocation("forge", resourceLocation));
    }

    public static Tags.IOptionalNamedTag<Item> createItemTag(ResourceLocation resourceLocation) {
        return ItemTags.createOptional(resourceLocation);
    }
}
