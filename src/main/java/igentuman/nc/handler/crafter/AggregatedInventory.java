package igentuman.nc.handler.crafter;

import igentuman.nc.item.ContainerBlockItem;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.Nullable;

public class AggregatedInventory {

    public record ItemKey(Item item, @Nullable CompoundTag tag) {
        public static ItemKey of(ItemStack stack) {
            return new ItemKey(stack.getItem(), stack.hasTag() ? stack.getTag().copy() : null);
        }
        public ItemStack sample() {
            ItemStack stack = new ItemStack(item);
            if (tag != null) stack.setTag(tag.copy());
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
                && (stack.getItem() instanceof ContainerBlockItem || stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).isPresent());
    }

    private List<IItemHandler> sources() {
        List<IItemHandler> out = new ArrayList<>();
        for (int i = 0; i < containers.getSlots(); i++) {
            ItemStack stack = containers.getStackInSlot(i);
            if (stack.isEmpty() || !(stack.getItem() instanceof ContainerBlockItem)) continue;
            IItemHandler handler = stack.getCapability(ForgeCapabilities.ITEM_HANDLER).resolve().orElse(null);
            if (handler != null) out.add(handler);
        }
        return out;
    }

    private List<IFluidHandler> fluidSources() {
        List<IFluidHandler> out = new ArrayList<>();
        for (int i = 0; i < containers.getSlots(); i++) {
            ItemStack stack = containers.getStackInSlot(i);
            if (stack.isEmpty()) continue;
            IFluidHandlerItem handler = stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).resolve().orElse(null);
            if (handler != null) out.add(new StoredFluidHandler(i, handler));
        }
        return out;
    }

    private final class StoredFluidHandler implements IFluidHandler {
        private final int slot;
        private final IFluidHandlerItem handler;

        private StoredFluidHandler(int slot, IFluidHandlerItem handler) {
            this.slot = slot;
            this.handler = handler;
        }

        private void changed(FluidAction action, int amount) {
            if (action.execute() && amount > 0 && containers instanceof IItemHandlerModifiable inventory) {
                inventory.setStackInSlot(slot, handler.getContainer());
            }
        }

        @Override
        public int getTanks() { return handler.getTanks(); }
        @Override
        public FluidStack getFluidInTank(int tank) { return handler.getFluidInTank(tank); }
        @Override
        public int getTankCapacity(int tank) { return handler.getTankCapacity(tank); }
        @Override
        public boolean isFluidValid(int tank, FluidStack stack) { return handler.isFluidValid(tank, stack); }
        @Override
        public int fill(FluidStack stack, FluidAction action) {
            int filled = handler.fill(stack, action);
            changed(action, filled);
            return filled;
        }
        @Override
        public FluidStack drain(FluidStack stack, FluidAction action) {
            FluidStack drained = handler.drain(stack, action);
            changed(action, drained.getAmount());
            return drained;
        }
        @Override
        public FluidStack drain(int amount, FluidAction action) {
            FluidStack drained = handler.drain(amount, action);
            changed(action, drained.getAmount());
            return drained;
        }
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
                reps.computeIfAbsent(key, k -> new FluidStack(f, 1));
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
                if (!s.isEmpty() && ItemStack.isSameItemSameTags(s, sample)) total += s.getCount();
            }
        }
        return total;
    }

    public int countFluid(FluidStack sample) {
        int total = 0;
        for (IFluidHandler h : fluidSources()) {
            for (int t = 0; t < h.getTanks(); t++) {
                FluidStack f = h.getFluidInTank(t);
                if (!f.isEmpty() && f.isFluidEqual(sample)) total += f.getAmount();
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

    public FluidStack extractFluid(FluidStack sample, int amount, boolean simulate) {
        FluidStack out = FluidStack.EMPTY;
        int remaining = amount;
        IFluidHandler.FluidAction action = simulate ? IFluidHandler.FluidAction.SIMULATE : IFluidHandler.FluidAction.EXECUTE;
        for (IFluidHandler h : fluidSources()) {
            if (remaining <= 0) break;
            FluidStack got = h.drain(new FluidStack(sample, remaining), action);
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

    public int insertFluid(FluidStack stack, boolean simulate) {
        if (stack.isEmpty()) return 0;
        IFluidHandler.FluidAction action = simulate ? IFluidHandler.FluidAction.SIMULATE : IFluidHandler.FluidAction.EXECUTE;
        List<IFluidHandler> sources = fluidSources();
        int remaining = stack.getAmount();
        sources.sort((a, b) -> Boolean.compare(containsFluid(b, stack), containsFluid(a, stack)));
        for (IFluidHandler handler : sources) {
            if (remaining <= 0) break;
            remaining -= handler.fill(new FluidStack(stack, remaining), action);
        }
        return stack.getAmount() - remaining;
    }

    private static boolean containsFluid(IFluidHandler handler, FluidStack sample) {
        for (int tank = 0; tank < handler.getTanks(); tank++) {
            if (handler.getFluidInTank(tank).isFluidEqual(sample)) return true;
        }
        return false;
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
