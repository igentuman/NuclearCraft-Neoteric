package igentuman.nc.util;

import igentuman.nc.config.Common;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static igentuman.nc.NuclearCraft.rlFromString;


/** Helpers for resolving blocks, items, and fluids by tag and building common material tag keys. */
public class TagUtil {

    public static Fluid getFirstMatchingFluidByTag(String key)
    {
        if (key.contains(":")) {
            String[] parts = key.split(":");
            String namespace = parts[0];
            if (!namespace.equals("c") && !namespace.equals("neoforge")) {
                FluidStack fluid = getFluidByName(key.replace("/", "_"));
                if (!fluid.isEmpty()) return fluid.getFluid();
                fluid = getFluidByName(key);
                if (!fluid.isEmpty()) return fluid.getFluid();
            }
            key = parts[1];
        }
        return Fluids.EMPTY;
    }

    public static FluidStack getFluidByName(String name)
    {
        ResourceLocation rl = rlFromString(name);
        if (!BuiltInRegistries.FLUID.containsKey(rl)) return FluidStack.EMPTY;
        Fluid fluid = BuiltInRegistries.FLUID.get(rl);
        if (fluid == null || fluid == Fluids.EMPTY) return FluidStack.EMPTY;
        return new FluidStack(fluid, 1);
    }

    public static HashSet<Block> getBlocksByTagKey(String key)
    {
        HashSet<Block> tmp = new HashSet<>();
        TagKey<Block> tag = TagKey.create(Registries.BLOCK, rlFromString(key));
        BuiltInRegistries.BLOCK.getTag(tag).ifPresent(holders ->
            holders.forEach(holder -> tmp.add(holder.value()))
        );
        return tmp;
    }

    public static Block getSingleBlockByTagKey(String key)
    {
        for (String mod : Common.MOD_TAG_PRIORITY.get()) {
            for (Block block : getBlocksByTagKey(key)) {
                if (block.getDescriptionId().contains(mod)) {
                    return block;
                }
            }
        }
        return getBlocksByTagKey(key).stream()
                .filter(block -> block != Blocks.AIR)
                .findFirst()
                .orElse(Blocks.AIR);
    }

    public static List<Item> getItemsByTagKey(String key)
    {
        List<Item> tmp = new ArrayList<>();
        TagKey<Item> tag = TagKey.create(Registries.ITEM, rlFromString(key));
        BuiltInRegistries.ITEM.getTag(tag).ifPresent(holders ->
            holders.forEach(holder -> tmp.add(holder.value()))
        );
        return tmp;
    }

    public static <TYPE> Optional<HolderSet.Named<TYPE>> tag(Registry<TYPE> registry, TagKey<TYPE> key) {
        return registry.getTag(key);
    }

    public static <TYPE> TagKey<TYPE> createKey(Registry<TYPE> registry, ResourceLocation tag) {
        return TagKey.create(registry.key(), tag);
    }

    public static <TYPE> Set<TagKey<TYPE>> tags(Registry<TYPE> registry, TYPE element) {
        return tagsStream(registry, element).collect(Collectors.toSet());
    }

    public static <TYPE> Stream<TagKey<TYPE>> tagsStream(Registry<TYPE> registry, TYPE element) {
        return registry.wrapAsHolder(element).tags();
    }

    public static <TYPE> Set<ResourceLocation> tagNames(Registry<TYPE> registry, TYPE element) {
        return tagNames(tagsStream(registry, element));
    }

    public static Set<ResourceLocation> tagNames(Stream<? extends TagKey<?>> stream) {
        return stream.map(TagKey::location)
                .collect(Collectors.toUnmodifiableSet());
    }

    public static TagKey<Item> plateTag(String name) {
        return ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "plates/" + name));
    }

    public static TagKey<Item> dustTag(String name) {
        return ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "dusts/" + name));
    }

    public static TagKey<Item> rawTag(String name) {
        return ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "raw_ore/" + name));
    }

    public static TagKey<Item> ingotTag(String name) {
        return ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "ingots/" + name));
    }

    public static TagKey<Item> blockTag(String name) {
        return ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "storage_blocks/" + name));
    }

    public static TagKey<Item> gemTag(String name) {
        return ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "gems/" + name));
    }

    public static TagKey<Item> nuggetTag(String name) {
        return ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "nuggets/" + name));
    }
}
