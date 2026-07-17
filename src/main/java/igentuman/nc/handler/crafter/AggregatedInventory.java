package igentuman.nc.handler.crafter;

import igentuman.nc.item.ContainerBlockItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Aggregates the item contents of every {@link ContainerBlockItem} held in the crafter's container
 * slots into a single logical inventory. Items with distinct NBT are kept as separate entries.
 */
public class AggregatedInventory {

    public record ItemKey(Item item, @Nullable CompoundTag tag) {
        public static ItemKey of(ItemStack stack) {
            return new ItemKey(stack.getItem(), stack.hasTag() ? stack.getTag().copy() : null);
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
            stack.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(out::add);
        }
        return out;
    }

    /**
     * All slots of every inserted container, concatenated in container/slot order (empties included).
     * Size equals the summed capacity of the inserted container items; empty when no containers present.
     */
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
                if (!s.isEmpty() && ItemStack.isSameItemSameTags(s, sample)) total += s.getCount();
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
                if (s.isEmpty() || !ItemStack.isSameItemSameTags(s, sample)) continue;
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

    /** Pulls a single item matching the ingredient from the first container slot that satisfies it. */
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

    /** Feeds every stored item into a {@link StackedContents} for vanilla recipe-feasibility checks. */
    public void accountInto(StackedContents contents) {
        for (IItemHandler h : sources()) {
            for (int i = 0; i < h.getSlots(); i++) {
                ItemStack s = h.getStackInSlot(i);
                if (!s.isEmpty()) contents.accountStack(s);
            }
        }
    }
}
