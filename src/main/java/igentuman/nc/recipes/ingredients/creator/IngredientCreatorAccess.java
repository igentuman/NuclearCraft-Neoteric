package igentuman.nc.recipes.ingredients.creator;

import java.util.function.Consumer;

public class IngredientCreatorAccess {

    private IngredientCreatorAccess() {
    }

    private static IItemStackIngredientCreator ITEM_STACK_INGREDIENT_CREATOR;
    private static IFluidStackIngredientCreator FLUID_STACK_INGREDIENT_CREATOR;

    /**
     * Gets the item stack ingredient creator.
     */
    public static IItemStackIngredientCreator item() {
        if (ITEM_STACK_INGREDIENT_CREATOR == null) {
            lookupInstance(IItemStackIngredientCreator.class, "igentuman.nc.recipes.ingredients.creator.ItemStackIngredientCreator",
                  helper -> ITEM_STACK_INGREDIENT_CREATOR = helper);
        }
        return ITEM_STACK_INGREDIENT_CREATOR;
    }

    /**
     * Gets the fluid stack ingredient creator.
     */
    public static IFluidStackIngredientCreator fluid() {
        if (FLUID_STACK_INGREDIENT_CREATOR == null) {//Harmless race
            lookupInstance(IFluidStackIngredientCreator.class, "nuclearcraft.common.recipe.ingredient.creator.FluidStackIngredientCreator",
                  helper -> FLUID_STACK_INGREDIENT_CREATOR = helper);
        }
        return FLUID_STACK_INGREDIENT_CREATOR;
    }


    private static <TYPE extends IIngredientCreator<?, ?, ?>> void lookupInstance(Class<TYPE> type, String className, Consumer<TYPE> setter) {
        try {
            Class<?> clazz = Class.forName(className);
            setter.accept(type.cast(clazz.getField("INSTANCE").get(null)));
        } catch (ReflectiveOperationException ex) {

        }
    }
}