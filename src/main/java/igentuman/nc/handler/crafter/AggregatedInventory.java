package igentuman.nc.handler.crafter;

import igentuman.nc.item.ContainerBlockItem;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    private final IItemHandler containers;

    public AggregatedInventory(IItemHandler containers) {
        this.containers = containers;
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

    public ItemStack insert(ItemStack stack, boolean simulate) {
        ItemStack remaining = stack.copy();
        for (IItemHandler h : sources()) {
            for (int i = 0; i < h.getSlots() && !remaining.isEmpty(); i++) {
                remaining = h.insertItem(i, remaining, simulate);
            }
        }
        return remaining;
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
