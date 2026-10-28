package igentuman.nc.handler.crafter;

import igentuman.nc.item.BarrelBlockItem;
import igentuman.nc.item.ContainerBlockItem;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.neoforged.neoforge.items.IItemHandler;

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

    private final IItemHandler containers;

    public AggregatedInventory(IItemHandler containers) {
        this.containers = containers;
    }

    public static boolean isStorageItem(ItemStack stack) {
        return !stack.isEmpty()
                && (stack.getItem() instanceof ContainerBlockItem || stack.getItem() instanceof BarrelBlockItem);
    }

    private List<IItemHandler> sources() {
        List<IItemHandler> out = new ArrayList<>();
        for (int i = 0; i < containers.getSlots(); i++) {
            ItemStack stack = containers.getStackInSlot(i);
            if (stack.isEmpty() || !(stack.getItem() instanceof ContainerBlockItem)) continue;
            IItemHandler handler = stack.getCapability(Capabilities.ItemHandler.ITEM);
            if (handler != null) out.add(handler);
        }
        return out;
    }

    private List<IFluidHandler> fluidSources() {
        List<IFluidHandler> out = new ArrayList<>();
        for (int i = 0; i < containers.getSlots(); i++) {
            ItemStack stack = containers.getStackInSlot(i);
            if (stack.isEmpty() || !(stack.getItem() instanceof BarrelBlockItem)) continue;
            IFluidHandlerItem handler = stack.getCapability(Capabilities.FluidHandler.ITEM);
            if (handler != null) out.add(handler);
        }
        return out;
    }

    public List<ItemStack> allSlots() {
        List<ItemStack> out = new ArrayList<>();
        for (IItemHandler h : sources()) {
            for (int i = 0; i < h.getSlots(); i++) {
                out.add(h.getStackInSlot(i));
            }
        }
        return out;
    }

    public List<Entry> entries() {
        Map<ItemKey, Integer> counts = new LinkedHashMap<>();
        Map<ItemKey, ItemStack> reps = new LinkedHashMap<>();
        for (IItemHandler h : sources()) {
            for (int i = 0; i < h.getSlots(); i++) {
                ItemStack s = h.getStackInSlot(i);
                if (s.isEmpty()) continue;
                ItemKey key = ItemKey.of(s);
                counts.merge(key, s.getCount(), Integer::sum);
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
        for (IFluidHandler h : fluidSources()) {
            for (int t = 0; t < h.getTanks(); t++) {
                FluidStack f = h.getFluidInTank(t);
                if (f.isEmpty()) continue;
                CraftKey key = CraftKey.of(f);
                amounts.merge(key, f.getAmount(), Integer::sum);
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
        for (IItemHandler h : sources()) {
            for (int i = 0; i < h.getSlots(); i++) {
                ItemStack s = h.getStackInSlot(i);
                if (!s.isEmpty() && ItemStack.isSameItemSameComponents(s, sample)) total += s.getCount();
            }
        }
        return total;
    }

    public int countFluid(FluidStack sample) {
        int total = 0;
        for (IFluidHandler h : fluidSources()) {
            for (int t = 0; t < h.getTanks(); t++) {
                FluidStack f = h.getFluidInTank(t);
                if (!f.isEmpty() && FluidStack.isSameFluidSameComponents(f, sample)) total += f.getAmount();
            }
        }
        return total;
    }

    public ItemStack extract(ItemStack sample, int amount, boolean simulate) {
        ItemStack out = ItemStack.EMPTY;
        int remaining = amount;
        for (IItemHandler h : sources()) {
            for (int i = 0; i < h.getSlots() && remaining > 0; i++) {
                ItemStack s = h.getStackInSlot(i);
                if (s.isEmpty() || !ItemStack.isSameItemSameComponents(s, sample)) continue;
                ItemStack got = h.extractItem(i, remaining, simulate);
                if (got.isEmpty()) continue;
                if (out.isEmpty()) out = got.copy();
                else out.grow(got.getCount());
                remaining -= got.getCount();
            }
        }
        return out;
    }

    /** Drains up to {@code amount} mB of the sample fluid across all barrels. */
    public FluidStack extractFluid(FluidStack sample, int amount, boolean simulate) {
        FluidStack out = FluidStack.EMPTY;
        int remaining = amount;
        IFluidHandler.FluidAction action = simulate ? IFluidHandler.FluidAction.SIMULATE : IFluidHandler.FluidAction.EXECUTE;
        for (IFluidHandler h : fluidSources()) {
            if (remaining <= 0) break;
            FluidStack got = h.drain(sample.copyWithAmount(remaining), action);
            if (got.isEmpty()) continue;
            if (out.isEmpty()) out = got.copy();
            else out.grow(got.getAmount());
            remaining -= got.getAmount();
        }
        return out;
    }

    public ItemStack insert(ItemStack stack, boolean simulate) {
        ItemStack remaining = stack.copy();
        for (IItemHandler h : sources()) {
            for (int i = 0; i < h.getSlots() && !remaining.isEmpty(); i++) {
                remaining = h.insertItem(i, remaining, simulate);
            }
        }
        return remaining;
    }

    /** Fills barrels with the fluid; barrels already holding that fluid are preferred. Returns the amount accepted. */
    public int insertFluid(FluidStack stack, boolean simulate) {
        if (stack.isEmpty()) return 0;
        IFluidHandler.FluidAction action = simulate ? IFluidHandler.FluidAction.SIMULATE : IFluidHandler.FluidAction.EXECUTE;
        List<IFluidHandler> sources = fluidSources();
        int remaining = stack.getAmount();
        for (int pass = 0; pass < 2 && remaining > 0; pass++) {
            for (IFluidHandler h : sources) {
                if (remaining <= 0) break;
                FluidStack current = h.getFluidInTank(0);
                boolean same = !current.isEmpty() && FluidStack.isSameFluidSameComponents(current, stack);
                if (pass == 0 ? !same : !current.isEmpty()) continue;
                remaining -= h.fill(stack.copyWithAmount(remaining), action);
            }
        }
        return stack.getAmount() - remaining;
    }

    public ItemStack extractIngredient(Ingredient ingredient, boolean simulate) {
        for (IItemHandler h : sources()) {
            for (int i = 0; i < h.getSlots(); i++) {
                ItemStack s = h.getStackInSlot(i);
                if (s.isEmpty() || !ingredient.test(s)) continue;
                ItemStack got = h.extractItem(i, 1, simulate);
                if (!got.isEmpty()) return got;
            }
        }
        return ItemStack.EMPTY;
    }

    public void accountInto(StackedContents contents) {
        for (IItemHandler h : sources()) {
            for (int i = 0; i < h.getSlots(); i++) {
                ItemStack s = h.getStackInSlot(i);
                if (!s.isEmpty()) contents.accountStack(s);
            }
        }
    }
}
