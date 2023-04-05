package igentuman.nc.recipes;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;

public abstract class NcRecipe implements Recipe<SimpleContainer>, INCRecipe {

    public static NcRecipe getRecipeFromTag(CompoundTag tag)
    {
        if(tag == null) return null;
        String recipeType = tag.getString("type");
        switch (recipeType) {
            case "fission_reactor":
                return new FissionRecipe(
                        ItemStack.of(tag.getCompound("input")),
                        ItemStack.of(tag.getCompound("output")));
        }
        return null;
    }
}