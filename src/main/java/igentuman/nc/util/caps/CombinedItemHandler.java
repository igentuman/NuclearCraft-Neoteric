package igentuman.nc.util.caps;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

/** Exposes multiple item handlers as one, indexing their slots sequentially across delegates. */
public class CombinedItemHandler implements IItemHandler {

    private final IItemHandler[] delegates;
    private final int totalSlots;

    public CombinedItemHandler(IItemHandler... delegates) {
        this.delegates = delegates;
        int total = 0;
        for (IItemHandler handler : delegates) {
            total += handler.getSlots();
        }
        this.totalSlots = total;
    }

    private HandlerSlot resolve(int slot) {
        int offset = 0;
        for (IItemHandler handler : delegates) {
            int size = handler.getSlots();
            if (slot < offset + size) {
                return new HandlerSlot(handler, slot - offset);
            }
            offset += size;
        }
        return null;
    }

    @Override
    public int getSlots() {
        return totalSlots;
    }

    @Override
    public @NotNull ItemStack getStackInSlot(int slot) {
        HandlerSlot hs = resolve(slot);
        return hs != null ? hs.handler.getStackInSlot(hs.slot) : ItemStack.EMPTY;
    }

    @Override
    public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        HandlerSlot hs = resolve(slot);
        return hs != null ? hs.handler.insertItem(hs.slot, stack, simulate) : stack;
    }

    @Override
    public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
        HandlerSlot hs = resolve(slot);
        return hs != null ? hs.handler.extractItem(hs.slot, amount, simulate) : ItemStack.EMPTY;
    }

    @Override
    public int getSlotLimit(int slot) {
        HandlerSlot hs = resolve(slot);
        return hs != null ? hs.handler.getSlotLimit(hs.slot) : 0;
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        HandlerSlot hs = resolve(slot);
        return hs != null && hs.handler.isItemValid(hs.slot, stack);
    }

    private record HandlerSlot(IItemHandler handler, int slot) {}
}
