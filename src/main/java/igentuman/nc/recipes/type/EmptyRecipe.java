package igentuman.nc.recipes.type;

import igentuman.nc.recipes.ingredient.FluidStackIngredient;
import igentuman.nc.recipes.ingredient.ItemStackIngredient;

public class EmptyRecipe extends NcRecipe {
    public EmptyRecipe() {
        super(null, new ItemStackIngredient[0], new ItemStackIngredient[0], new FluidStackIngredient[0], new FluidStackIngredient[0], 0, 0, 0, 0);
    }

    @Override
    public boolean isIncomplete() {
        return true;
    }
}
