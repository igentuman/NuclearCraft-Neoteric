package igentuman.nc.util;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

public final class IgnoredIInventory implements RecipeInput {

    public static final IgnoredIInventory INSTANCE = new IgnoredIInventory();

    private IgnoredIInventory() {
    }

    @Override
    public ItemStack getItem(int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public int size() {
        return 0;
    }
}
