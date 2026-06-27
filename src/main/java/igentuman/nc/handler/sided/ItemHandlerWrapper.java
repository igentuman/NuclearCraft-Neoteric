package igentuman.nc.handler.sided;

import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

public class ItemHandlerWrapper implements IItemHandler {

    private final ItemCapabilityHandler handler;
    private final Direction side;

    public ItemHandlerWrapper(ItemCapabilityHandler handler, Direction side) {
        this.handler = handler;
        this.side = side;
    }

    @Override
    public int getSlots() {
        return handler.getSlots();
    }

    @Override
    public @NotNull ItemStack getStackInSlot(int slot) {
        return handler.getStackInSlot(slot);
    }

    @Override
    public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        if (!handler.canInsertFromSide(slot, stack, side)) return stack;
        return handler.insertItem(slot, stack, simulate);
    }

    @Override
    public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (!handler.canExtractFromSide(slot, side)) return ItemStack.EMPTY;
        return handler.extractItem(slot, amount, simulate);
    }

    @Override
    public int getSlotLimit(int slot) {
        return handler.getSlotLimit(slot);
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        if (!handler.canInsertFromSide(slot, stack, side)) return false;
        return handler.isItemValid(slot, stack);
    }
}
