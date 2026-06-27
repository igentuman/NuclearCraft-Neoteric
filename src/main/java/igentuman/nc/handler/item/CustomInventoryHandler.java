package igentuman.nc.handler.item;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;

public class CustomInventoryHandler extends ItemStackHandler {

    public CustomInventoryHandler(int size) {
        super(size);
    }

    public void voidSlot(int slot) {
        setStackInSlot(slot, ItemStack.EMPTY);
    }

    public static CustomInventoryHandler init(int size, Runnable onChanged) {
        return new CustomInventoryHandler(size) {
            @Override
            protected void onContentsChanged(int slot) {
                onChanged.run();
            }
        };
    }
}
