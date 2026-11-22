package igentuman.nc.handler.crafter;

import igentuman.nc.item.BarrelBlockItem;
import igentuman.nc.item.ContainerBlockItem;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Flat view over every storage item (containers for items, barrels for fluids) sitting in the crafter. */
public class AggregatedInventory {

    public record ItemKey(Item item, DataComponentPatch patch) {
        public static ItemKey of(ItemStack stack) {
            return new ItemKey(stack.getItem(), stack.getComponentsPatch());
        }

        public ItemStack sample() {
            ItemStack stack = new ItemStack(item);
            stack.applyComponents(patch);
            return stack;
        }
    }

    public record Entry(ItemKey key, ItemStack stack, int count) {}

    public record FluidEntry(CraftKey key, FluidStack stack, int amount) {}

    private final ResourceHandler<ItemResource> containers;

    public AggregatedInventory(ResourceHandler<ItemResource> containers) {
        this.containers = containers;
    }

    public static boolean isStorageItem(ItemStack stack) {
        return !stack.isEmpty()
                && (stack.getItem() instanceof ContainerBlockItem || stack.getItem() instanceof BarrelBlockItem);
    }

    private List<ResourceHandler<ItemResource>> sources() {
        return AggregatedItemHandler.sources(containers);
    }

    private List<ResourceHandler<FluidResource>> fluidSources() {
        List<ResourceHandler<FluidResource>> out = new ArrayList<>();
        for (int i = 0; i < containers.size(); i++) {
            var resource = containers.getResource(i);
            if (resource.isEmpty() || !(resource.getItem() instanceof BarrelBlockItem)) continue;
            var access = ItemAccess.forHandlerIndexStrict(containers, i);
            var handler = access.getCapability(Capabilities.Fluid.ITEM);
            if (handler != null) out.add(handler);
        }
        return out;
    }

    private static ItemStack stackAt(ResourceHandler<ItemResource> handler, int index) {
        return handler.getResource(index).toStack(handler.getAmountAsInt(index));
    }

    private static FluidStack fluidAt(ResourceHandler<FluidResource> handler, int index) {
        return handler.getResource(index).toStack(handler.getAmountAsInt(index));
    }

    private static int addCounts(int first, int second) {
        return (int) Math.min(Integer.MAX_VALUE, (long) first + second);
    }

    public List<ItemStack> allSlots() {
        List<ItemStack> out = new ArrayList<>();
        for (ResourceHandler<ItemResource> h : sources()) {
            for (int i = 0; i < h.size(); i++) {
                out.add(stackAt(h, i));
            }
        }
        return out;
    }

    public List<Entry> entries() {
        Map<ItemKey, Integer> counts = new LinkedHashMap<>();
        Map<ItemKey, ItemStack> reps = new LinkedHashMap<>();
        for (ResourceHandler<ItemResource> h : sources()) {
            for (int i = 0; i < h.size(); i++) {
                ItemStack s = stackAt(h, i);
                if (s.isEmpty()) continue;
                ItemKey key = ItemKey.of(s);
                counts.merge(key, s.getCount(), AggregatedInventory::addCounts);
                reps.computeIfAbsent(key, k -> {
                    ItemStack one = s.copy();
                    one.setCount(1);
                    return one;
                });
            }
        }
        List<Entry> list = new ArrayList<>(counts.size());
        for (Map.Entry<ItemKey, Integer> e : counts.entrySet()) {
            list.add(new Entry(e.getKey(), reps.get(e.getKey()), e.getValue()));
        }
        return list;
    }

    public List<FluidEntry> fluidEntries() {
        Map<CraftKey, Integer> amounts = new LinkedHashMap<>();
        Map<CraftKey, FluidStack> reps = new LinkedHashMap<>();
        for (ResourceHandler<FluidResource> h : fluidSources()) {
            for (int t = 0; t < h.size(); t++) {
                FluidStack f = fluidAt(h, t);
                if (f.isEmpty()) continue;
                CraftKey key = CraftKey.of(f);
                amounts.merge(key, f.getAmount(), AggregatedInventory::addCounts);
                reps.computeIfAbsent(key, k -> f.copyWithAmount(1));
            }
        }
        List<FluidEntry> list = new ArrayList<>(amounts.size());
        for (Map.Entry<CraftKey, Integer> e : amounts.entrySet()) {
            list.add(new FluidEntry(e.getKey(), reps.get(e.getKey()), e.getValue()));
        }
        return list;
    }

