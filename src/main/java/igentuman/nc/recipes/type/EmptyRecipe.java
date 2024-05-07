package igentuman.nc.recipes.type;

import igentuman.nc.recipes.ingredient.FluidStackIngredient;
import net.minecraftforge.fluids.FluidStack;

public class EmptyRecipe extends NcRecipe {

    public EmptyRecipe() {
        super(null, new FluidStackIngredient[0], new FluidStack[0], 1D, 1D, 1D, 1D);
    }
}
