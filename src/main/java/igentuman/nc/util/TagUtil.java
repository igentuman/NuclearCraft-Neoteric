package igentuman.nc.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.Tags;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        Tag<Block> blockTag = BlockTags.getAllTags().getTag(new ResourceLocation("forge", key));
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
        Tag<Block> blockTag = BlockTags.getAllTags().getTag(key);
        if (blockTag != null) {
            for (Block block : blockTag.getValues()) {
                tmp.add(block);
            }
        } else {
            System.out.println("Tag not found.");
        }
        return tmp;
    }


    public static Set<ResourceLocation> tagNames(Stream<? extends Tag.Named<?>> stream) {
        return stream.map(Tag.Named::getName)
                .collect(Collectors.toUnmodifiableSet());
    }

    public static Tag.Named<Fluid> createFluidTagKey(ResourceLocation resourceLocation) {
        return FluidTags.createOptional(resourceLocation);
    }

    public static Tag.Named<Fluid> createFluidTagKey(String resourceLocation) {
        return FluidTags.createOptional(new ResourceLocation("forge", resourceLocation));
    }

    public static Tag.Named<Item> createItemTag(ResourceLocation resourceLocation) {
        return ItemTags.createOptional(resourceLocation);
    }
}
