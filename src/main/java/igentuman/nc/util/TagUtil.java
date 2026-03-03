package igentuman.nc.util;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static igentuman.nc.handler.config.MaterialsConfig.MATERIAL_PRODUCTS;
import static igentuman.nc.setup.registration.Registries.BLOCK_REGISTRY;
import static igentuman.nc.setup.registration.Registries.ITEM_REGISTRY;
import static igentuman.nc.util.NcUtils.rlFromString;

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
        ResourceLocation rl = rlFromString(name);
        Fluid fluid = BuiltInRegistries.FLUID.get(rl);
        if (fluid == net.minecraft.world.level.material.Fluids.EMPTY) {
            return FluidStack.EMPTY;
        }
        return new FluidStack(fluid, 1);
    }

    public static HashSet<Block> getBlocksByTagKey(String key) {
        HashSet<Block> tmp = new HashSet<>();
        TagKey<Block> tag = TagKey.create(BLOCK_REGISTRY, rlFromString(key));
        BuiltInRegistries.BLOCK.getTag(tag).ifPresent(holders -> {
            for (Holder<Block> holder : holders) {
                tmp.add(holder.value());
            }
        });
        return tmp;
    }

    public static Block getSingleBlockByTagKey(String key)
    {
        for(String mod: MATERIAL_PRODUCTS.MODS_PRIORITY.get()) {
            for(Block holder : getBlocksByTagKey(key)) {
                if(holder.getDescriptionId().contains(mod)) {
                    return holder;
                }
            }
        }
        return getBlocksByTagKey(key).stream()
                .filter(block -> block != Blocks.AIR)
                .findFirst()
                .orElse(Blocks.AIR);
    }

    public static List<Item> getItemsByTagKey(String key) {
        List<Item> tmp = new ArrayList<>();
        TagKey<Item> tag = TagKey.create(ITEM_REGISTRY, rlFromString(key));
        BuiltInRegistries.ITEM.getTag(tag).ifPresent(holders -> {
            for (Holder<Item> holder : holders) {
                tmp.add(holder.value());
            }
        });
        return tmp;
    }

    /**
     * Creates a TagKey for the given registry and resource location.
     * Replacement for the old manager(registry).createTagKey(rl) pattern.
     */
    public static <TYPE> TagKey<TYPE> createKey(Registry<TYPE> registry, ResourceLocation tag) {
        return TagKey.create(registry.key(), tag);
    }

    /**
     * Gets the tag contents from a registry by tag key.
     * Returns an Optional containing the named holder set, or empty if the tag doesn't exist.
     */
    public static <TYPE> Optional<HolderSet.Named<TYPE>> getTag(Registry<TYPE> registry, TagKey<TYPE> key) {
        return registry.getTag(key);
    }

    /**
     * Gets all tag keys associated with the given element in the registry.
     */
    public static <TYPE> Set<TagKey<TYPE>> tags(Registry<TYPE> registry, TYPE element) {
        Optional<Holder.Reference<TYPE>> holderOpt = registry.getResourceKey(element)
                .flatMap(registry::getHolder);
        if (holderOpt.isPresent()) {
            return holderOpt.get().tags().collect(Collectors.toSet());
        }
        return Set.of();
    }

    /**
     * Gets a stream of tag keys associated with the given element in the registry.
     */
    public static <TYPE> Stream<TagKey<TYPE>> tagsStream(Registry<TYPE> registry, TYPE element) {
        Optional<Holder.Reference<TYPE>> holderOpt = registry.getResourceKey(element)
                .flatMap(registry::getHolder);
        return holderOpt.map(ref -> ref.tags()).orElse(Stream.empty());
    }

    /**
     * Gets tag name resource locations for the given element.
     */
    public static <TYPE> Set<ResourceLocation> tagNames(Registry<TYPE> registry, TYPE element) {
        return tagNames(tagsStream(registry, element));
    }

    public static Set<ResourceLocation> tagNames(Stream<? extends TagKey<?>> stream) {
        return stream.map(TagKey::location)
                .collect(Collectors.toUnmodifiableSet());
    }

    /**
     * Checks if a tag contains the given element.
     */
    public static <TYPE> boolean tagContains(Registry<TYPE> registry, TagKey<TYPE> key, TYPE element) {
        return registry.getTag(key)
                .map(holders -> holders.stream().anyMatch(h -> h.value() == element))
                .orElse(false);
    }

    /**
     * Checks if a tag is empty (has no entries).
     */
    public static <TYPE> boolean isTagEmpty(Registry<TYPE> registry, TagKey<TYPE> key) {
        return registry.getTag(key)
                .map(holders -> holders.size() == 0)
                .orElse(true);
    }

    /**
     * Gets all elements in a tag as a list.
     */
    public static <TYPE> List<TYPE> getTagElements(Registry<TYPE> registry, TagKey<TYPE> key) {
        return registry.getTag(key)
                .map(holders -> holders.stream().map(Holder::value).toList())
                .orElse(List.of());
    }
}
