package igentuman.nc.handler.sided.capability;

import igentuman.nc.handler.sided.SlotModePair;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

/*
 * WrappedHandler by noeppi_noeppi
 * under https://github.com/ModdingX/LibX/blob/1.19/LICENSE
 *
 */
public class ItemHandlerWrapper implements IItemHandlerModifiable {
    private final ItemCapabilityHandler handler;
    private final Predicate<Integer> extract;
    private final BiPredicate<Integer, ItemStack> insert;

    private final Direction side;

    public ItemHandlerWrapper(
            Direction side,
            ItemCapabilityHandler handler,
            Predicate<Integer> extract,
            BiPredicate<Integer, ItemStack> insert
    ) {
        this.handler = handler;
        this.extract = extract;
        this.insert = insert;
        this.side = side;
    }

    @Override
    public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
        this.handler.setStackInSlot(slot, stack);
    }

    @Override
    public int getSlots() {
        return this.handler.getSlots();
    }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int slot) {
        return this.handler.getStackInSlot(slot);
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        return this.insert.test(slot, stack) ? this.handler.insertItem(slot, stack, simulate) : stack;
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if( side != null &&
                (handler.getMode(slot, 0) == SlotModePair.SlotMode.INPUT ||
                        handler.getMode(slot, 0) == SlotModePair.SlotMode.PULL)
        ) {
            return ItemStack.EMPTY;
        }
        return this.extract.test(slot) ? this.handler.extractItem(slot, amount, simulate) : ItemStack.EMPTY;
    }

    @Override
    public int getSlotLimit(int slot) {
        return this.handler.getSlotLimit(slot);
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        return this.insert.test(slot, stack) && this.handler.isItemValid(slot, stack);
    }
}