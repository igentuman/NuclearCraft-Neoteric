package igentuman.nc.handler.crafter;

import igentuman.nc.item.ContainerBlockItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class AggregatedItemHandler implements IItemHandler {

    private final IItemHandler containers;

    public AggregatedItemHandler(IItemHandler containers) {
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

    private record Loc(IItemHandler handler, int slot) {}

    @Nullable
    private Loc locate(int slot) {
        for (IItemHandler h : sources()) {
            int n = h.getSlots();
            if (slot < n) return new Loc(h, slot);
            slot -= n;
        }
        return null;
    }

    @Override
    public int getSlots() {
        int total = 0;
        for (IItemHandler h : sources()) total += h.getSlots();
        return total;
    }

    @Override
    public @NotNull ItemStack getStackInSlot(int slot) {
        Loc l = locate(slot);
        return l == null ? ItemStack.EMPTY : l.handler.getStackInSlot(l.slot);
    }

    @Override
    public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        Loc l = locate(slot);
        return l == null ? stack : l.handler.insertItem(l.slot, stack, simulate);
    }

    @Override
    public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
        Loc l = locate(slot);
        return l == null ? ItemStack.EMPTY : l.handler.extractItem(l.slot, amount, simulate);
    }

    @Override
    public int getSlotLimit(int slot) {
        Loc l = locate(slot);
        return l == null ? 0 : l.handler.getSlotLimit(l.slot);
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        Loc l = locate(slot);
        return l != null && l.handler.isItemValid(l.slot, stack);
    }
}
