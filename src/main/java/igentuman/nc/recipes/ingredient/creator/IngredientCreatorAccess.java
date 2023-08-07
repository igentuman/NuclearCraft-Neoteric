package igentuman.nc.recipes.ingredient.creator;

import igentuman.nc.NuclearCraft;

import java.util.function.Consumer;

/**
 * Provides access to helpers for creating various types of ingredients.
 */
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
            lookupInstance(IItemStackIngredientCreator.class, "igentuman.nc.recipes.ingredient.creator.ItemStackIngredientCreator",
                  helper -> ITEM_STACK_INGREDIENT_CREATOR = helper);
        }
        return ITEM_STACK_INGREDIENT_CREATOR;
    }

    /**
     * Gets the fluid stack ingredient creator.
     */
    public static IFluidStackIngredientCreator fluid() {
        if (FLUID_STACK_INGREDIENT_CREATOR == null) {
            lookupInstance(IFluidStackIngredientCreator.class, "igentuman.nc.recipes.ingredient.creator.FluidStackIngredientCreator",
                  helper -> FLUID_STACK_INGREDIENT_CREATOR = helper);
        }
        return FLUID_STACK_INGREDIENT_CREATOR;
    }


    private static <TYPE extends IIngredientCreator<?, ?, ?>> void lookupInstance(Class<TYPE> type, String className, Consumer<TYPE> setter) {
        try {
            Class<?> clazz = Class.forName(className);
            setter.accept(type.cast(clazz.getField("INSTANCE").get(null)));
        } catch (ReflectiveOperationException ex) {
            NuclearCraft.LOGGER.error("Error retrieving {}, Nuclearcraft may be absent, damaged, or outdated.", className);
        }
    }
}