package igentuman.nc.recipe;

import igentuman.nc.setup.Registers;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeInput;

/** Common recipe-book metadata for recipes executed by NuclearCraft machines and server systems. */
public interface MachineRecipe<T extends RecipeInput> extends Recipe<T> {
    @Override
    default boolean isSpecial() {
        return true;
    }

    @Override
    default boolean showNotification() {
        return false;
    }

    @Override
    default String group() {
        return "";
    }

    @Override
    default PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    default RecipeBookCategory recipeBookCategory() {
        return Registers.PROCESSOR_RECIPE_CATEGORY.get();
    }
}
