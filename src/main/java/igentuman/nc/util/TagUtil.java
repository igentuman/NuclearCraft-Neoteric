package igentuman.nc.util;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraftforge.registries.tags.IReverseTag;
import net.minecraftforge.registries.tags.ITag;
import net.minecraftforge.registries.tags.ITagManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TagUtil {
    public static List<Block> getBlocksByTagKey(String key)
    {
        List<Block> tmp = new ArrayList<>();
        TagKey<Block> tag = TagKey.create(Registry.BLOCK_REGISTRY, new ResourceLocation(key));
        for(Holder<Block> holder : Registry.BLOCK.getTagOrEmpty(tag)) {
            tmp.add(holder.value());
        }
        return tmp;
    }

    public static List<Item> getItemsByTagKey(String key)
    {
        List<Item> tmp = new ArrayList<>();
        TagKey<Item> tag = TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation(key));
        for(Holder<Item> holder : Registry.ITEM.getTagOrEmpty(tag)) {
            tmp.add(holder.value());
        }
        return tmp;
    }

    public static <TYPE extends IForgeRegistryEntry<TYPE>> ITagManager<TYPE> manager(IForgeRegistry<TYPE> registry) {
        ITagManager<TYPE> tags = registry.tags();
        if (tags == null) {
            throw new IllegalStateException("Expected " + registry.getRegistryName() + " to have tags.");
        }
        return tags;
    }

    public static <TYPE extends IForgeRegistryEntry<TYPE>> ITag<TYPE> tag(IForgeRegistry<TYPE> registry, TagKey<TYPE> key) {
        return manager(registry).getTag(key);
    }

    public static <TYPE extends IForgeRegistryEntry<TYPE>> TagKey<TYPE> createKey(IForgeRegistry<TYPE> registry, ResourceLocation tag) {
        return manager(registry).createTagKey(tag);
    }

    public static <TYPE extends IForgeRegistryEntry<TYPE>> Set<TagKey<TYPE>> tags(IForgeRegistry<TYPE> registry, TYPE element) {
        return tags(manager(registry), element);
    }
    public static <TYPE extends IForgeRegistryEntry<TYPE>> Set<TagKey<TYPE>> tags(ITagManager<TYPE> tagManager, TYPE element) {
        return tagsStream(tagManager, element).collect(Collectors.toSet());
    }

    public static <TYPE extends IForgeRegistryEntry<TYPE>> Stream<TagKey<TYPE>> tagsStream(IForgeRegistry<TYPE> registry, TYPE element) {
        return tagsStream(manager(registry), element);
    }

    public static <TYPE extends IForgeRegistryEntry<TYPE>> Stream<TagKey<TYPE>> tagsStream(ITagManager<TYPE> tagManager, TYPE element) {
        return tagManager.getReverseTag(element)
                .map(IReverseTag::getTagKeys)
                .orElse(Stream.empty());
    }

    public static <TYPE extends IForgeRegistryEntry<TYPE>> Set<ResourceLocation> tagNames(IForgeRegistry<TYPE> registry, TYPE element) {
        return tagNames(manager(registry), element);
    }

    public static <TYPE extends IForgeRegistryEntry<TYPE>> Set<ResourceLocation> tagNames(ITagManager<TYPE> tagManager, TYPE element) {
        return tagNames(tagsStream(tagManager, element));
    }

    public static Set<ResourceLocation> tagNames(Stream<? extends TagKey<?>> stream) {
        return stream.map(TagKey::location)
                .collect(Collectors.toUnmodifiableSet());
    }
}
