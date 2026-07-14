package igentuman.nc.recipe;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Resolves tag-based recipe outputs to a concrete item/fluid member using the configured mod priority order. */
public class TagOutputResolver {

    private static volatile Map<String, Integer> priorityIndex = new HashMap<>();

    public static void setPriority(List<? extends String> mods) {
        Map<String, Integer> map = new HashMap<>();
        for (int i = 0; i < mods.size(); i++) {
            map.put(mods.get(i), i);
        }
        priorityIndex = map;
    }

    private static int rank(String namespace) {
        return priorityIndex.getOrDefault(namespace, Integer.MAX_VALUE);
    }

    public static ItemStack resolveItem(TagKey<Item> tag, int count) {
        Optional<HolderSet.Named<Item>> set = BuiltInRegistries.ITEM.getTag(tag);
        if (set.isEmpty()) return ItemStack.EMPTY;
        Item best = null;
        int bestRank = Integer.MAX_VALUE;
        for (Holder<Item> holder : set.get()) {
            Item item = holder.value();
            int r = rank(BuiltInRegistries.ITEM.getKey(item).getNamespace());
            if (r < bestRank) {
                bestRank = r;
                best = item;
            }
        }
        return best == null ? ItemStack.EMPTY : new ItemStack(best, count);
    }

    public static List<ItemStack> membersItem(TagKey<Item> tag, int count) {
        Optional<HolderSet.Named<Item>> set = BuiltInRegistries.ITEM.getTag(tag);
        if (set.isEmpty()) return List.of();
        List<Item> items = new ArrayList<>();
        for (Holder<Item> holder : set.get()) {
            items.add(holder.value());
        }
        items.sort(Comparator.comparingInt(i -> rank(BuiltInRegistries.ITEM.getKey(i).getNamespace())));
        List<ItemStack> result = new ArrayList<>(items.size());
        for (Item item : items) {
            result.add(new ItemStack(item, count));
        }
        return result;
    }

    public static FluidStack resolveFluid(TagKey<Fluid> tag, int amount) {
        Optional<HolderSet.Named<Fluid>> set = BuiltInRegistries.FLUID.getTag(tag);
        if (set.isEmpty()) return FluidStack.EMPTY;
        Fluid best = null;
        int bestRank = Integer.MAX_VALUE;
        for (Holder<Fluid> holder : set.get()) {
            Fluid fluid = holder.value();
            int r = rank(BuiltInRegistries.FLUID.getKey(fluid).getNamespace());
            if (r < bestRank) {
                bestRank = r;
                best = fluid;
            }
        }
        return best == null ? FluidStack.EMPTY : new FluidStack(best, amount);
    }

    public static List<FluidStack> membersFluid(TagKey<Fluid> tag, int amount) {
        Optional<HolderSet.Named<Fluid>> set = BuiltInRegistries.FLUID.getTag(tag);
        if (set.isEmpty()) return List.of();
        List<Fluid> fluids = new ArrayList<>();
        for (Holder<Fluid> holder : set.get()) {
            fluids.add(holder.value());
        }
        fluids.sort(Comparator.comparingInt(f -> rank(BuiltInRegistries.FLUID.getKey(f).getNamespace())));
        List<FluidStack> result = new ArrayList<>(fluids.size());
        for (Fluid fluid : fluids) {
            result.add(new FluidStack(fluid, amount));
        }
        return result;
    }
}
