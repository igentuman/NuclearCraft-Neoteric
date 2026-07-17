package igentuman.nc.recipes.type;

import igentuman.nc.recipes.ingredient.FluidStackIngredient;
import igentuman.nc.recipes.ingredient.ItemStackIngredient;
import net.minecraft.resources.ResourceLocation;

public class EmptyRecipe extends NcRecipe {

    public EmptyRecipe(ResourceLocation recipeId) {
        super(recipeId, new ItemStackIngredient[0], new ItemStackIngredient[0], new FluidStackIngredient[0], new FluidStackIngredient[0], 0, 0, 0, 0);
    }

    @Override
    public boolean isIncomplete() {
        return true;
    }
}
