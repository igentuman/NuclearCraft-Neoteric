package igentuman.nc.recipes.ingredient.creator;

/**
 * Provides access to helpers for creating various types of ingredients.
 */
public class IngredientCreatorAccess {

    private IngredientCreatorAccess() {
    }

    /**
     * Gets the item stack ingredient creator.
     */
    public static IItemStackIngredientCreator item() {
        return ItemStackIngredientCreator.INSTANCE;
    }

    /**
     * Gets the fluid stack ingredient creator.
     */
    public static IFluidStackIngredientCreator fluid() {
        return FluidStackIngredientCreator.INSTANCE;
    }
}