    public int count(ItemStack sample) {
        int total = 0;
        for (ResourceHandler<ItemResource> h : sources()) {
            for (int i = 0; i < h.size(); i++) {
                ItemStack s = stackAt(h, i);
                if (!s.isEmpty() && ItemStack.isSameItemSameComponents(s, sample)) total = addCounts(total, s.getCount());
            }
        }
        return total;
    }

    public int countFluid(FluidStack sample) {
        int total = 0;
        for (ResourceHandler<FluidResource> h : fluidSources()) {
            for (int t = 0; t < h.size(); t++) {
                FluidStack f = fluidAt(h, t);
                if (!f.isEmpty() && FluidStack.isSameFluidSameComponents(f, sample)) total = addCounts(total, f.getAmount());
            }
        }
        return total;
    }

    /** Extracts across linked stores in the supplied transaction. */
    public ItemStack extract(ItemStack sample, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonNegative(amount);
        if (sample.isEmpty() || amount == 0) return ItemStack.EMPTY;
        ItemResource resource = ItemResource.of(sample);
        int extracted = 0;
        for (var handler : sources()) {
            extracted += handler.extract(resource, amount - extracted, transaction);
            if (extracted == amount) break;
        }
        return resource.toStack(extracted);
    }

    public ItemStack extract(ItemStack sample, int amount, boolean simulate) {
        try (var transaction = Transaction.openRoot()) {
            ItemStack extracted = extract(sample, amount, transaction);
            if (!simulate) transaction.commit();
            return extracted;
        }
    }

    /** Drains barrels through their real inventory locations, including component replacement. */
    public FluidStack extractFluid(FluidStack sample, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonNegative(amount);
        if (sample.isEmpty() || amount == 0) return FluidStack.EMPTY;
        FluidResource resource = FluidResource.of(sample);
        int extracted = 0;
        for (var handler : fluidSources()) {
            extracted += handler.extract(resource, amount - extracted, transaction);
            if (extracted == amount) break;
        }
        return resource.toStack(extracted);
    }

    public FluidStack extractFluid(FluidStack sample, int amount, boolean simulate) {
        try (var transaction = Transaction.openRoot()) {
            FluidStack extracted = extractFluid(sample, amount, transaction);
            if (!simulate) transaction.commit();
            return extracted;
        }
    }

    public ItemStack insert(ItemStack stack, TransactionContext transaction) {
        if (stack.isEmpty()) return ItemStack.EMPTY;
        ItemResource resource = ItemResource.of(stack);
        int inserted = 0;
        for (var handler : sources()) {
            inserted += handler.insert(resource, stack.getCount() - inserted, transaction);
            if (inserted == stack.getCount()) break;
        }
        return stack.copyWithCount(stack.getCount() - inserted);
    }

    public ItemStack insert(ItemStack stack, boolean simulate) {
        try (var transaction = Transaction.openRoot()) {
            ItemStack remainder = insert(stack, transaction);
            if (!simulate) transaction.commit();
            return remainder;
        }
    }

    /** Fills existing matching barrels before empty barrels; returns the accepted amount. */
    public int insertFluid(FluidStack stack, TransactionContext transaction) {
        if (stack.isEmpty()) return 0;
        var resource = FluidResource.of(stack);
        var sources = fluidSources();
        int remaining = stack.getAmount();
        for (int pass = 0; pass < 2 && remaining > 0; pass++) {
            for (var handler : sources) {
                if (remaining <= 0) break;
                var current = handler.getResource(0);
                boolean same = current.equals(resource);
                if (pass == 0 ? !same : !current.isEmpty()) continue;
                remaining -= handler.insert(resource, remaining, transaction);
            }
        }
        return stack.getAmount() - remaining;
    }

    public int insertFluid(FluidStack stack, boolean simulate) {
        try (var transaction = Transaction.openRoot()) {
            int inserted = insertFluid(stack, transaction);
            if (!simulate) transaction.commit();
            return inserted;
        }
    }

    public ItemStack extractIngredient(Ingredient ingredient, boolean simulate) {
        try (var transaction = Transaction.openRoot()) {
            for (var handler : sources()) {
                for (int i = 0; i < handler.size(); i++) {
                    var resource = handler.getResource(i);
                    if (resource.isEmpty() || !ingredient.test(resource.toStack())) continue;
                    int got = handler.extract(i, resource, 1, transaction);
                    if (got == 0) continue;
                    if (!simulate) transaction.commit();
                    return resource.toStack(got);
                }
            }
        }
        return ItemStack.EMPTY;
    }

    public void accountInto(StackedItemContents contents) {
        for (ItemStack stack : allSlots()) if (!stack.isEmpty()) contents.accountStack(stack);
    }
}
