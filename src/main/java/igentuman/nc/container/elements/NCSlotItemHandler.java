package igentuman.nc.container.elements;

import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

import java.util.ArrayList;
import java.util.List;

public class NCSlotItemHandler extends SlotItemHandler {

    private List<Item> allowed = new ArrayList<>();
    public NCSlotItemHandler(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
        super(itemHandler, index, xPosition, yPosition);
    }

    public Slot allowed(Item item) {
        allowed.clear();
        allowed.add(item);
        return this;
    }

    public Slot allowed(List<Item> items) {
        allowed.clear();
        allowed.addAll(items);
        return this;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        if(hidden)
            return false;
        if(!allowed.isEmpty())
            return allowed.contains(stack.getItem());
        return super.mayPlace(stack);
    }
    public boolean hidden = false;
    public Slot hidden() {
        hidden = true;
        return this;
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

    }

    public static class ReadOnly extends NCSlotItemHandler {
        public ReadOnly(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
            super(itemHandler, index, xPosition, yPosition);
        }
    }
}
