package igentuman.nc.recipes;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.item.ItemStack;

public interface INCRecipe {

    ItemStack getInputStack();

    ItemStack getOutput();

    CompoundNBT serialize();

}
