package igentuman.nc.util.inventory;

import igentuman.nc.util.ItemDataUtils;
import igentuman.nc.util.NBTConstants;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;

public interface IItemSustainedInventory extends ISustainedInventory {

    @Override
    default void setInventory(ListTag nbtTags, Object... data) {
        if (data[0] instanceof ItemStack stack) {
            ItemDataUtils.setListOrRemove(stack, NBTConstants.ITEMS, nbtTags);
        }
    }

    @Override
    default ListTag getInventory(Object... data) {
        if (data[0] instanceof ItemStack stack) {
            return ItemDataUtils.getList(stack, NBTConstants.ITEMS);
        }
        return null;
    }

    default boolean canContentsDrop(ItemStack stack) {
        return true;
    }
}