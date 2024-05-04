package igentuman.nc.util;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.tags.IReverseTag;
import net.minecraftforge.registries.tags.ITag;
import net.minecraftforge.registries.tags.ITagManager;
import static igentuman.nc.setup.registration.NCBlocks.ITEMS;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static igentuman.nc.handler.config.MaterialsConfig.MATERIAL_PRODUCTS;

public class TagUtil {

    public static Fluid getFirstMatchingFluidByTag(String key)
    {
        if(key.contains(":")) {
            String[] parts = key.split(":");
            if(!Objects.equals(parts[0], "forge")) {
                FluidStack fluid = getFluidByName(key.replace("/","_"));
                if(!fluid.isEmpty()) {
                    return fluid.getFluid();
                }
                fluid = getFluidByName(key);
                if(!fluid.isEmpty()) {
                    return fluid.getFluid();
                }
            }
            key = parts[1];
        }

        for(String mod: MATERIAL_PRODUCTS.MODS_PRIORITY.get()) {
            FluidStack fluid = getFluidByName(mod+":"+key);
            if(!fluid.isEmpty()) {
                return fluid.getFluid();
            }
            fluid = getFluidByName(mod+":"+key.replace("/","_"));
            if(!fluid.isEmpty()) {
                return fluid.getFluid();
            }
        }
        return FluidStack.EMPTY.getFluid();
    }

    public static FluidStack getFluidByName(String name)
    {
        CompoundTag tag = new CompoundTag();
        tag.putString("FluidName", name);
        tag.putInt("Amount", 1);
        return FluidStack.loadFluidStackFromNBT(tag);
    }

    public static List<Block> getBlocksByTagKey(String key)
    {
        List<Block> tmp = new ArrayList<>();
        TagKey<Block> tag = TagKey.create(Registry.BLOCK_REGISTRY, new ResourceLocation(key));
        for(Holder<Block> holder : Registry.BLOCK.getTagOrEmpty(tag)) {
            tmp.add(holder.get());
        }
        return tmp;
    }

    public static List<Item> getItemsByTagKey(String key)
    {
        List<Item> tmp = new ArrayList<>();
        TagKey<Item> tag = TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation(key));
        for(HolderSet.Named<Item> holder : Registry.ITEM.getTag(tag).stream().toList()) {
            //tmp.add(holder);
            //todo resolve
        }
        return tmp;
    }

    public static <TYPE> ITagManager<TYPE> manager(IForgeRegistry<TYPE> registry) {
        ITagManager<TYPE> tags = registry.tags();
        if (tags == null) {
            throw new IllegalStateException("Expected " + registry.getRegistryName() + " to have tags.");
        }
        return tags;
    }

    public static <TYPE> ITag<TYPE> tag(IForgeRegistry<TYPE> registry, TagKey<TYPE> key) {
        return manager(registry).getTag(key);
    }

    public static <TYPE> TagKey<TYPE> createKey(IForgeRegistry<TYPE> registry, ResourceLocation tag) {
        return manager(registry).createTagKey(tag);
    }

    public static <TYPE> Set<TagKey<TYPE>> tags(IForgeRegistry<TYPE> registry, TYPE element) {
        return tags(manager(registry), element);
    }

    public static <TYPE> Set<TagKey<TYPE>> tags(ITagManager<TYPE> tagManager, TYPE element) {
        return tagsStream(tagManager, element).collect(Collectors.toSet());
    }

    public static <TYPE> Stream<TagKey<TYPE>> tagsStream(IForgeRegistry<TYPE> registry, TYPE element) {
        return tagsStream(manager(registry), element);
    }

    public static <TYPE> Stream<TagKey<TYPE>> tagsStream(ITagManager<TYPE> tagManager, TYPE element) {
        return tagManager.getReverseTag(element)
                .map(IReverseTag::getTagKeys)
                .orElse(Stream.empty());
    }

    public static <TYPE> Set<ResourceLocation> tagNames(IForgeRegistry<TYPE> registry, TYPE element) {
        return tagNames(manager(registry), element);
    }

    public static <TYPE > Set<ResourceLocation> tagNames(ITagManager<TYPE> tagManager, TYPE element) {
        return tagNames(tagsStream(tagManager, element));
    }

    public static Set<ResourceLocation> tagNames(Stream<? extends TagKey<?>> stream) {
        return stream.map(TagKey::location)
                .collect(Collectors.toUnmodifiableSet());
    }
}
