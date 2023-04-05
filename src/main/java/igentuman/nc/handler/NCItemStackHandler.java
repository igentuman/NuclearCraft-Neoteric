package igentuman.nc.handler;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;

public class NCItemStackHandler extends ItemStackHandler {
    public NCItemStackHandler(int i) {
        super(i);
    }

    public ItemStack extractItemInternal(int slot, int amount, boolean simulate)
    {
        return super.extractItem(slot, amount, simulate);
    }

    public ItemStack insertItemInternal(int slot, @Nonnull ItemStack stack, boolean simulate) {
        if(slot != 0) return ItemStack.EMPTY;
        return super.insertItem(slot, stack, simulate);
    }

}
