package igentuman.nc.recipes;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public interface INCRecipe {

    ItemStack getInputStack();

    ItemStack getOutput();

    CompoundTag serialize();

}
