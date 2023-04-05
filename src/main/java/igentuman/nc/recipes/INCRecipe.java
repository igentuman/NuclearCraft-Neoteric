package igentuman.nc.recipes;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;

public interface INCRecipe {
    ItemStack getResultItem();

    ItemStack getInputStack();

    ItemStack getOutput();

    CompoundTag serialize();
    INCRecipe deserialize(CompoundTag tag);
}
