package igentuman.nc.container.elements;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class NCSlotItemHandler extends SlotItemHandler {

    private Item allowed;
    public NCSlotItemHandler(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
        super(itemHandler, index, xPosition, yPosition);
    }

    public Slot allowed(Item item) {
        allowed = item;
        return this;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        if(allowed == null)
            return stack.getItem().equals(allowed);
        return super.mayPlace(stack);
    }

    public static class Output extends NCSlotItemHandler {
        public Output(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
            super(itemHandler, index, xPosition, yPosition);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }
    }

    public static class Input extends NCSlotItemHandler {
        public Input(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
            super(itemHandler, index, xPosition, yPosition);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return true;
        }
    }

    public static class ReadOnly extends NCSlotItemHandler {
        public ReadOnly(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
            super(itemHandler, index, xPosition, yPosition);
        }
    }
}
